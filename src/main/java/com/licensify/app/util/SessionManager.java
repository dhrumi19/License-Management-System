package com.licensify.app.util;

import com.licensify.app.model.License;
import com.licensify.app.model.User;

/**
 * Handles global session state for the logged-in user and their current license.
 */
public class SessionManager {

    private static User currentUser;
    private static License activeLicense;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static License getActiveLicense() {
        return activeLicense;
    }

    public static void setActiveLicense(License license) {
        activeLicense = license;
    }

    public static void cleanSession() {
        currentUser = null;
        activeLicense = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
