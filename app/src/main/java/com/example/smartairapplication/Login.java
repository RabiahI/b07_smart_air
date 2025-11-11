package com.example.smartairapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    private static final String TAG = "Login";

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
            String[] roles = {"Parent", "Provider", "Child"};
            for (String role: roles) {
                DatabaseReference roleRef  = userRef.child(role).child(uid);
                roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       if (snapshot.exists()) {
                           User user = null;
                           if ("Parent".equals(role)) {
                               user = snapshot.getValue(Parent.class);
                           } else if ("Provider".equals(role)) {
                               user = snapshot.getValue(Provider.class);
                           } else if ("Child".equals(role)) {
                               user = snapshot.getValue(Child.class);
                           }

                           if (user != null) {
                               Toast.makeText(Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                               Intent intent;
                               if (user instanceof Parent) {
                                   intent = new Intent(Login.this, ParentHomeActivity.class);
                               } else if (user instanceof Provider) {
                                   intent = new Intent(Login.this, ProviderHomeActivity.class);
                               } else if (user instanceof Child) {
                                   intent = new Intent(Login.this, ChildHomeActivity.class);
                               } else {
                                   intent = new Intent(Login.this, MainActivity.class);
                               }
                               startActivity(intent);
                               finish();

                           }
                       }
                   }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Login.this, "Failed to get user data.", Toast.LENGTH_SHORT).show();
                }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.registerNow);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String uid = firebaseUser.getUid();
                                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                                        String [] roles = {"Parent", "Provider", "Child"};
                                        for (String role: roles) {
                                            DatabaseReference roleRef = userRef.child(role).child(uid);
                                            roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        User user = null;
                                                       if ("Parent".equals(role)) {
                                                           user = snapshot.getValue(Parent.class);
                                                       } else if ("Provider".equals(role)) {
                                                           user = snapshot.getValue(Provider.class);
                                                       } else if ("Child".equals(role)) {
                                                           user = snapshot.getValue(Child.class);
                                                       }

                                                       if (user != null) {
                                                            Toast.makeText(Login.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                                            Intent intent;
                                                            if (user instanceof Parent){
                                                                intent = new Intent(Login.this, ParentHomeActivity.class);
                                                            } else if (user instanceof Provider){
                                                                intent = new Intent(Login.this, ProviderHomeActivity.class);
                                                            } else {
                                                                intent = new Intent(Login.this, ChildHomeActivity.class);
                                                            }
    
                                                            startActivity(intent);
                                                            finish();
                                                       }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Toast.makeText(Login.this, "Failed to get user data.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthInvalidUserException e) {
                                        Toast.makeText(Login.this, "User does not exist.", Toast.LENGTH_LONG).show();
                                    } catch (FirebaseAuthInvalidCredentialsException e) {
                                        Toast.makeText(Login.this, "Wrong password.", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Log.e(TAG, e.getMessage());
                                        Toast.makeText(Login.this, "Authentication failed: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
            }
        });
    }
}
