package com.example.smartairapplication;

import com.google.firebase.auth.FirebaseUser;

public class LoginPresenter {

    private final Login view;
    private final LoginModel model;

    public LoginPresenter(Login view) {
        this.view = view;
        this.model = new LoginModel();
    }

    public LoginPresenter(Login view, LoginModel model) {
        this.view = view;
        this.model = model;
    }

    public void checkCurrentUser() {
        FirebaseUser user = model.getCurrentUser();
        if (user != null) {
            view.navigateBasedOnRole(user.getUid());
        }
    }

    public void onRegisterClicked() {
        view.navigateToRegistration();
    }

    public void login(String email, String password) {

        if (email == null || email.trim().isEmpty()) {
            view.showMessage("Please enter your email");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            view.showMessage("Please enter your password");
            return;
        }

        view.showLoading();

        model.signIn(email, password, new LoginModel.SignInCallback() {
            @Override
            public void onSuccess() {
                view.hideLoading();
                FirebaseUser user = model.getCurrentUser();
                if (user != null) {
                    view.navigateBasedOnRole(user.getUid());
                } else {
                    view.showMessage("Login succeeded, but user is unavailable.");
                }
            }

            @Override
            public void onInvalidCredentials() {
                view.hideLoading();
                view.showMessage("Invalid email or password.");
            }

            @Override
            public void onGenericFailure() {
                view.hideLoading();
                view.showMessage("Login failed. Please try again later.");
            }
        });
    }

    public void resetPassword(String email) {

        if (email == null || email.trim().isEmpty()) {
            view.showMessage("Please enter your email");
            return;
        }

        view.showLoading();

        model.sendPasswordResetEmail(email, new LoginModel.PasswordResetCallback() {
            @Override
            public void onSuccess() {
                view.hideLoading();
                view.showMessage("Password reset link sent to your email.");
            }

            @Override
            public void onFailure() {
                view.hideLoading();
                view.showMessage("Failed to send reset email.");
            }
        });
    }
}
