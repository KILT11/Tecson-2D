/*
 * Place this file in: trying > Source Packages > config > SessionManager.java
 */
package config;

/**
 * SessionManager - Handles user session state across the application.
 * Singleton class: only one instance exists throughout the app.
 */
public class SessionManager {

    // Singleton instance
    private static SessionManager instance;

    private SessionManager() {
        clearSession();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Session data
    private int     userId;
    private String  userName;
    private String  userEmail;
    private String  userType;
    private String  userStatus;
    private boolean loggedIn;

    /**
     * Call this right after a successful login.
     */
    public void createSession(int userId, String userName,
                              String userEmail, String userType,
                              String userStatus) {
        this.userId     = userId;
        this.userName   = userName;
        this.userEmail  = userEmail;
        this.userType   = userType;
        this.userStatus = userStatus;
        this.loggedIn   = true;
        System.out.println("[Session] Created for: " + userName + " | ID: " + userId);
    }

    /**
     * Call this on logout to wipe all session data.
     */
    public void clearSession() {
        this.userId     = 0;
        this.userName   = null;
        this.userEmail  = null;
        this.userType   = null;
        this.userStatus = null;
        this.loggedIn   = false;
        System.out.println("[Session] Cleared.");
    }

    // Getters
    public boolean isLoggedIn()    { return loggedIn;   }
    public int     getUserId()     { return userId;     }
    public String  getUserName()   { return userName;   }
    public String  getUserEmail()  { return userEmail;  }
    public String  getUserType()   { return userType;   }
    public String  getUserStatus() { return userStatus; }
}