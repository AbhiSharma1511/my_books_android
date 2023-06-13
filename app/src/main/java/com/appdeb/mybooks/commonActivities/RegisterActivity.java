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

import com.appdeb.mybooks.databinding.ActivityRegisterBinding;
import com.appdeb.mybooks.userActivities.DashboardUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    ActivityRegisterBinding binding;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    private String userName="", userEmail="", password="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validationData();
            }
        });

    }

    private void validationData()
    {
        userName = binding.nameRegister.getText().toString().trim();
        userEmail = binding.emailRegister.getText().toString().trim();
        password = binding.passwordRegister.getText().toString().trim();
        String confPassword = binding.confirmPasswordRegister.getText().toString().trim();

        if(TextUtils.isEmpty(userName)){
            Toast.makeText(RegisterActivity.this, "Enter your name..!", Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            Toast.makeText(RegisterActivity.this, "Invalid email pattern..!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this, "Enter password..", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(confPassword)){
            Toast.makeText(RegisterActivity.this, "Confirm password..", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(confPassword)){
            Toast.makeText(RegisterActivity.this, "Password doesn't match..!", Toast.LENGTH_SHORT).show();
        }
        else{
            signInWithEmailAndPassword(userName,userEmail,password);
        }
    }


    private  void signInWithEmailAndPassword(String userName, String userEmail, String password)
    {
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        auth.createUserWithEmailAndPassword(userEmail, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
//                    progressDialog.dismiss();
                    updateUserInfo();
                }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info...");

        long timeStamp = System.currentTimeMillis();

        String uId = auth.getUid();

        //setup data to add db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",uId);
        hashMap.put("email",userEmail);
        hashMap.put("name",userName);
        hashMap.put("profileImage","");
        hashMap.put("userType","user");
        hashMap.put("timestamp",timeStamp);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(uId)
                .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,"Account created..",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, DashboardUser.class));
                    finish();
                }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });


    }
}