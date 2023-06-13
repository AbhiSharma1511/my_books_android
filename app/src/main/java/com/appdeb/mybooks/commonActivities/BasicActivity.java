package com.appdeb.mybooks.commonActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.appdeb.mybooks.databinding.ActivityBasicBinding;
import com.appdeb.mybooks.userActivities.DashboardUser;

public class BasicActivity extends AppCompatActivity {

    ActivityBasicBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBasicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BasicActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        binding.btnskip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BasicActivity.this, DashboardUser.class);
                startActivity(intent);
                finish();
            }
        });

    }
}