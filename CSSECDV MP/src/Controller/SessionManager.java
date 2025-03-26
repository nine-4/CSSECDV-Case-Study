/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;
import Model.User;
/**
 *
 * @author Martin
 */
public class SessionManager {
     private static User currentUser; // Stores the logged-in user

    // Set the current logged-in user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Get the logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Get the username of the logged-in user
    public static String getCurrentUsername() {
        return (currentUser != null) ? currentUser.getUsername() : null;
    }

    // Get the role of the logged-in user
    public static int getCurrentUserRole() {
        return (currentUser != null) ? currentUser.getRole() : -1; // Return -1 if no user is logged in
    }

    // Logout the user (clear session)
    public static void logout() {
        currentUser = null;
    }
}
    

