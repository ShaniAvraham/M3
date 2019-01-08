package com.example.demo.appdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Button signupButton = findViewById(R.id.signup_btn);
        signupButton.setOnClickListener(new View.OnClickListener() {


            // sign up page button onClick
            @Override
            public void onClick(View v) {
                // Sign Up button on click - verify username is not taken and password is valid and if ok, intent explore activity

                // ToDo: verify Sign Up username is not taken and password is valid
                /*
                 // message to next activity
                    EditText editText = (EditText) findViewById(R.id.editText);
                    String message = editText.getText().toString();
                    intent.putExtra(EXTRA_MESSAGE, message);*/

                Intent intent_explore = new Intent(SignUpActivity.this, HomeActivity.class);
                startActivity(intent_explore);
            }
        });
    }
}
