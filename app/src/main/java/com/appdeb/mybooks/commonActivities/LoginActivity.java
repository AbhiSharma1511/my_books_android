package com.appdeb.mybooks.commonActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.appdeb.mybooks.adminActivities.DashboardAdmin;
import com.appdeb.mybooks.databinding.ActivityLoginBinding;
import com.appdeb.mybooks.userActivities.DashboardUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        binding.btnloginLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = binding.emailLogin.getText().toString();
                String password = binding.passwordLogin.getText().toString();
                if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                    Toast.makeText(LoginActivity.this, "Invalid email pattern..!", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this, "Enter password..", Toast.LENGTH_SHORT).show();
                }
                else {
                    signInWithEmailAndPassword(userEmail, password);
                }

            }
        });

        binding.forgotLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));

            }
        });

        binding.newUserLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void signInWithEmailAndPassword(String userEmail, String password) {
        progressDialog.setMessage("Logging In...");
        progressDialog.show();
//        auth.signInWithEmailAndPassword(userEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()){
//                    progressDialog.dismiss();
//                    startActivity(new Intent(LoginActivity.this,DashboardUser.class));
//                    finish();
//                }else{
//                    Toast.makeText(LoginActivity.this, "Invalid Email or Password..!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        auth.signInWithEmailAndPassword(userEmail, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
//                        progressDialog.dismiss();
//                        startActivity(new Intent(LoginActivity.this,DashboardUser.class));
//                        finish();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking User...");

        FirebaseUser firebaseUser  = auth.getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        String userType = ""+snapshot.child("userType").getValue();
                        if (userType.equals("user")){
                            startActivity(new Intent(LoginActivity.this, DashboardUser.class));
                            finish();
                        }
                        else if (userType.equals("admin")){
                            startActivity(new Intent(LoginActivity.this, DashboardAdmin.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}