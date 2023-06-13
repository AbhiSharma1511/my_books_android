package com.appdeb.mybooks.adapter;

import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import com.appdeb.mybooks.MyApplication;
import com.appdeb.mybooks.commonActivities.PdfDetailActivity;
import com.appdeb.mybooks.databinding.RowPdfFavoriteBinding;
import com.appdeb.mybooks.databinding.RowPdfUserBinding;
import com.appdeb.mybooks.filters.FilterPdfUser;
import com.appdeb.mybooks.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterPdfUser extends RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList, filterList;

    private RowPdfUserBinding binding;

    private FilterPdfUser filter;

    public AdapterPdfUser(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderPdfUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {

        ModelPdf model = pdfArrayList.get(position);

        String bookId = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        String categoryId = model.getCategoryId();
        long timestamp = model.getTimestamp();

        String date = MyApplication.getDate(timestamp);

        // now set data..
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(date);

        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar,
                null
        );

        MyApplication.loadCategory(
                ""+categoryId,
                binding.tvCategory
        );

        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId",bookId);
                context.startActivity(intent);
            }
        });




    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter = new FilterPdfUser(filterList,this);
        }
        return filter;
    }


    public class HolderPdfUser extends RecyclerView.ViewHolder{

        TextView titleTv, descriptionTv, categoryTv, dateTv, sizeTv;
        PDFView pdfView;
        ProgressBar progressBar;


        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

            titleTv = binding.tvTitle;
            descriptionTv = binding.tvDescription;
            categoryTv = binding.tvCategory;
            sizeTv = binding.tvSize;
            dateTv = binding.tvDate;
            progressBar = binding.progressBar;
            pdfView = binding.pdfView;
        }
    }
}
