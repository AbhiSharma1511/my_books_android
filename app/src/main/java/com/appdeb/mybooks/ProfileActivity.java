package com.appdeb.mybooks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.appdeb.mybooks.adapter.AdapterPdfFavorite;
import com.appdeb.mybooks.databinding.ActivityProfileBinding;
import com.appdeb.mybooks.models.ModelPdf;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private FirebaseAuth auth;

    private ArrayList<ModelPdf> pdfArrayList;

    private AdapterPdfFavorite adapterPdfFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        loadUserInfo();

        loadFavoriteBooks();


        binding.imgBtnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,ProfileEditActivity.class));
            }
        });
        binding.btnBack3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadFavoriteBooks() {

        pdfArrayList = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(auth.getUid()).child("Favorites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String bookId = ""+ds.child("bookId").getValue();

                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            pdfArrayList.add(modelPdf);
                        }

                        binding.tvFavoriteBookCount.setText(""+pdfArrayList.size());

                        adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this,pdfArrayList);

                        Toast.makeText(ProfileActivity.this, "Error can be after this!", Toast.LENGTH_SHORT).show();

                        binding.rvBook.setAdapter(adapterPdfFavorite);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public void loadUserInfo(){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("userType").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();


                        String formattedDate = MyApplication.getDate(Long.parseLong(timestamp));

                        binding.tvName.setText(name);
                        binding.tvEmail.setText(email);
                        binding.tvMemberDate.setText(formattedDate);
                        binding.tvAccountType.setText(userType);


                        Glide.with(ProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_24)
                                .into(binding.profileIv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}