package com.appdeb.mybooks.adminActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.appdeb.mybooks.ProfileActivity;
import com.appdeb.mybooks.commonActivities.BasicActivity;
import com.appdeb.mybooks.adapter.AdapterCategory;
import com.appdeb.mybooks.databinding.ActivityDashboardAdminBinding;
import com.appdeb.mybooks.models.ModelCategory;
import com.appdeb.mybooks.userActivities.DashboardUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdmin extends AppCompatActivity {

    private ActivityDashboardAdminBinding binding;
    private FirebaseAuth auth;

    private ArrayList<ModelCategory> categoryArrayList;
    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        checkUser();
        loadCategory();

        /*********************************** Click Listener on button ***************************************/
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterCategory.getFilter().filter(s);
                }catch (Exception e){
//                    Toast.makeText(, "", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.logoutAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                checkUser();
            }
        });

        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdmin.this, CategoryAddActivity.class));
            }
        });

        binding.addPdfFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdmin.this, PdfAddActivity.class));
            }
        });

        binding.btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdmin.this, ProfileActivity.class));
            }
        });

    }
    /***********************************************************************************/

    private void checkUser() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser==null)
        {
            startActivity(new Intent(DashboardAdmin.this, BasicActivity.class));
            finish();
        }
        else{
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);
        }
    }

    private void loadCategory() {
        categoryArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                for (DataSnapshot snapshot2 : snapshot.getChildren()){
                    ModelCategory model = snapshot2.getValue(ModelCategory.class);

                    categoryArrayList.add(model);
                }
                // setup adapter
                adapterCategory = new AdapterCategory(DashboardAdmin.this, categoryArrayList);

                // set adapter to recyclerview
                binding.categoriesRv.setAdapter(adapterCategory);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.my_menu,menu);
//        MenuItem item = menu.findItem(R.id.searchMenu);
//         SearchView searchView = (SearchView) item.getActionView();
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//        return super.onCreateOptionsMenu(menu);
//    }
}