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
import static org.mockito.ArgumentMatchers.eq;

import com.google.firebase.auth.FirebaseUser;

@RunWith(MockitoJUnitRunner.class)
public class ChildLoginPresenterTest {
    @Mock
    ChildLoginActivity view; // handles UI elements

    @Mock
    ChildLoginModel model; // handles firebase auth stuff

    @Mock
    FirebaseUser firebaseUser;

    @Captor
    ArgumentCaptor<ChildLoginModel.SignInCallback> callbackCaptor;

    ChildLoginPresenter presenter;

    @Before
    public void setup() {
        presenter = new ChildLoginPresenter(view, "parent123", model);
    }

    // check current user
    @Test
    public void checkCurrentUser_nullUser_doesNothing() {
        when(model.getCurrentUser()).thenReturn(null);

        presenter.checkCurrentUser();

        verify(view, never()).showLoading();
        verify(model, never()).verifyChild(anyString(), anyString(), any());
    }

    //login validation
    @Test
    public void login_emptyUsername_showsMessage() {
        presenter.login("", "pass");

        verify(view).showMessage("Enter username");
        verify(model, never()).signInAndVerifyChild(anyString(), anyString(), anyString(), any());
    }

    @Test
    public void login_emptyPassword_showsMessage() {
        presenter.login("child", "");

        verify(view).showMessage("Enter password");
        verify(model, never()).signInAndVerifyChild(anyString(), anyString(), anyString(), any());
    }

    //login - successful sign in
    @Test
    public void login_success_flow() {
        presenter.login("child", "pass");

        verify(view).showLoading();

        verify(model).signInAndVerifyChild(anyString(), anyString(), eq("parent123"), callbackCaptor.capture());

        ChildLoginModel.SignInCallback callback = callbackCaptor.getValue();
        callback.onSuccess();

        verify(view).hideLoading();
        verify(view).showMessage("Login Successful.");
        verify(view).navigateToChildHome("parent123");
    }

    //login - invalid credentials
    @Test
    public void login_invalidCredentials_flow() {
        presenter.login("child", "pass");

        verify(view).showLoading();
        verify(model).signInAndVerifyChild(anyString(), anyString(), eq("parent123"), callbackCaptor.capture());

        ChildLoginModel.SignInCallback callback = callbackCaptor.getValue();
        callback.onInvalidCredentials();

        verify(view).hideLoading();
        verify(view).showMessage("Invalid username or password.");
        verify(view).signOut();
    }

    //login - verification failure
    @Test
    public void login_verificationFailure_flow() {
        presenter.login("child", "pass");

        verify(view).showLoading();
        verify(model).signInAndVerifyChild(anyString(), anyString(), eq("parent123"), callbackCaptor.capture());

        ChildLoginModel.SignInCallback callback = callbackCaptor.getValue();
        callback.onVerificationFailure();

        verify(view).hideLoading();
        verify(view).showMessage("Login failed: You are not registered under this parent.");
        verify(view).signOut();
    }

    //login - generic failure
    @Test
    public void login_genericFailure_flow() {
        presenter.login("child", "pass");

        verify(view).showLoading();
        verify(model).signInAndVerifyChild(anyString(), anyString(), eq("parent123"), callbackCaptor.capture());

        ChildLoginModel.SignInCallback callback = callbackCaptor.getValue();
        callback.onGenericFailure();

        verify(view).hideLoading();
        verify(view).showMessage("Authentication failed.");
        verify(view).signOut();
    }

    @Test
    public void checkCurrentUser_existingUser_successFlow() {
        when(model.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("child123");

        presenter.checkCurrentUser();

        // Capture callback passed to verifyChild()
        ArgumentCaptor<ChildLoginModel.SignInCallback> captor =
                ArgumentCaptor.forClass(ChildLoginModel.SignInCallback.class);

        verify(model).verifyChild(eq("parent123"), eq("child123"), captor.capture());
        verify(view).showLoading();

        // Trigger callback
        captor.getValue().onSuccess();

        verify(view).hideLoading();
        verify(view).showMessage("Welcome back!");
        verify(view).navigateToChildHome("parent123");
    }

    @Test
    public void checkCurrentUser_existingUser_verificationFailure() {
        when(model.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("child123");

        presenter.checkCurrentUser();

        ArgumentCaptor<ChildLoginModel.SignInCallback> captor =
                ArgumentCaptor.forClass(ChildLoginModel.SignInCallback.class);

        verify(model).verifyChild(eq("parent123"), eq("child123"), captor.capture());
        verify(view).showLoading();

        captor.getValue().onVerificationFailure();

        verify(view).hideLoading();
        verify(view).signOut();
    }

    @Test
    public void checkCurrentUser_existingUser_genericFailure() {
        when(model.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("child123");

        presenter.checkCurrentUser();

        ArgumentCaptor<ChildLoginModel.SignInCallback> captor =
                ArgumentCaptor.forClass(ChildLoginModel.SignInCallback.class);

        verify(model).verifyChild(eq("parent123"), eq("child123"), captor.capture());
        verify(view).showLoading();

        captor.getValue().onGenericFailure();

        verify(view).hideLoading();
        verify(view).signOut();
    }

    @Test
    public void checkCurrentUser_existingUser_invalidCredentials() {
        when(model.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("child123");

        presenter.checkCurrentUser();

        ArgumentCaptor<ChildLoginModel.SignInCallback> captor =
                ArgumentCaptor.forClass(ChildLoginModel.SignInCallback.class);

        verify(model).verifyChild(eq("parent123"), eq("child123"), captor.capture());
        verify(view).showLoading();

        // trigger the empty callback
        captor.getValue().onInvalidCredentials();

        // EXPECTED: nothing happens except loading should NOT be hidden
        // (presenter doesn't call anything for invalid credentials)
        verify(view, never()).hideLoading();
        verify(view, never()).signOut();
        verify(view, never()).navigateToChildHome(anyString());
    }


}
