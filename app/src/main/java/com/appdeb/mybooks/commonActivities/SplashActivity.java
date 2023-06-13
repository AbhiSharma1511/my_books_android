package com.appdeb.mybooks.commonActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.appdeb.mybooks.R;
import com.appdeb.mybooks.adminActivities.DashboardAdmin;
import com.appdeb.mybooks.userActivities.DashboardUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();
            }
        },2000);
    }

    private void checkUser() {
        FirebaseUser firebaseUser  = auth.getCurrentUser();
        if (firebaseUser == null)
        {
            Intent intent = new Intent(SplashActivity.this, BasicActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userType = ""+snapshot.child("userType").getValue();
                            if (userType.equals("user")){
                                startActivity(new Intent(SplashActivity.this, DashboardUser.class));
                                finish();
                            }
                            else if (userType.equals("admin")){
                                startActivity(new Intent(SplashActivity.this, DashboardAdmin.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}