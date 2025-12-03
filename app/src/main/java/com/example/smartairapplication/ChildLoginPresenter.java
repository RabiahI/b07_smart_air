package com.example.smartairapplication;

import com.google.firebase.auth.FirebaseUser;

public class ChildLoginPresenter {

    private final ChildLoginActivity view;
    private final ChildLoginModel model;
    private String parentId;

    public ChildLoginPresenter(ChildLoginActivity view, String parentId) {
        this.view = view;
        this.model = new ChildLoginModel();
        this.parentId = parentId;
    }

    // constructor for testing
    public ChildLoginPresenter(ChildLoginActivity view, String parentId, ChildLoginModel model) {
        this.view = view;
        this.model = model;
        this.parentId = parentId;
    }


    public void checkCurrentUser() {
        FirebaseUser currentUser = model.getCurrentUser();
        if (currentUser != null) {
            view.showLoading();
            model.verifyChild(parentId, currentUser.getUid(), new ChildLoginModel.SignInCallback() {
                @Override
                public void onSuccess() {
                    view.hideLoading();
                    view.showMessage("Welcome back!");
                    view.navigateToChildHome(parentId);
                }

                @Override
                public void onInvalidCredentials() {
                }

                @Override
                public void onVerificationFailure() {
                    view.hideLoading();
                    view.signOut();
                }

                @Override
                public void onGenericFailure() {
                    view.hideLoading();
                    view.signOut();
                }
            });
        }
    }

    public void login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            view.showMessage("Enter username");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            view.showMessage("Enter password");
            return;
        }

        view.showLoading();
        String childEmail = username + "@smartair.ca";

        model.signInAndVerifyChild(childEmail, password, parentId, new ChildLoginModel.SignInCallback() {
            @Override
            public void onSuccess() {
                view.hideLoading();
                view.showMessage("Login Successful.");
                view.navigateToChildHome(parentId);
            }

            @Override
            public void onInvalidCredentials() {
                view.hideLoading();
                view.showMessage("Invalid username or password.");
                view.signOut();
            }

            @Override
            public void onVerificationFailure() {
                view.hideLoading();
                view.showMessage("Login failed: You are not registered under this parent.");
                view.signOut();
            }

            @Override
            public void onGenericFailure() {
                view.hideLoading();
                view.showMessage("Authentication failed.");
                view.signOut();
            }
        });
    }
}
