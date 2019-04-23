package com.example.demo.appdemo;

import android.app.Application;

public class CurrentUser extends Application {

    static User currentUser;
    static String userId;

    /**
     * setCurrentUser method sets the received user to
     *
     * @param user (String) the userId to set
     */
    static void setCurrentUser(User user)
    {
        currentUser = user;
    }

    static User getCurrentUser() {
        return currentUser;
    }

    public static void setUserId(String userId) {
        CurrentUser.userId = userId;
    }

    public static String getUserId() {
        return userId;
    }
}
