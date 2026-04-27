package dev.eduplay.core;

import dev.eduplay.entities.User;

public class SessionManager {

    private static SessionManager instance;
    private User currentUser;
    private String sessionToken;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(User user, String token) {
        this.currentUser = user;
        this.sessionToken = token;
    }

    public void logout() {
        this.currentUser = null;
        this.sessionToken = null;
    }

    public User getCurrentUser() { return currentUser; }
    public String getSessionToken() { return sessionToken; }
    public boolean isLoggedIn() { return currentUser != null; }
}