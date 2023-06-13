package com.appdeb.mybooks.adminActivities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.appdeb.mybooks.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity  {

    private ActivityPdfAddBinding binding;

    private FirebaseAuth auth;

    private ArrayList<String> categoryTitleArrayList;
    private ArrayList<String> categoryIdArrayList;

    private static final int PDF_PICK_CODE = 1000;

    private static final String TAG = "ADD_PDF_TAG";

    private Uri pdfUri = null;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        
        loadPdfCategory();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        /*********************************** Click listeners *******************************************************/
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        binding.bookCategoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();

            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
                
            }
        });
        /****************************************************************************/
    }


    /******************************* check the data and upload the data into firebase storage and details into firebase DataReference *******************************************/
    private String title="", description="";
    private void validateData() {
//        Log.d(TAG, "uploadPdfToStorage: validating data...");

        title = binding.bookTitleEt.getText().toString().trim();
        description = binding.bookDescriptionEt.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(PdfAddActivity.this, "Enter title...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(PdfAddActivity.this, "Enter description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryTitle)){
            Toast.makeText(PdfAddActivity.this,"Pick Category...",Toast.LENGTH_SHORT).show();
        }
        else if (pdfUri == null){
            Toast.makeText(PdfAddActivity.this,"Pick Pdf...",Toast.LENGTH_SHORT).show();
        }
        else{
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...");

        progressDialog.setMessage("Uploading pdf");
        progressDialog.show();

        long timeStamp = System.currentTimeMillis();

        String filePathAndName = "Books/"+timeStamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: upload pdf to storage...");
                        Log.d(TAG, "onSuccess: getting pdf url...");
                        progressDialog.setMessage("Pdf upload successfully.");

//                        Task<Uri> uriTask = taskSnapshot.getMetadata().getReference().getDownloadUrl();
//                        while(uriTask.isSuccessful()){
//                            progressDialog.setMessage("Uploaded2 to storage successfully.");
//                            String uploadedPdfUrl = ""+uriTask.getResult();
//                            uploadPdfInfoToDb(uploadedPdfUrl, timeStamp);
//                        }

                        if (taskSnapshot.getMetadata() != null) {
                            if (taskSnapshot.getMetadata().getReference() != null) {
                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        progressDialog.setMessage("Getting url of the pdf...");
                                        String uploadedPdfUrl = uri.toString();
                                        uploadPdfInfoToDb(uploadedPdfUrl, timeStamp);
                                    }
                                });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Pdf uploading fail due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Pdf uploading fail due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timeStamp) {
        Log.d(TAG, "uploadPdfInfoToDb: uploading pdf info to firebase db...");
        progressDialog.setMessage("Uploading pdf info...");

        String uid = auth.getUid();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timeStamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+selectedCategoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timeStamp",timeStamp);

        // adding the more details of book...
        hashMap.put("viewCount",0);
        hashMap.put("downloadCount",0);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(""+timeStamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.setMessage("Information upload successfully.");
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully upload to database");
                        Toast.makeText(PdfAddActivity.this, "Successfully upload to database", Toast.LENGTH_SHORT).show();
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to upload to db due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload to db due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
/*******************************************************************************************************************/


    private void loadPdfCategory() {
//        Log.d(TAG, "loadPdfCategory: Loading pdf categories...");
        categoryTitleArrayList = new ArrayList<String>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
//                    ModelCategory model = ds.getValue(ModelCategory.class);
//                    categoryTitleArrayList.add(model);

                    // get id and title of the category.
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    // add to respective arrayList..
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PdfAddActivity.this, "There is some error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String selectedCategoryTitle, selectedCategoryId;
    private void categoryPickDialog() {

        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        String[] categoriesArray =  new String[categoryTitleArrayList.size()];
        for (int i = 0; i< categoryTitleArrayList.size(); i++)
        {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        selectedCategoryTitle = categoriesArray[which];
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);

                        binding.bookCategoryTv.setText(selectedCategoryTitle);
//                       Log.d(TAG, "categoryPickDialog: Selected Category: "+category);
                    }
                }).show();
    }



/******************* Method for picking the pdf from the phone storage **************************/
    private void pdfPickIntent() {
        Log.d(TAG,"pdfPickIntent: starting pdf pick intent.");
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent,"Select Pdf"),PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == 1000)
            {
                Log.d(TAG,"onActivityResult: Pdf Picked");
                pdfUri = data.getData();
                Log.d(TAG,"onActivityResult: URI: "+pdfUri);
                Toast.makeText(PdfAddActivity.this,"Pdf is picked.",Toast.LENGTH_SHORT).show();

            }
        }
        else{
//            Log.d(TAG,"onActivityResult: Cancelled picking pdf");
            Toast.makeText(PdfAddActivity.this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }

/******************* End of Method for picking the pdf from the phone storage **************************/
}