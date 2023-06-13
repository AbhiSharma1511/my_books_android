package com.appdeb.mybooks.filters;

import android.widget.Filter;

import com.appdeb.mybooks.adapter.AdapterCategory;
import com.appdeb.mybooks.adapter.AdapterPdfAdmin;
import com.appdeb.mybooks.models.ModelCategory;
import com.appdeb.mybooks.models.ModelPdf;

import java.util.ArrayList;
//import android.widget.Filter;

public class FilterPdfAdmin extends Filter {

    ArrayList<ModelPdf> filterList;
    AdapterPdfAdmin adapterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length()>0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredCategory = new ArrayList<>();
            for (int i =0;i<filterList.size();i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredCategory.add(filterList.get(i));
                }
            }
            results.count = filteredCategory.size();
            results.values = filteredCategory;
        }
        else{
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)results.values;

        adapterPdfAdmin.notifyDataSetChanged();

    }

}
