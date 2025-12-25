package com.example.appeng;

import android.os.Bundle;
import android.speech.tts.TextToSpeech; // Thư viện chuyển văn bản thành giọng nói
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * WordDetailActivity: Màn hình hiển thị chi tiết từ vựng (nghĩa, ví dụ, từ đồng nghĩa, trái nghĩa).
 */
public class WordDetailActivity extends AppCompatActivity {

    // Khai báo các thành phần hiển thị văn bản
    private TextView tvWord, tvPhonetic, tvMeaning, tvExample, tvSynonym, tvAntonym;
    private ImageButton btnAudio; // Nút bấm để nghe phát âm
    private TextToSpeech tts;      // Đối tượng xử lý giọng nói
    private String word;           // Biến lưu trữ từ đang hiển thị để phát âm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail); // Kết nối với giao diện XML

        // --- 1. Ánh xạ các View từ Layout XML ---
        tvWord = findViewById(R.id.tvWord);
        tvPhonetic = findViewById(R.id.tvPhonetic);
        tvMeaning = findViewById(R.id.tvMeaning);
        tvExample = findViewById(R.id.tvExample);
        tvSynonym = findViewById(R.id.tvSynonym);
        tvAntonym = findViewById(R.id.tvAntonym);
        btnAudio = findViewById(R.id.btnAudio);

        // --- 2. Nhận dữ liệu truyền đến từ Intent ---
        // Sử dụng hàm getStringExtraSafe tự định nghĩa bên dưới để tránh lỗi Null
        word     = getStringExtraSafe("word");
        String phonetic = getStringExtraSafe("phonetic");
        String meaning  = getStringExtraSafe("meaning");
        String example  = getStringExtraSafe("example");
        String synonym  = getStringExtraSafe("synonym");
        String antonym  = getStringExtraSafe("antonym");

        // --- 3. Hiển thị dữ liệu lên giao diện ---
        tvWord.setText(word);
        tvPhonetic.setText(phonetic);
        tvMeaning.setText(meaning);

        // Kiểm tra nếu có dữ liệu (không trống) thì mới hiển thị, kèm theo dấu đầu dòng
        tvExample.setText(!example.isEmpty() ? "• Example: " + example : "");
        tvSynonym.setText(!synonym.isEmpty() ? "• Synonym: " + synonym : "");
        tvAntonym.setText(!antonym.isEmpty() ? "• Antonym: " + antonym : "");

        // --- 4. Khởi tạo tính năng phát âm (TTS) ---
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US); // Thiết lập ngôn ngữ Tiếng Anh (Mỹ)
                tts.setSpeechRate(0.9f);    // Giảm tốc độ đọc xuống 0.9 để nghe rõ hơn
            }
        });

        // Sự kiện khi nhấn vào nút loa
        btnAudio.setOnClickListener(v -> {
            if (tts != null && !word.isEmpty()) {
                // QUEUE_FLUSH: Hủy âm thanh đang phát (nếu có) để phát ngay từ này
                tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    /**
     * Hàm hỗ trợ lấy dữ liệu từ Intent một cách an toàn.
     * Trả về chuỗi rỗng "" thay vì giá trị null nếu không tìm thấy key.
     */
    private String getStringExtraSafe(String key) {
        String s = getIntent().getStringExtra(key);
        return s != null ? s : "";
    }

    /**
     * Giải phóng tài nguyên TTS khi Activity bị đóng để tránh rò rỉ bộ nhớ (Memory Leak).
     */
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();     // Dừng phát âm
            tts.shutdown(); // Tắt engine TTS
        }
        super.onDestroy();
    }
}