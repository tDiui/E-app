package com.example.appeng.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.appeng.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appeng.Model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Category category);
    }

    private Context context;
    private List<Category> categoryList;
    private final OnItemClickListener listener;

    public CategoryAdapter(Context context, List<Category> categoryList, OnItemClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());

        int resId = context.getResources().getIdentifier(
                category.getIcon(), "drawable", context.getPackageName());
        if (resId != 0) {
            holder.ivCategoryIcon.setImageResource(resId);
        } else {
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_category);
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(category);
            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView ivCategoryIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
        }
    }
}