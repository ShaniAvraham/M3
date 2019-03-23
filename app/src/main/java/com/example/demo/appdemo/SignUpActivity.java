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
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = ".SignUpActivity";

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    // UI components
    private EditText emailField, passwordField;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signupButton = findViewById(R.id.signup_btn);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();


        // sign up page button onClick
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String pass = passwordField.getText().toString();
                if (!email.equals("") && !pass.equals("")) {
                    signUp(email, pass);
                } else {
                    toastMessage("Please fill in all the fields.");
                }
            }
        });
    }

    /**
     * signIn function receives an email address and a password and signs up the user
     * if sign up succeeded, it start HomeActivity
     * else, it displays a message
     *
     * @param email    the typed email
     * @param password the typed password
     */
    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null)
                                addNewUser(user);
                            Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            toastMessage("Authentication failed.");
                            //TODO: add message about fail
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * addNewUser funcion receives a user and adds it to the database
     *
     * @param newUser (FirebaseUser) the user to add
     */
    private void addNewUser(FirebaseUser newUser) {
        User user = new User(0, null);
        Log.w(TAG, "!@!id " + newUser.getUid());
        db.collection("users").document(newUser.getUid()).set(user);
    }
}
