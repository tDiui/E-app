package com.example.appeng.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.appeng.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appeng.Model.Vocab;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private Context context;
    private List<Vocab> vocabList;

    public ReviewAdapter(Context context, List<Vocab> vocabList) {
        this.context = context;
        this.vocabList = vocabList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Vocab vocab = vocabList.get(position);
        holder.tvWord.setText(vocab.getWord());
        holder.tvMeaning.setText(vocab.getMeaning());

        String info = "Sai " + vocab.getMistakeCount() + " lần";
        if (vocab.getLastMistakeTime() != null) {
            info += ", gần nhất: " + vocab.getLastMistakeTime();
        }
        holder.tvMistakeInfo.setText(info);
    }

    @Override
    public int getItemCount() {
        return vocabList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvMeaning, tvMistakeInfo;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
            tvMeaning = itemView.findViewById(R.id.tvMeaning);
            tvMistakeInfo = itemView.findViewById(R.id.tvMistakeInfo);
        }
    }
}
