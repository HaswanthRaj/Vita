package com.haswanth.vita;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class loginsignup extends AppCompatActivity {

    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginsignup);

        // Initialize buttons
        loginButton = findViewById(R.id.loginbut);
        registerButton = findViewById(R.id.registerbutton);

        // Set click listener for Login Button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Login Activity
                Intent intent = new Intent(loginsignup.this, login.class);
                startActivity(intent);
            }
        });

        // Set click listener for Register Button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Register Activity
                Intent intent = new Intent(loginsignup.this, signup.class);
                startActivity(intent);
            }
        });
    }
}
