package com.appdeb.mybooks.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.appdeb.mybooks.commonActivities.PdfDetailActivity;
import com.appdeb.mybooks.adminActivities.PdfEditActivity;
import com.appdeb.mybooks.MyApplication;
import com.appdeb.mybooks.databinding.RowPdfAdminBinding;
import com.appdeb.mybooks.filters.FilterPdfAdmin;
import com.appdeb.mybooks.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.viewHolder> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    private static final String TAG = "PDF_ADAPTER_TAG";

    private ProgressDialog progressDialog;

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;


        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new viewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        ModelPdf model = pdfArrayList.get(position);

        String title = model.getTitle();
        String description = model.getDescription();
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        long timestamp = model.getTimestamp();
        String formatDate= MyApplication.getDate(timestamp);
        String pdfUrl = model.getUrl();

        holder.tvTitle.setText(title);
        holder.tvDescription.setText(description);
        holder.tvDate.setText(formatDate);

        MyApplication.loadCategory(
                ""+categoryId,
                holder.tvCategory);
        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null);
        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.tvSize);

        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "Button More is clicked..", Toast.LENGTH_SHORT).show();
                moreOptionDialog(model, holder);
//                Toast.makeText(context, "Button222 More is clicked..", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });


    }

    private void moreOptionDialog(@NonNull ModelPdf model, viewHolder holder) {

        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        String[] option = {"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);

                        }else if(which==1){
                            MyApplication.deleteBook(
                                    context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle);
//                            deleteBook(model, holder);
                        }
                    }
                }).show();
    }

//    private void deleteBook(ModelPdf model, viewHolder holder) {
//        String bookId = model.getId();
//        String bookUrl = model.getUrl();
//        String bookTitle = model.getTitle();
//
//        progressDialog.setMessage("Deleting "+bookTitle+" ...");
//        progressDialog.show();
//
//        StorageReference storageReference =FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
//        storageReference.delete()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
////                        Toast.makeText(context,"Book delete Successfully",Toast.LENGTH_SHORT).show();
//
//                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
//                        reference.child(bookId)
//                                .removeValue()
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void unused) {
//                                        progressDialog.dismiss();
//                                        Toast.makeText(context,"Book delete Successfully",Toast.LENGTH_SHORT).show();
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        progressDialog.dismiss();
//                                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                progressDialog.dismiss();
//                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void loadPdfSize(ModelPdf model, viewHolder holder) {
//
//        String pdfUrl = model.getUrl();
//        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
//        storageReference.getMetadata()
//                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
//                    @Override
//                    public void onSuccess(StorageMetadata storageMetadata) {
//                        double bytes = storageMetadata.getSizeBytes();
//
//                        double kb= bytes/1024;
//                        double mb = kb/1024;
//
//                        if (mb >= 1){
//                            holder.tvSize.setText(String.format("%.2f",mb) + " MB");
//                        }else if (kb >= 1){
//                            holder.tvSize.setText(String.format("%.2f",kb) + " KB");
//                        }else {
//                            holder.tvSize.setText(String.format("%.2f",bytes) + " bytes");
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

//    private void loadPdfFromUrlSinglePage(ModelPdf model, viewHolder holder) {
//
//        String pdfUrl = model.getUrl();
//        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
//        storageReference.getBytes(MAX_BYTES_PDF)
//                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(byte[] bytes) {
//
//                        holder.pdfView.fromBytes(bytes)
//                                .pages(0)
//                                .spacing(0)
//                                .swipeHorizontal(false)
//                                .enableSwipe(false)
//                                .onError(new OnErrorListener() {
//                                    @Override
//                                    public void onError(Throwable t) {
//                                        Log.d(TAG, "onError: "+t.getMessage());
//                                        holder.progressBar.setVisibility(View.INVISIBLE);
//                                    }
//                                })
//                                .onPageError(new OnPageErrorListener() {
//                                    @Override
//                                    public void onPageError(int page, Throwable t) {
//                                        holder.progressBar.setVisibility(View.INVISIBLE);
//                                        Log.d(TAG, "onPageError: "+t.getMessage());
//                                    }
//                                })
//                                .onLoad(new OnLoadCompleteListener() {
//                                    @Override
//                                    public void loadComplete(int nbPages) {
//                                        holder.progressBar.setVisibility(View.INVISIBLE);
//                                    }
//                                })
//                                .load();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        holder.progressBar.setVisibility(View.INVISIBLE);
//                    }
//                });
//
//    }

//    private void loadCategory(ModelPdf model, viewHolder holder) {
//
//        String categoryId = model.getCategoryId();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Categories");
//
//        databaseReference.child(categoryId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        String category = ""+snapshot.child("category").getValue();
//
//                        holder.tvCategory.setText(category);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterPdfAdmin(filterList,this);
        }
        return filter;
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        PDFView pdfView;
        ProgressBar progressBar;
        TextView tvTitle,tvDescription, tvCategory, tvSize, tvDate;
        ImageButton btnMore;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            tvTitle = binding.tvTitle;
            tvDescription = binding.tvDescription;
            tvSize = binding.tvSize;
            tvDate = binding.tvDate;
            tvCategory = binding.tvCategory;
            btnMore = binding.btnMore;
        }
    }
}
