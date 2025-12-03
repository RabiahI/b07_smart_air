package com.example.smartairapplication;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.auth.FirebaseUser;


@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest {

    @Mock
    Login view; // handles UI elements

    @Mock
    LoginModel model; // handles firebase auth stuff

    @Mock
    FirebaseUser firebaseUser;

    @Captor
    ArgumentCaptor<LoginModel.SignInCallback> signInCallbackCaptor;
    @Captor
    ArgumentCaptor<LoginModel.PasswordResetCallback> resetCallbackCaptor;

    LoginPresenter presenter;

    @Before
    public void setup() {
        presenter = new LoginPresenter(view, model); // inject mocks
    }


    // checkCurrentUser
    @Test
    public void checkCurrentUser_noUser_doesNothing() {
        when(model.getCurrentUser()).thenReturn(null);

        presenter.checkCurrentUser();

        verify(view, never()).navigateBasedOnRole(anyString());
    }

    @Test
    public void checkCurrentUser_userExists() {
        when(model.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("123");

        presenter.checkCurrentUser();

        verify(view).navigateBasedOnRole("123");
    }


    // onRegisterClicked
    @Test
    public void onRegisterClicked_navigatesToRegistration() {
        presenter.onRegisterClicked();

        verify(view).navigateToRegistration();
    }

    // login
    @Test
    public void login_emptyEmail_showsMessage() {
        presenter.login("", "123456");

        verify(view).showMessage("Please enter your email");
        verify(model, never()).signIn(anyString(), anyString(), any(LoginModel.SignInCallback.class));
    }

    @Test
    public void login_emptyPassword_showsMessage() {
        presenter.login("exampleemail@gmail.com", "");

        verify(view).showMessage("Please enter your password");
        verify(model, never()).signIn(anyString(), anyString(), any(LoginModel.SignInCallback.class));
    }



    // resetPassword
    @Test
    public void resetPassword_emptyEmail_showsMessage() {
        presenter.resetPassword("");
        verify(view).showMessage("Please enter your email");
    }

    @Test
    public void resetPassword_sendSuccess_showsSuccessMessage() {
        presenter.resetPassword("exampleemail@gmail.com");

        verify(view).showLoading();
        verify(model).sendPasswordResetEmail(anyString(), resetCallbackCaptor.capture());

        LoginModel.PasswordResetCallback callback = resetCallbackCaptor.getValue();
        callback.onSuccess();

        verify(view).hideLoading();
        verify(view).showMessage("Password reset link sent to your email.");
    }

    @Test
    public void resetPassword_sendFailure_showsFailureMessage() {
        presenter.resetPassword("exampleemail@gmail.com");
        verify(view).showLoading();
        verify(model).sendPasswordResetEmail(anyString(), resetCallbackCaptor.capture());

        LoginModel.PasswordResetCallback callback = resetCallbackCaptor.getValue();
        callback.onFailure();

        verify(view).hideLoading();
        verify(view).showMessage("Failed to send reset email.");
    }
}