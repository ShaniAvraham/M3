package com.example.demo.appdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Firebase authentication related imports


public class MainActivity extends AppCompatActivity {

    private static final String TAG = ".MainActivity";

    private FirebaseAuth mAuth;

    // UI components
    private EditText emailField, passwordField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI components
        Button loginButton = findViewById(R.id.login_btn);
        Button signupButton = findViewById(R.id.signup_btn);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);

        // firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // login button onClick
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String pass = passwordField.getText().toString();
                if (!email.equals("") && !pass.equals("")) {
                    signIn(email, pass);
                } else {
                    toastMessage("Please fill in all the fields.");
                }
            }
        });

        // sign up button on click
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }


    /**
     * signIn function receives an email address and a password and authenticates the user
     * if sign in succeeded, it start HomeActivity
     * else, it displays a message
     *
     * @param email    the typed email
     * @param password the typed password
     */
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in succeeded, start HomeActivity
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in failed, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            toastMessage(task.getException().getMessage());
                        }
                    }
                });
    }


    /**
     * toastMessage function receives a message and displays
     *
     * @param message the message to show
     */
    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}