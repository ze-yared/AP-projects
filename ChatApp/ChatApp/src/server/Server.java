package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static List<ClientHandler> clients = new ArrayList<>();
    private static List<String> chatHistory = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════");
        System.out.println("  CHAT SERVER RUNNING ON PORT 12345");
        System.out.println("═══════════════════════════════");
        
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("✓ Server started! Waiting for clients...\n");
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("→ New client connected!");
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static void broadcast(String message, ClientHandler sender) {
        System.out.println("Broadcasting: " + message);
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.send(message);
            }
        }
    }
    
    static void broadcastFile(String senderName, String fileName, byte[] fileData, ClientHandler sender) {
        String header = "FILE:" + senderName + ":" + fileName + ":" + fileData.length;
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendFile(header, fileData);
            }
        }
    }
    
    static void addToHistory(String message) {
        chatHistory.add(message);
        if (chatHistory.size() > 100) {
            chatHistory.remove(0);
        }
    }
    
    static List<String> getHistory() {
        return new ArrayList<>(chatHistory);
    }
    
    static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("← Client disconnected. Total: " + clients.size());
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private String username;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataOut = new DataOutputStream(socket.getOutputStream());
            dataIn = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
            username = in.readLine();
            System.out.println("✓ " + username + " has joined!");
            
            // Send history to new client
            out.println("=== CHAT HISTORY ===");
            for (String msg : Server.getHistory()) {
                out.println(msg);
            }
            out.println("=== END OF HISTORY ===");
            
            // Announce to everyone
            String joinMsg = "✨ " + username + " JOINED THE CHAT ✨";
            Server.addToHistory(joinMsg);
            Server.broadcast(joinMsg, this);
            
            // Listen for messages
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/FILE/")) {
                    handleFileTransfer(message);
                } else {
                    String fullMsg = username + ": " + message;
                    System.out.println("💬 " + fullMsg);
                    Server.addToHistory(fullMsg);
                    Server.broadcast(fullMsg, this);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠ " + username + " disconnected");
        } finally {
            Server.removeClient(this);
            String leaveMsg = "👋 " + username + " LEFT THE CHAT 👋";
            Server.addToHistory(leaveMsg);
            Server.broadcast(leaveMsg, this);
            try { socket.close(); } catch (Exception e) {}
        }
    }
    
    private void handleFileTransfer(String header) throws IOException {
        String[] parts = header.substring(6).split(":");
        String fileName = parts[0];
        int fileSize = Integer.parseInt(parts[1]);
        
        // Read file data
        byte[] fileData = new byte[fileSize];
        dataIn.readFully(fileData);
        
        // Save file on server
        File saveDir = new File("server_files");
        if (!saveDir.exists()) saveDir.mkdir();
        String filepath = "server_files/" + System.currentTimeMillis() + "_" + fileName;
        
        try (FileOutputStream fos = new FileOutputStream(filepath)) {
            fos.write(fileData);
        }
        
        System.out.println("📁 File received: " + fileName + " from " + username);
        
        // Broadcast file to ALL other clients
        Server.broadcastFile(username, fileName, fileData, this);
        
        // Also send a text notification
        String fileMsg = "📎 " + username + " sent file: " + fileName;
        Server.addToHistory(fileMsg);
        Server.broadcast(fileMsg, this);
    }
    
    void send(String msg) {
        out.println(msg);
    }
    
    void sendFile(String header, byte[] fileData) {
        try {
            dataOut.writeBytes(header + "\n");
            dataOut.write(fileData);
            dataOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}