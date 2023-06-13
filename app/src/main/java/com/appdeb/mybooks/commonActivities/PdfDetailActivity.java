package com.appdeb.mybooks.commonActivities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.appdeb.mybooks.MyApplication;
import com.appdeb.mybooks.R;
import com.appdeb.mybooks.adapter.AdapterPdfFavorite;
import com.appdeb.mybooks.adminActivities.PdfViewActivity;
import com.appdeb.mybooks.databinding.ActivityPdfDetailBinding;
import com.appdeb.mybooks.models.ModelPdf;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PdfDetailActivity extends AppCompatActivity {

    ActivityPdfDetailBinding binding;

    String bookId, bookTitle, bookUrl ;

    boolean isInMyFavorite = false;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        binding.btnDownloadBook.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavorite();
        }

        loadBookDetails();

        MyApplication.incrementBookViewCount(bookId);


        binding.btnBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.btnReadBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });

        binding.btnDownloadBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//                    Toast.makeText(PdfDetailActivity.this, "Here is some error2", Toast.LENGTH_SHORT).show();
                    if (firebaseAuth.getCurrentUser()==null){
                        Toast.makeText(PdfDetailActivity.this, "You're not logged in.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        MyApplication.downloadBook(PdfDetailActivity.this, "" + bookId, "" + bookTitle, "" + bookUrl);
                    }

                }
                else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }

            }
        });
        
        binding.btnRemFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser()==null){
                    Toast.makeText(PdfDetailActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(isInMyFavorite){
                        MyApplication.removeFromFavorite(PdfDetailActivity.this,bookId);
                    }
                    else {
                        MyApplication.addToFavorite(PdfDetailActivity.this,bookId);
                    }
                }
            }
        });
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),isGranted ->{
                if (isGranted){
                    MyApplication.downloadBook(this,""+bookId, ""+bookTitle,""+bookUrl);
                }
                else {
                    Toast.makeText(this, "Permission was denied", Toast.LENGTH_SHORT).show();
                }
                    });

    private void loadBookDetails() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                         bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewCount = ""+snapshot.child("viewCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                         bookUrl = ""+snapshot.child("url").getValue();
//                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        binding.btnDownloadBook.setVisibility(View.VISIBLE);

//                        String date = MyApplication.getDate(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId, binding.categoryTv
                        );

                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.pdfView1,
                                binding.progressBar,
                                binding.pageTv
                        );

                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.sizeTv
                        );

//                        MyApplication.loadPdfPageCount(
//                                PdfDetailActivity.this,
//                                ""+bookUrl,
//                                binding.pageTv
//                        );

                        binding.titleTv.setText(""+bookTitle);
                        binding.descriptionTv.setText(""+description);
                        binding.viewTv.setText(viewCount.replace("null","N/A"));
                        binding.downloadTv.setText(downloadsCount.replace("null","N/A"));
//                        binding.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite(){

        if (firebaseAuth.getCurrentUser()==null){
            Toast.makeText(PdfDetailActivity.this, "You're not logged in.", Toast.LENGTH_SHORT).show();
        }
        else {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            isInMyFavorite = snapshot.exists();
                            if (isInMyFavorite){
                                binding.btnRemFav.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white_24,0,0);
                                binding.btnRemFav.setText("Remove Favorite");
                            }
                            else{
                                binding.btnRemFav.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white,0,0);
                                binding.btnRemFav.setText("Add Favorite");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }
    }


}