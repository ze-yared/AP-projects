package client;

import database.DB;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Client extends Application {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private String username;
    private DB database;
    private boolean connected = false;
    private Stage mainStage;
    
    // UI Components
    private TextArea chatArea;
    private TextField messageField;
    private TextField serverField;
    private TextField portField;
    private TextField usernameField;
    private Button connectBtn;
    private Button sendBtn;
    private Button fileBtn;
    private Label statusLabel;
    private Label timeLabel;
    
    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        database = new DB();
        
        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a1a;");
        
        // Top connection panel
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(10));
        topPanel.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #3d3d3d; -fx-border-width: 0 0 1 0;");
        
        GridPane connectionGrid = new GridPane();
        connectionGrid.setHgap(10);
        connectionGrid.setVgap(10);
        connectionGrid.setAlignment(Pos.CENTER);
        
        Label serverLabel = new Label("Server:");
        serverLabel.setTextFill(Color.WHITE);
        serverLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        serverField = new TextField("localhost");
        serverField.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-font-size: 12px;");
        
        Label portLabel = new Label("Port:");
        portLabel.setTextFill(Color.WHITE);
        portField = new TextField("12345");
        portField.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white;");
        
        Label userLabel = new Label("Username:");
        userLabel.setTextFill(Color.WHITE);
        usernameField = new TextField();
        usernameField.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white;");
        
        connectBtn = new Button("Connect");
        connectBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        connectBtn.setOnAction(e -> connectToServer());
        
        connectionGrid.add(serverLabel, 0, 0);
        connectionGrid.add(serverField, 1, 0);
        connectionGrid.add(portLabel, 2, 0);
        connectionGrid.add(portField, 3, 0);
        connectionGrid.add(userLabel, 4, 0);
        connectionGrid.add(usernameField, 5, 0);
        connectionGrid.add(connectBtn, 6, 0);
        
        // Status bar
        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusLabel = new Label("⚫ OFFLINE");
        statusLabel.setTextFill(Color.RED);
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        timeLabel = new Label();
        timeLabel.setTextFill(Color.GRAY);
        timeLabel.setFont(Font.font("Arial", 10));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statusBar.getChildren().addAll(statusLabel, spacer, timeLabel);
        
        topPanel.getChildren().addAll(connectionGrid, statusBar);
        root.setTop(topPanel);
        
        // Chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: #d4d4d4; -fx-font-size: 13px; -fx-font-family: 'Consolas';");
        chatArea.setFont(Font.font("Consolas", 13));
        chatArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        
        root.setCenter(chatArea);
        
        // Bottom input panel
        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #3d3d3d; -fx-border-width: 1 0 0 0;");
        
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        
        messageField = new TextField();
        messageField.setDisable(true);
        messageField.setPromptText("Type your message here...");
        messageField.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-font-size: 13px; -fx-prompt-text-fill: #888;");
        messageField.setOnAction(e -> sendMessage());
        
        sendBtn = new Button("Send");
        sendBtn.setDisable(true);
        sendBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        sendBtn.setOnAction(e -> sendMessage());
        
        fileBtn = new Button("📎 Send File");
        fileBtn.setDisable(true);
        fileBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        fileBtn.setOnAction(e -> sendFile());
        
        inputBox.getChildren().addAll(messageField, sendBtn, fileBtn);
        HBox.setHgrow(messageField, Priority.ALWAYS);
        
        bottomPanel.getChildren().add(inputBox);
        root.setBottom(bottomPanel);
        
        // Update time
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    timeLabel.setText(sdf.format(new Date()));
                });
            }
        }, 0, 1000);
        
        // Load chat history
        loadChatHistory();
        
        Scene scene = new Scene(root, 950, 650);
        primaryStage.setTitle("Chat Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void connectToServer() {
        String server = serverField.getText();
        int port = Integer.parseInt(portField.getText());
        username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            showError("Please enter a username!");
            return;
        }
        
        connectBtn.setDisable(true);
        connectBtn.setText("Connecting...");
        
        new Thread(() -> {
            try {
                socket = new Socket(server, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                dataOut = new DataOutputStream(socket.getOutputStream());
                dataIn = new DataInputStream(socket.getInputStream());
                
                // Send username
                out.println(username);
                
                Platform.runLater(() -> {
                    connected = true;
                    connectBtn.setText("Connected");
                    connectBtn.setStyle("-fx-background-color: #45a049; -fx-text-fill: white;");
                    serverField.setDisable(true);
                    portField.setDisable(true);
                    usernameField.setDisable(true);
                    messageField.setDisable(false);
                    sendBtn.setDisable(false);
                    fileBtn.setDisable(false);
                    statusLabel.setText("🟢 ONLINE as " + username);
                    statusLabel.setTextFill(Color.GREEN);
                    mainStage.setTitle("Chat Application - " + username);
                    appendSystemMessage("Connected to chat server!");
                });
                
                // Listen for messages
                String message;
                while ((message = in.readLine()) != null) {
                    final String msg = message;
                    if (msg.startsWith("FILE:")) {
                        // Handle incoming file
                        handleFileReceive(msg);
                    } else {
                        Platform.runLater(() -> {
                            // Display all messages except our own (we already displayed them)
                            if (!msg.startsWith(username + ":")) {
                                displayMessage(msg);
                                
                                // Save to database for regular messages
                                if (!msg.contains("===") && !msg.contains("JOINED") && !msg.contains("LEFT") && msg.contains(":")) {
                                    String[] parts = msg.split(":", 2);
                                    if (parts.length == 2 && !parts[1].contains("sent file:")) {
                                        database.saveMessage(parts[0].trim(), parts[1].trim());
                                    }
                                }
                            }
                        });
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("Connection failed: " + e.getMessage());
                    connectBtn.setDisable(false);
                    connectBtn.setText("Connect");
                    connectBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    statusLabel.setText("⚫ OFFLINE");
                    statusLabel.setTextFill(Color.RED);
                });
            }
        }).start();
    }
    
    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty() && connected) {
            // Send to server
            out.println(msg);
            
            // Display immediately on sender's screen
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String displayMsg = "[" + timestamp + "] " + username + ": " + msg;
            chatArea.appendText(displayMsg + "\n");
            
            // Save to database
            database.saveMessage(username, msg);
            
            messageField.clear();
            chatArea.setScrollTop(Double.MAX_VALUE);
        }
    }
    
    private void sendFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        File file = fileChooser.showOpenDialog(null);
        
        if (file != null && connected) {
            new Thread(() -> {
                try {
                    // Read file into bytes
                    byte[] fileData = new byte[(int) file.length()];
                    FileInputStream fis = new FileInputStream(file);
                    fis.read(fileData);
                    fis.close();
                    
                    // Send file header
                    out.println("/FILE/" + file.getName() + ":" + fileData.length);
                    
                    // Send file data
                    dataOut.write(fileData);
                    dataOut.flush();
                    
                    // Display on sender's screen
                    Platform.runLater(() -> {
                        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        String fileMsg = "[" + timestamp + "] " + username + ": 📎 sent file: " + file.getName();
                        chatArea.appendText(fileMsg + "\n");
                        database.saveMessage(username, "📎 sent file: " + file.getName());
                        chatArea.setScrollTop(Double.MAX_VALUE);
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showError("Failed to send file: " + e.getMessage());
                    });
                }
            }).start();
        }
    }
    
    private void handleFileReceive(String header) {
        try {
            String[] parts = header.split(":");
            String sender = parts[1];
            String fileName = parts[2];
            int fileSize = Integer.parseInt(parts[3]);
            
            // Read file data
            byte[] fileData = new byte[fileSize];
            dataIn.readFully(fileData);
            
            // Save file to received_files directory
            File receivedDir = new File("received_files");
            if (!receivedDir.exists()) receivedDir.mkdir();
            
            // Create unique filename to avoid conflicts
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filepath = "received_files/" + timestamp + "_" + fileName;
            
            try (FileOutputStream fos = new FileOutputStream(filepath)) {
                fos.write(fileData);
            }
            
            // Display file received message
            Platform.runLater(() -> {
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String fileMsg = "[" + time + "] 📥 " + sender + " sent file: " + fileName + " (Saved to: " + filepath + ")";
                chatArea.appendText(fileMsg + "\n");
                database.saveMessage(sender, "📎 sent file: " + fileName);
                chatArea.setScrollTop(Double.MAX_VALUE);
                
                // Show success dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("File Received");
                alert.setHeaderText("File received from " + sender);
                alert.setContentText("File: " + fileName + "\nSaved to: " + filepath);
                alert.show();
            });
            
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showError("Failed to receive file: " + e.getMessage());
            });
        }
    }
    
    private void displayMessage(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        
        if (message.startsWith("===")) {
            chatArea.appendText("\n" + message + "\n");
        } 
        else if (message.startsWith("✨") || message.startsWith("👋")) {
            chatArea.appendText("\n[" + timestamp + "] " + message + "\n");
        } 
        else if (message.contains("sent file:")) {
            chatArea.appendText("[" + timestamp + "] " + message + "\n");
        }
        else if (message.contains(":")) {
            chatArea.appendText("[" + timestamp + "] " + message + "\n");
        } 
        else {
            chatArea.appendText(message + "\n");
        }
        
        chatArea.setScrollTop(Double.MAX_VALUE);
    }
    
    private void appendSystemMessage(String msg) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        chatArea.appendText("[" + timestamp + "] ℹ " + msg + "\n");
        chatArea.setScrollTop(Double.MAX_VALUE);
    }
    
    private void loadChatHistory() {
        List<String> history = database.getHistory();
        if (!history.isEmpty()) {
            chatArea.appendText("═══════════════════════════════════════════════════\n");
            chatArea.appendText("              PREVIOUS CHAT HISTORY\n");
            chatArea.appendText("═══════════════════════════════════════════════════\n\n");
            for (String msg : history) {
                chatArea.appendText(msg + "\n");
            }
            chatArea.appendText("\n═══════════════════════════════════════════════════\n");
            chatArea.appendText("                 ACTIVE CHAT\n");
            chatArea.appendText("═══════════════════════════════════════════════════\n\n");
        }
    }
    
    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
    
    @Override
    public void stop() {
        try {
            if (socket != null) socket.close();
            if (database != null) database.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}