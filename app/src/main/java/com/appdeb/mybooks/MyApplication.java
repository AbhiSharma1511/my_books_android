package com.appdeb.mybooks;

import static com.appdeb.mybooks.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.appdeb.mybooks.adapter.AdapterPdfAdmin;
import com.appdeb.mybooks.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.local.IidStore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String getDate(long timestamp) {
        DateFormat obj = new SimpleDateFormat("dd MMM yyyy ");
        Date res = new Date(timestamp);
        String date = obj.format(res);
        return  date;
    }
//    public static final String getDate(long timestamp) {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
//        String dateString = simpleDateFormat.format(timestamp);
//        String date = String.format("Date: %s", dateString);
//
//        return date;
//    }

    public static void deleteBook(Context context, String bookId, String bookUrl, String bookTitle) {

        ProgressDialog progressDialog = new ProgressDialog(context);

        String TAG ="DELETE_BOOK_TAG";

        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Deleting "+bookTitle+" ...");
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
//                        Toast.makeText(context,"Book delete Successfully",Toast.LENGTH_SHORT).show();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context,"Book delete Successfully",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void loadPdfSize(String pdfUrl, String pdfTitle, TextView tvSize) {

        String TAG ="PDF_SIZE_TAG";

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();

                        double kb= bytes/1024;
                        double mb = kb/1024;

                        if (mb >= 1){
                            tvSize.setText(String.format("%.2f",mb) + " MB");
                        }else if (kb >= 1){
                            tvSize.setText(String.format("%.2f",kb) + " KB");
                        }else {
                            tvSize.setText(String.format("%.2f",bytes) + " bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(MyApplication.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void loadPdfFromUrlSinglePage(String pdfUrl, String pdfTitle , PDFView pdfView, ProgressBar progressBar, TextView pageTv) {
        String TAG = "PDF_URL_TAG";

        ProgressBar progressbar;

//        String pdfUrl = model.getUrl();
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.d(TAG, "onError: "+t.getMessage());
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);

                                        if (pageTv != null){
                                            pageTv.setText(""+nbPages);
                                        }
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }

    public static void  loadCategory(String categoryId, TextView tvCategory) {

//        String categoryId = model.getCategoryId();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Categories");

        databaseReference.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();

                        tvCategory.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void incrementBookViewCount(String bookId){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewCount = ""+snapshot.child("viewCount").getValue();

                        if (viewCount.equals("") || viewCount.equals("null")){
                            viewCount="0";
                        }
                        long newViewCount = Long.parseLong(viewCount)+1;

                        HashMap<String, Object> hashMap= new HashMap<>();
                        hashMap.put("viewCount",newViewCount);

                        DatabaseReference databaseReference1 =FirebaseDatabase.getInstance().getReference("Books");
                        databaseReference1.child(bookId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void downloadBook(Context context, String bookId, String bookTitle, String bookUrl) {

        String nameWithExtension  = bookTitle+".pdf";

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Downloading "+nameWithExtension+"...");
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

        StorageReference storageReference1 = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference1.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        saveDownloadedBook(context, progressDialog, bytes, nameWithExtension, bookId);
//                        Toast.makeText(context, "Here is some error", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context,"Failed to download due to "+e.getMessage(),Toast.LENGTH_SHORT);
                    }
                });
    }

    private static void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {

        try{
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdir();

            String filePath = downloadsFolder.getPath()+"/"+nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);

            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
            incrementBookDownloadCount(bookId);
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Failed saving to Download Folder due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void incrementBookDownloadCount(String bookId) {

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference1.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();

                        if (downloadsCount.equals("")|| downloadsCount.equals("null")){
                            downloadsCount ="0";

                        }
                        long newDownloadsCount = Long.parseLong(downloadsCount )+1;

                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount", newDownloadsCount);

                        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("Books");
                        databaseReference2.child(bookId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

//    public static void loadPdfPageCount(Context context, String pdfUrl, TextView pageTv){
//
//        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(""+pdfUrl);
//        storageReference.getBytes(Constants.MAX_BYTES_PDF)
//                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(byte[] bytes) {
//
//                        PDFView pdfView = new PDFView(context,  null);
//                        pdfView.fromBytes(bytes)
//                                .onLoad(new OnLoadCompleteListener() {
//                                    @Override
//                                    public void loadComplete(int nbPages) {
//                                        pageTv.setText(""+nbPages);
//                                    }
//                                });
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                });
//    }

    public  static  void addToFavorite(Context context, String bookId){

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()==null){
            Toast.makeText(context, "You're not logged in.", Toast.LENGTH_SHORT).show();
        }
        else{
            long timestamp = System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bookId",bookId);
            hashMap.put("timestamp", timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Added to your favorite list...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failure to add to favorite due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }

    public static void removeFromFavorite(Context context, String bookId){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser()==null){
            Toast.makeText(context, "You're not logged in.", Toast.LENGTH_SHORT).show();
        }
        else{

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favorites").child(bookId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Removed from your favorite list...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failure to remove from favorite due to"+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }


}
