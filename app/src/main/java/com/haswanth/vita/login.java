package com.haswanth.vita;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login extends AppCompatActivity implements View.OnClickListener {
    private TextView register;
    private EditText editemail,editpassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private Button signin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(this);

        signin = (Button) findViewById(R.id.loginbutton);
        signin.setOnClickListener(this);

        editemail = (EditText) findViewById(R.id.lemail);
        editpassword = (EditText) findViewById(R.id.lpassword);

        progressBar = (ProgressBar) findViewById(R.id.lprogressbar);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v){

        if (v.getId() == R.id.register) {
            startActivity(new Intent(this, signup.class));
        } else if (v.getId() == R.id.loginbutton) {
            userlogin();
        }

    }

    private void userlogin() {
        String email = editemail.getText().toString().trim();
        String password = editpassword.getText().toString().trim();

        if (email.isEmpty()) {
            editemail.setError("Email is required");
            editemail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editemail.setError("Enter a valid email");
            editemail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            editpassword.setError("Password is required");
            editpassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editpassword.setError("Minimum length of password should be 6");
            editpassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        signin.setVisibility(View.GONE);

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user.isEmailVerified()) {
                        Toast.makeText(login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(login.this, MainActivity.class));
                        finish();
                    } else {
                        user.sendEmailVerification();
                        Toast.makeText(login.this, "Please verify your email, If there is no Email then check your spam folder", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        signin.setVisibility(View.VISIBLE);
                    }
                }else{
                    Toast.makeText(login.this,"Failed to Login", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}