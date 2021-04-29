package com.example.blogactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private Button register;
    private Button login;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        register=findViewById(R.id.Register);
        login=findViewById(R.id.Login);
        mAuth=FirebaseAuth.getInstance();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginActivity;
                loginActivity = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(loginActivity);

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=email.getText().toString();
                String entry=password.getText().toString();
                if(TextUtils.isEmpty(name)) {
                    email.setError("Email is required");
                }
                    else if(TextUtils.isEmpty(entry)) {
                        password.setError("Password is required");
                    }
                    else
                    {
                        mAuth.createUserWithEmailAndPassword(name, entry).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    sendtomain();
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,  errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }



        });


    }
    private void sendtomain() {
        Intent mainintent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainintent);
        finish();
    }
}