package com.example.appeng.Adapter;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.appeng.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appeng.Model.Vocab;
import com.example.appeng.WordDetailActivity;


import java.util.List;
import java.util.Locale;

public class VocabAdapter extends RecyclerView.Adapter<VocabAdapter.VocabViewHolder> {

    private final Context context;
    private List<Vocab> vocabList;
    private TextToSpeech tts;

    public VocabAdapter(Context context, List<Vocab> vocabList) {
        this.context = context;
        this.vocabList = vocabList;

        // Khởi tạo TTS
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.9f);
            }
        });
    }

    @NonNull
    @Override
    public VocabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocab, parent, false);
        return new VocabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabViewHolder holder, int position) {
        Vocab vocab = vocabList.get(position);

        holder.tvWord.setText(vocab.getWord());

        if (vocab.getPhonetic() != null && !vocab.getPhonetic().isEmpty()) {
            holder.tvPhonetic.setText(vocab.getPhonetic());
            holder.tvPhonetic.setVisibility(View.VISIBLE);
        } else {
            holder.tvPhonetic.setVisibility(View.GONE);
        }

        holder.tvMeaning.setText(vocab.getMeaning());

        holder.btnPlayAudio.setOnClickListener(v -> {
            if (tts != null) {
                tts.speak(vocab.getWord(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WordDetailActivity.class);
            intent.putExtra("word", vocab.getWord());
            intent.putExtra("phonetic", vocab.getPhonetic());
            intent.putExtra("meaning", vocab.getMeaning());
            intent.putExtra("example", vocab.getExample());
            intent.putExtra("synonym", vocab.getSynonym());
            intent.putExtra("antonym", vocab.getAntonym());
            intent.putExtra("image", vocab.getImage());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return vocabList.size();
    }

    public void releaseTTS() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    public static class VocabViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvPhonetic, tvMeaning;
        ImageButton btnPlayAudio;

        public VocabViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
            tvPhonetic = itemView.findViewById(R.id.tvPhonetic);
            tvMeaning = itemView.findViewById(R.id.tvMeaning);
            btnPlayAudio = itemView.findViewById(R.id.btnPlayAudio);
        }
    }
}
