package com.example.demo.appdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button loginButton = findViewById(R.id.login_btn);
        loginButton.setOnClickListener(new View.OnClickListener() {

            // login button onClick
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExploreActivity.class);

                // ToDo: verify Login username and password
                /*
                // message to next activity
                EditText editText = (EditText) findViewById(R.id.editText);
                String message = editText.getText().toString();
                intent.putExtra(EXTRA_MESSAGE, message);*/

                startActivity(intent);

            }
        });


        Button signupButton = findViewById(R.id.signup_btn);
        signupButton.setOnClickListener(new View.OnClickListener() {

            // login button onClick
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);

            }
        });
    }
}
