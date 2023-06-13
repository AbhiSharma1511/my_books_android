package com.appdeb.mybooks.filters;

import android.widget.Filter;

import com.appdeb.mybooks.adapter.AdapterPdfUser;
import com.appdeb.mybooks.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {
    ArrayList<ModelPdf> filterList;
    AdapterPdfUser adapterPdfUser;

    public FilterPdfUser(ArrayList<ModelPdf> filterList, AdapterPdfUser adapterPdfUser) {
        this.filterList = filterList;
        this.adapterPdfUser = adapterPdfUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length()>0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModel = new ArrayList<>();
            for (int i =0;i<filterList.size();i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredModel.add(filterList.get(i));
                }
            }
            results.count = filteredModel.size();
            results.values = filteredModel;
        }
        else{
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapterPdfUser.pdfArrayList = (ArrayList<ModelPdf>)results.values;

        adapterPdfUser.notifyDataSetChanged();
    }
}
