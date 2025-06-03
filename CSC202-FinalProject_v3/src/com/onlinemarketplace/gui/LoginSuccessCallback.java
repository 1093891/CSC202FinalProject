package com.onlinemarketplace.gui;

import com.onlinemarketplace.model.User;

/**
 * Custom callback interface for successful user login.
 * Replaces java.util.function.Consumer<User>.
 */
public interface LoginSuccessCallback {
    void onLoginSuccess(User user);
}
