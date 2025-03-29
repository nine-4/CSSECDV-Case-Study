package Controller;


import Model.History;
import Model.Logs;
import Model.Product;
import Model.User;
import View.Frame;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener; 

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;


public class Main {
    
    public SQLite sqlite;
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;
    
    public static void main(String[] args) {
        new Main().init();
        Frame mainFrame = Frame.getInstance(); // Start main UI
        mainFrame.setVisible(true);

        startSessionChecker();
    }
    
    public void init(){
        // Initialize a driver object
        sqlite = new SQLite();

        // Create a database
        sqlite.createNewDatabase();

        // Drop users table if needed
        sqlite.dropHistoryTable();
        sqlite.dropLogsTable();
        sqlite.dropProductTable();
        sqlite.dropUserTable();

        // Create users table if not exist
        sqlite.createHistoryTable();
        sqlite.createLogsTable();
        sqlite.createProductTable();
        sqlite.createUserTable();

        // Add sample history
        sqlite.addHistory("admin", "Antivirus", 1, "2019-04-03 14:30:00.000");
        sqlite.addHistory("manager", "Firewall", 1, "2019-04-03 14:30:01.000");
        sqlite.addHistory("staff", "Scanner", 1, "2019-04-03 14:30:02.000");
        sqlite.addHistory("client2", "Antivirus", 3, "2019-04-03 14:30:03.000");

        // Add sample logs
        sqlite.addLogs("NOTICE", "admin", "User creation successful", new Timestamp(new Date().getTime()).toString());
        sqlite.addLogs("NOTICE", "manager", "User creation successful", new Timestamp(new Date().getTime()).toString());
        sqlite.addLogs("NOTICE", "admin", "User creation successful", new Timestamp(new Date().getTime()).toString());

        // Add sample product
        sqlite.addProduct("Antivirus", 5, 500.0);
        sqlite.addProduct("Firewall", 3, 1000.0);
        sqlite.addProduct("Scanner", 10, 100.0);

        // Add sample users
        sqlite.addUser("admin", this.hashPassword("qwerty1234") , 5);
        sqlite.addUser("manager", this.hashPassword("qwerty1234"), 4);
        sqlite.addUser("staff", this.hashPassword("qwerty1234"), 3);
        sqlite.addUser("client1", this.hashPassword("qwerty1234"), 2);
        sqlite.addUser("client2", this.hashPassword("qwerty1234"), 2);


        // Get users
        ArrayList<History> histories = sqlite.getHistory();
        for(int nCtr = 0; nCtr < histories.size(); nCtr++){
            System.out.println("===== History " + histories.get(nCtr).getId() + " =====");
            System.out.println(" Username: " + histories.get(nCtr).getUsername());
            System.out.println(" Name: " + histories.get(nCtr).getName());
            System.out.println(" Stock: " + histories.get(nCtr).getStock());
            System.out.println(" Timestamp: " + histories.get(nCtr).getTimestamp());
        }

        // Get users
        ArrayList<Logs> logs = sqlite.getLogs();
        for(int nCtr = 0; nCtr < logs.size(); nCtr++){
            System.out.println("===== Logs " + logs.get(nCtr).getId() + " =====");
            System.out.println(" Username: " + logs.get(nCtr).getEvent());
            System.out.println(" Password: " + logs.get(nCtr).getUsername());
            System.out.println(" Role: " + logs.get(nCtr).getDesc());
            System.out.println(" Timestamp: " + logs.get(nCtr).getTimestamp());
        }

        // Get users
        ArrayList<Product> products = sqlite.getProduct();
        for(int nCtr = 0; nCtr < products.size(); nCtr++){
            System.out.println("===== Product " + products.get(nCtr).getId() + " =====");
            System.out.println(" Name: " + products.get(nCtr).getName());
            System.out.println(" Stock: " + products.get(nCtr).getStock());
            System.out.println(" Price: " + products.get(nCtr).getPrice());
        }
        // Get users
        ArrayList<User> users = sqlite.getUsers();
        for(int nCtr = 0; nCtr < users.size(); nCtr++){
            System.out.println("===== User " + users.get(nCtr).getId() + " =====");
            System.out.println(" Username: " + users.get(nCtr).getUsername());
            System.out.println(" Password: " + users.get(nCtr).getPassword());
            System.out.println(" Role: " + users.get(nCtr).getRole());
            System.out.println(" Locked: " + users.get(nCtr).getLocked());
        }

        // Initialize User Interface
        Frame frame = new Frame();
        frame.init(this);
        
        
    }
    
    public String validateLogin(String username, String password) {
        

        ArrayList<User> users = sqlite.getUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                if (user.getPassword().equals(password) && user.getLocked() == 0) {
                    failedAttempts = 0; // Reset on successful login
                    return "SUCCESS"; // Indicate a successful login
                } 
                else {
                    failedAttempts++;
                    if (failedAttempts >= MAX_ATTEMPTS || user.getLocked() == 1) {
                        sqlite.lockUser(username, 1);
                        return "Too many failed attempts. Account locked. Reach out to the admin to unlock your account";
                    }
                return "Username/password is incorrect!";
                }
            }
        }
         
        
        return "Username/password is incorrect!";
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));

            // pad with leading zeros to ensure 64-character hash
            while (hexString.length() < 64) {
                hexString.insert(0, '0');
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    public String sanitizeInput(String input) {
        if (input == null) return "";

        // Remove script-related characters
        input = input.replaceAll("[<>\"'%;()&]", ""); // Remove potentially dangerous characters

        return input;
    }
    
    public int getRoleFromUsername(String username){
        int role = -1;
        ArrayList<User> users = sqlite.getUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                role = user.getRole();
            }
        }
        return role;
    }
    
    public User getUserFromUsername(String username) {
        ArrayList<User> users = sqlite.getUsers(); 

        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user; 
            }
        }
    
        return null;
    }
    
    private static void startSessionChecker() {
        Timer sessionTimer = new Timer(5000, new ActionListener() { // Check every 5 seconds
            public void actionPerformed(ActionEvent e) {
                if (SessionManager.isSessionExpired()) {
                    SessionManager.logout();
                    Frame.getInstance().redirectToLogin();
                }
            }
        });
        sessionTimer.start(); // Start the timer
    }
}
