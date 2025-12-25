package com.example.appeng.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appeng.Model.Exercise;
import com.example.appeng.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ViewHolder> {

    private Context context;
    private List<Exercise> exerciseList;

    private boolean reviewMode = false;
    private HashMap<Integer, String> reviewAnswers;

    private HashMap<Integer, String> userAnswers = new HashMap<>();

    private HashMap<Integer, List<String>> randomizedOptions = new HashMap<>();

    public ExerciseAdapter(Context context, List<Exercise> exerciseList) {
        this.context = context;
        this.exerciseList = exerciseList;
    }

    public HashMap<Integer, String> getUserAnswers() {
        return userAnswers;
    }

    public void setReviewMode(boolean enable, HashMap<Integer, String> answers) {
        this.reviewMode = enable;
        this.reviewAnswers = answers;
    }

    @NonNull
    @Override
    public ExerciseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseAdapter.ViewHolder holder, int position) {
        Exercise ex = exerciseList.get(position);

        holder.tvQuestion.setText((position + 1) + ". " + ex.getQuestion());

        if (!randomizedOptions.containsKey(ex.getId())) {
            List<String> options = new ArrayList<>();
            options.add("A. " + ex.getOptionA());
            options.add("B. " + ex.getOptionB());
            options.add("C. " + ex.getOptionC());
            options.add("D. " + ex.getOptionD());
            Collections.shuffle(options);
            randomizedOptions.put(ex.getId(), options);
        }

        List<String> options = randomizedOptions.get(ex.getId());

        holder.rbA.setText(options.get(0));
        holder.rbB.setText(options.get(1));
        holder.rbC.setText(options.get(2));
        holder.rbD.setText(options.get(3));

        resetColor(holder);

        if (reviewMode) {
            applyReviewMode(holder, ex, options);
            return;
        }

        holder.radioGroup.setOnCheckedChangeListener(null);
        holder.radioGroup.clearCheck();

        if (userAnswers.containsKey(ex.getId())) {
            String saved = userAnswers.get(ex.getId());
            checkSavedAnswer(holder, saved);
        }

        holder.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = group.findViewById(checkedId);
            if (rb != null) {
                String answer = rb.getText().toString().substring(3).trim();
                userAnswers.put(ex.getId(), answer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    private void resetColor(ViewHolder h) {
        h.rbA.setBackgroundColor(Color.TRANSPARENT);
        h.rbB.setBackgroundColor(Color.TRANSPARENT);
        h.rbC.setBackgroundColor(Color.TRANSPARENT);
        h.rbD.setBackgroundColor(Color.TRANSPARENT);
    }

    private void applyReviewMode(ViewHolder holder, Exercise ex, List<String> options) {

        String userAns = reviewAnswers.get(ex.getId());
        String correct = ex.getCorrectAnswer();

        holder.rbA.setEnabled(false);
        holder.rbB.setEnabled(false);
        holder.rbC.setEnabled(false);
        holder.rbD.setEnabled(false);

        RadioButton[] rbs = {holder.rbA, holder.rbB, holder.rbC, holder.rbD};

        for (RadioButton rb : rbs) {
            String text = rb.getText().toString().substring(3).trim();

            if (text.equalsIgnoreCase(correct)) {
                rb.setBackgroundColor(Color.parseColor("#D4EFDF"));
            }

            if (userAns != null && text.equalsIgnoreCase(userAns) && !userAns.equals(correct)) {
                rb.setBackgroundColor(Color.parseColor("#F5B7B1"));
            }
        }

        if (userAns != null) {
            for (RadioButton rb : rbs) {
                if (rb.getText().toString().contains(userAns)) {
                    rb.setChecked(true);
                }
            }
        }
    }

    private void checkSavedAnswer(ViewHolder h, String savedAns) {
        if (h.rbA.getText().toString().contains(savedAns)) h.rbA.setChecked(true);
        if (h.rbB.getText().toString().contains(savedAns)) h.rbB.setChecked(true);
        if (h.rbC.getText().toString().contains(savedAns)) h.rbC.setChecked(true);
        if (h.rbD.getText().toString().contains(savedAns)) h.rbD.setChecked(true);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvQuestion;
        RadioGroup radioGroup;
        RadioButton rbA, rbB, rbC, rbD;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            rbA = itemView.findViewById(R.id.rbA);
            rbB = itemView.findViewById(R.id.rbB);
            rbC = itemView.findViewById(R.id.rbC);
            rbD = itemView.findViewById(R.id.rbD);
        }
    }
}
