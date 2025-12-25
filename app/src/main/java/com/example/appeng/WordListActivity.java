package com.example.appeng;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;

import com.example.appeng.Adapter.VocabAdapter;
import com.example.appeng.Model.Vocab;
import com.example.appeng.Model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * WordListActivity: Hiển thị danh sách từ vựng theo chủ đề đã chọn.
 * Cung cấp các tính năng: Xem danh sách, mở Flashcard và Luyện nghe.
 */
public class WordListActivity extends AppCompatActivity {

    // Khai báo các thành phần UI và dữ liệu
    private RecyclerView recyclerView;   // Danh sách hiển thị từ vựng
    private VocabAdapter vocabAdapter;   // Bộ nạp dữ liệu (Adapter) cho danh sách
    private List<Vocab> vocabList;       // Danh sách chứa các đối tượng từ vựng
    private DatabaseHelper dbHelper;     // Công cụ quản lý SQLite
    private User user;                   // Thông tin người dùng hiện tại

    private int categoryId = -1;         // ID của chủ đề được truyền từ MainActivity
    private String categoryName = "";    // Tên của chủ đề

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list); // Gắn giao diện XML

        // --- 1. Khởi tạo và thiết lập RecyclerView ---
        recyclerView = findViewById(R.id.recyclerViewVocab);
        // Thiết lập hiển thị theo dạng danh sách dọc (Linear)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vocabList = new ArrayList<>();
        dbHelper = new DatabaseHelper(this);

        // Lấy thông tin người dùng từ Session
        user = SessionManager.getUser(this);

        // --- 2. Nhận dữ liệu truyền vào từ Intent ---
        Intent intent = getIntent();
        categoryId = intent.getIntExtra("categoryId", -1); // Nhận ID chủ đề
        categoryName = intent.getStringExtra("categoryName"); // Nhận tên chủ đề

        // Nếu nhận được ID hợp lệ, tiến hành tải từ vựng từ Database
        if (categoryId != -1) {
            loadVocabByCategory(categoryId);
        }

        // Khởi tạo Adapter và gắn vào RecyclerView
        vocabAdapter = new VocabAdapter(this, vocabList);
        recyclerView.setAdapter(vocabAdapter);

        // --- 3. Thiết lập các nút chức năng học tập ---

        // Nút Flashcard: Chuyển sang chế độ học bằng thẻ ghi nhớ
        Button btnFlashCard = findViewById(R.id.btnFlashCard);
        btnFlashCard.setOnClickListener(v -> {
            Intent flash = new Intent(this, FlashCardActivity.class);
            flash.putExtra("categoryId", categoryId); // Truyền ID chủ đề đi cùng
            flash.putExtra("categoryName", categoryName);
            startActivity(flash);
        });

        // Nút Listening: Chuyển sang màn hình luyện nghe
        Button btnListening = findViewById(R.id.btnListening);
        btnListening.setOnClickListener(v -> {
            Intent listen = new Intent(this, ListeningActivity.class);
            listen.putExtra("categoryId", categoryId);
            listen.putExtra("categoryName", categoryName);
            startActivity(listen);
        });
    }

    /**
     * Tải danh sách từ vựng từ SQLite dựa trên ID danh mục.
     */
    private void loadVocabByCategory(int categoryId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            dbHelper.createDatabase(); // Đảm bảo database đã sẵn sàng
            db = dbHelper.openDatabase();

            // Truy vấn lấy tất cả từ vựng có categoryId tương ứng
            cursor = db.rawQuery(
                    "SELECT * FROM vocab WHERE categoryId = ?",
                    new String[]{String.valueOf(categoryId)}
            );

            if (cursor.moveToFirst()) {
                do {
                    // Đọc dữ liệu từ các cột
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String word = cursor.getString(cursor.getColumnIndexOrThrow("word"));
                    String meaning = cursor.getString(cursor.getColumnIndexOrThrow("meaning"));

                    // Lấy dữ liệu an toàn (tránh lỗi nếu cột không tồn tại hoặc null)
                    String phonetic = getSafe(cursor, "phonetic");
                    String example = getSafe(cursor, "example");
                    String synonym = getSafe(cursor, "synonym");
                    String antonym = getSafe(cursor, "antonym");
                    String image = getSafe(cursor, "image");

                    // Tạo đối tượng Vocab mới và thêm vào danh sách
                    Vocab vocab = new Vocab(id, word, meaning, phonetic, example, synonym, antonym, image);
                    vocabList.add(vocab);

                } while (cursor.moveToNext());
            }

        } catch (IOException e) {
            Log.e("WordListActivity", "Lỗi sao chép DB", e);
        } catch (Exception e) {
            Log.e("WordListActivity", "Lỗi tải từ vựng", e);
        } finally {
            // Đóng cursor và database để giải phóng tài nguyên
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Hàm hỗ trợ lấy dữ liệu từ Cursor một cách an toàn.
     * Trả về chuỗi rỗng nếu cột không tồn tại.
     */
    private String getSafe(Cursor cursor, String col) {
        int i = cursor.getColumnIndex(col);
        return i == -1 ? "" : cursor.getString(i);
    }

    /**
     * Giải phóng bộ nhớ của Text-To-Speech (nếu có dùng trong Adapter) khi Activity bị đóng.
     */
    @Override
    protected void onDestroy() {
        if (vocabAdapter != null) {
            vocabAdapter.releaseTTS();
        }
        super.onDestroy();
    }
}