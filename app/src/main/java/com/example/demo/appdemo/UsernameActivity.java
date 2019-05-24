package com.example.demo.appdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class UsernameActivity extends AppCompatActivity {

    private static final String TAG = ".UsernameActivity";

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    // UI components
    private EditText usernameField;
    private Button doneButton;

    private String username;

    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        doneButton = findViewById(R.id.done_button);

        usernameField = findViewById(R.id.username);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // done button onClick
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the typed username

                username = usernameField.getText().toString();
                if (!username.equals(""))
                {
                    CollectionReference usersRef = db.collection("users");

                    // check if the username already exists
                    Query users = usersRef.whereEqualTo("username", username);
                    users.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty())
                                {
                                    updateUsername(username);
                                }
                                else
                                {
                                    toastMessage("This username is taken");
                                }
                            }
                        }
                    });
                }

                else
                    // if the username didn't choose a username, the username is the email address
                    continueToHomeActivity();
            }
        });

    }

    /**
     * updateUsername update the user's username according to his choice
     *
     * @param username the chosen username
     */
    void updateUsername(String username) {
        DocumentReference userRef = db.collection("users").document(user.getUid());
        userRef.update("username", username);
        continueToHomeActivity();
    }

    /**
     * continueToHomeActivity transfers the user to the home page
     */
    void continueToHomeActivity()
    {
        Intent intent = new Intent(UsernameActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
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
