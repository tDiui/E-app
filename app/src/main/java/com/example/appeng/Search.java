package com.example.appeng;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appeng.Adapter.VocabAdapter;
import com.example.appeng.Model.Vocab;
import com.example.appeng.Model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Search Activity: Quản lý chức năng tìm kiếm từ vựng.
 * Cho phép tìm kiếm theo từ (tiếng Anh) hoặc nghĩa (tiếng Việt).
 */
public class Search extends AppCompatActivity {

    private EditText edtSearch;       // Ô nhập từ khóa tìm kiếm
    private RecyclerView recyclerView; // Hiển thị danh sách kết quả
    private VocabAdapter vocabAdapter; // Bộ nạp dữ liệu cho danh sách từ vựng
    private List<Vocab> vocabList;     // Danh sách chứa kết quả tìm kiếm
    private DatabaseHelper dbHelper;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // --- 1. Ánh xạ và Thiết lập giao diện ---
        edtSearch = findViewById(R.id.edtSearch);
        recyclerView = findViewById(R.id.recyclerViewSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vocabList = new ArrayList<>();
        vocabAdapter = new VocabAdapter(this, vocabList);
        recyclerView.setAdapter(vocabAdapter);

        dbHelper = new DatabaseHelper(this);

        // --- 2. Kiểm tra phiên đăng nhập ---
        user = SessionManager.getUser(this);
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // --- 3. Thiết lập thanh điều hướng Bottom Navigation ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_search); // Đánh dấu icon Search đang chọn

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_review) {
                startActivity(new Intent(this, Review.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                return true;
            }
            return false;
        });

        // --- 4. Lắng nghe thay đổi nội dung ô tìm kiếm (Real-time Search) ---
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Mỗi khi người dùng gõ phím, hàm tìm kiếm sẽ được gọi ngay lập tức
                searchWord(s.toString().trim());
            }
        });
    }

    /**
     * Logic tìm kiếm từ vựng trong Database
     */
    private void searchWord(String keyword) {
        vocabList.clear(); // Xóa kết quả cũ trước khi tìm mới

        // Nếu ô tìm kiếm trống thì không làm gì cả
        if (keyword.isEmpty()) {
            vocabAdapter.notifyDataSetChanged();
            return;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            dbHelper.createDatabase();
            db = dbHelper.openDatabase();

            // Chuyển từ khóa về chữ thường và loại bỏ dấu tiếng Việt để tìm kiếm chính xác hơn
            String normalizedKeyword = removeVietnameseAccents(keyword.toLowerCase());

            // Lấy toàn bộ từ vựng để lọc trong code (hoặc có thể lọc bằng SQL LIKE)
            cursor = db.rawQuery("SELECT * FROM vocab", null);

            if (cursor.moveToFirst()) {
                do {
                    // Lấy dữ liệu từ các cột trong Database
                    int id          = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String word     = cursor.getString(cursor.getColumnIndexOrThrow("word"));
                    String meaning  = cursor.getString(cursor.getColumnIndexOrThrow("meaning"));
                    String phonetic = cursor.getString(cursor.getColumnIndexOrThrow("phonetic"));
                    String example  = cursor.getString(cursor.getColumnIndexOrThrow("example"));
                    String synonym  = cursor.getString(cursor.getColumnIndexOrThrow("synonym"));
                    String antonym  = cursor.getString(cursor.getColumnIndexOrThrow("antonym"));
                    String image    = cursor.getString(cursor.getColumnIndexOrThrow("image"));

                    // Chuẩn hóa từ và nghĩa trong DB để so sánh không dấu
                    String normalizedWord = removeVietnameseAccents(word.toLowerCase());
                    String normalizedMeaning = removeVietnameseAccents(meaning.toLowerCase());

                    // Nếu từ hoặc nghĩa có chứa từ khóa tìm kiếm
                    if (normalizedWord.contains(normalizedKeyword)
                            || normalizedMeaning.contains(normalizedKeyword)) {

                        vocabList.add(new Vocab(
                                id, word, meaning, phonetic,
                                example, synonym, antonym, image
                        ));
                    }

                } while (cursor.moveToNext());
            }

            // Cập nhật giao diện danh sách
            vocabAdapter.notifyDataSetChanged();

        } catch (IOException e) {
            Log.e("Search", "Lỗi sao chép DB", e);
        } catch (Exception e) {
            Log.e("Search", "Lỗi khi tìm kiếm", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    /**
     * Hàm hỗ trợ: Loại bỏ dấu tiếng Việt (ví dụ: "Học" -> "hoc")
     * Giúp người dùng tìm kiếm linh hoạt hơn ngay cả khi không gõ dấu.
     */
    private String removeVietnameseAccents(String str) {
        str = str.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        str = str.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        str = str.replaceAll("[ìíịỉĩ]", "i");
        str = str.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        str = str.replaceAll("[ùúụủũưừứựửữ]", "u");
        str = str.replaceAll("[ỳýỵỷỹ]", "y");
        str = str.replaceAll("đ", "d");
        return str;
    }
}