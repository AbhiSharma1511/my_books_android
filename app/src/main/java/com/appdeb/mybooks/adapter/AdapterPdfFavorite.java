package com.appdeb.mybooks.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appdeb.mybooks.MyApplication;
import com.appdeb.mybooks.commonActivities.PdfDetailActivity;
import com.appdeb.mybooks.databinding.RowPdfFavoriteBinding;
import com.appdeb.mybooks.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFavorite extends RecyclerView.Adapter<AdapterPdfFavorite.viewHolder> {

    private Context context;

    private ArrayList<ModelPdf> pdfArrayList;

    private RowPdfFavoriteBinding binding;

    public AdapterPdfFavorite(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context),parent,false);

        return new viewHolder(binding.getRoot());
    }


    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        ModelPdf model = pdfArrayList.get(position);
        loadBookDetails(model, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",model.getId());
                context.startActivity(intent);
            }
        });

        holder.btnRemoveFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context,model.getId());
            }
        });

    }


    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }


    class viewHolder extends RecyclerView.ViewHolder{

        TextView titleTv, descriptionTv, categoryTv, dateTv, sizeTv;
        PDFView pdfView;
        ProgressBar progressBar;
        ImageButton btnRemoveFav;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.tvTitle;
            descriptionTv = binding.tvDescription;
            categoryTv = binding.tvCategory;
            sizeTv = binding.tvSize;
            dateTv = binding.tvDate;
            progressBar = binding.progressBar;
            pdfView = binding.pdfView;
            btnRemoveFav = binding.btnRemoveFav;

        }
    }

    private void loadBookDetails(@NonNull ModelPdf model, viewHolder holder) {

        String bookId = model.getId();
//
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Books");
        databaseReference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
//                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
//                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
//                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
//
                        model.setFavorite(true);
                        model.setTitle(title);
                        model.setDescription(description);
                        model.setCategoryId(title);
                        model.setUrl(bookUrl);
                        model.setUid(uid);
//                        model.setTimestamp(Long.parseLong(timestamp));
//                        model.setTimestamp(timestamp);
//
//
//                        String date = MyApplication.getDate(Long.parseLong(timestamp));

                        holder.titleTv.setText(title);
                        holder.descriptionTv.setText(description);
//                        holder.dateTv.setText(date);
                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+title,
                                holder.pdfView,
                                holder.progressBar,
                                null
                        );

                        MyApplication.loadCategory(
                                ""+categoryId,
                                holder.categoryTv
                        );

                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+title,
                                holder.sizeTv
                        );
                    }
//
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}
