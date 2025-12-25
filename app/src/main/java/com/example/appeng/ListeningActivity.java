package com.example.appeng;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech; // Thư viện để chuyển văn bản thành giọng nói
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appeng.Model.User;
import com.example.appeng.Model.Vocab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * ListeningActivity: Quản lý tính năng luyện nghe và điền từ.
 */
public class ListeningActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện
    private Button btnPlay, btnCheck, btnNext, btnBack;
    private EditText edtAnswer;
    private TextView tvResult;

    // Khai báo đối tượng TTS, Database và danh sách dữ liệu
    private TextToSpeech tts;
    private DatabaseHelper dbHelper;
    private List<Vocab> vocabList;
    private int currentIndex = 0; // Vị trí từ hiện tại trong danh sách

    private User user; // Đối tượng user đang đăng nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening);

        // --- 1. Kiểm tra phiên đăng nhập ---
        user = SessionManager.getUser(this);
        if (user == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để luyện nghe!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --- 2. Ánh xạ các View từ Layout XML ---
        btnPlay = findViewById(R.id.btnPlay);
        btnCheck = findViewById(R.id.btnCheck);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        edtAnswer = findViewById(R.id.edtAnswer);
        tvResult = findViewById(R.id.tvResult);

        // --- 3. Khởi tạo Database và danh sách ---
        dbHelper = new DatabaseHelper(this);
        vocabList = new ArrayList<>();

        // Lấy categoryId từ Intent (được truyền từ màn hình chọn chủ đề)
        int categoryId = getIntent().getIntExtra("categoryId", -1);
        if (categoryId != -1) loadVocabByCategory(categoryId);

        // Kiểm tra nếu không có dữ liệu thì vô hiệu hóa các nút
        if (vocabList.isEmpty()) {
            tvResult.setText("Không có dữ liệu để luyện nghe");
            btnPlay.setEnabled(false);
            btnCheck.setEnabled(false);
            btnNext.setEnabled(false);
        }

        // --- 4. Khởi tạo công cụ TextToSpeech (TTS) ---
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Thiết lập ngôn ngữ là Tiếng Anh (Mỹ)
                tts.setLanguage(Locale.US);
                // Thiết lập tốc độ đọc (0.9f là chậm hơn bình thường một chút để dễ nghe)
                tts.setSpeechRate(0.9f);
            }
        });

        // --- 5. Thiết lập sự kiện Click cho các nút ---
        btnPlay.setOnClickListener(v -> playAudio());    // Phát âm thanh
        btnCheck.setOnClickListener(v -> checkAnswer()); // Kiểm tra đáp án
        btnNext.setOnClickListener(v -> goToNextWord()); // Sang từ tiếp theo
        btnBack.setOnClickListener(v -> finish());       // Quay lại
    }

    /**
     * Tải danh sách từ vựng từ SQLite dựa theo ID chủ đề.
     */
    private void loadVocabByCategory(int categoryId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            dbHelper.createDatabase(); // Đảm bảo file DB đã tồn tại trong hệ thống
            db = dbHelper.openDatabase();

            cursor = db.rawQuery("SELECT * FROM vocab WHERE categoryId = ?",
                    new String[]{String.valueOf(categoryId)});

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String word = cursor.getString(cursor.getColumnIndexOrThrow("word"));
                    String meaning = cursor.getString(cursor.getColumnIndexOrThrow("meaning"));
                    String image = cursor.getString(cursor.getColumnIndexOrThrow("image"));

                    vocabList.add(new Vocab(id, word, meaning, "", "", "", "", image));

                } while (cursor.moveToNext());
            }

            // Xáo trộn danh sách từ để mỗi lần học lại sẽ có thứ tự khác nhau
            Collections.shuffle(vocabList);

        } catch (IOException e) {
            Log.e("ListeningActivity", "Lỗi copy db", e);
        } catch (Exception e) {
            Log.e("ListeningActivity", "Lỗi tải vocab", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Thực hiện phát âm từ hiện tại qua loa/tai nghe.
     */
    private void playAudio() {
        if (!vocabList.isEmpty() && currentIndex < vocabList.size()) {
            String word = vocabList.get(currentIndex).getWord();
            if (tts != null) {
                // QUEUE_FLUSH: Hủy các từ đang đọc dở (nếu có) để đọc ngay từ mới
                tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }

    /**
     * So sánh câu trả lời của người dùng với đáp án đúng.
     */
    private void checkAnswer() {
        if (vocabList.isEmpty() || currentIndex >= vocabList.size()) return;

        // Chuẩn hóa văn bản: xóa khoảng trắng dư và chuyển về chữ thường
        String userAnswer = edtAnswer.getText().toString().trim().toLowerCase();
        String correctWord = vocabList.get(currentIndex).getWord().toLowerCase();

        if (userAnswer.equals(correctWord)) {
            tvResult.setText("Đúng rồi!");
            tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvResult.setText("Sai! Đáp án đúng: " + vocabList.get(currentIndex).getWord());
            tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            // Nếu sai, lưu từ này vào bảng 'review' (lịch sử lỗi) để nhắc nhở học lại
            int vocabId = vocabList.get(currentIndex).getId();
            dbHelper.addMistake("vocab", vocabId, user.getId());
        }
    }

    /**
     * Chuyển sang từ vựng tiếp theo trong danh sách.
     */
    private void goToNextWord() {
        currentIndex++;

        if (currentIndex < vocabList.size()) {
            edtAnswer.setText(""); // Xóa trắng ô nhập liệu
            tvResult.setText("");  // Xóa kết quả thông báo cũ
            playAudio();           // Tự động phát âm từ tiếp theo
        } else {
            // Khi đã hết danh sách từ
            tvResult.setText("🎉 Bạn đã hoàn thành chủ đề này!");
            btnPlay.setEnabled(false);
            btnCheck.setEnabled(false);
            btnNext.setEnabled(false);
        }
    }

    /**
     * Giải phóng bộ nhớ của TTS khi thoát ứng dụng.
     */
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown(); // Cực kỳ quan trọng để tránh rò rỉ bộ nhớ
        }
        super.onDestroy();
    }
}