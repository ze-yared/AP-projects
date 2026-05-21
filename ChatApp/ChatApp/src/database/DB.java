package database;

import java.sql.*;
import java.util.*;

public class DB {
    private Connection conn;
    
    public DB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/chat_app", 
                "root", 
                "K8#xp!qL$29mN@Pz"
            );
            System.out.println("Database connected!");
        } catch (Exception e) {
            System.out.println("DB Error: " + e.getMessage());
        }
    }
    
    public void saveMessage(String username, String message) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO messages (username, message) VALUES (?, ?)"
            );
            stmt.setString(1, username);
            stmt.setString(2, message);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<String> getHistory() {
        List<String> history = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT username, message, timestamp FROM messages ORDER BY timestamp LIMIT 50"
            );
            while (rs.next()) {
                history.add("[" + rs.getString("timestamp") + "] " + 
                           rs.getString("username") + ": " + 
                           rs.getString("message"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
    
    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}