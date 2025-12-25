package com.example.appeng;

// Các thư viện hỗ trợ hiệu ứng (Animation)
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

// Glide: Thư viện tải và xử lý hình ảnh mạnh mẽ
import com.bumptech.glide.Glide;
import com.example.appeng.Model.Vocab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlashCardActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện
    private View cardFront, cardBack;    // Hai Layout đại diện cho mặt trước và mặt sau của thẻ
    private TextView tvWord, tvMeaning;  // Text hiển thị từ vựng và nghĩa
    private ImageView ivImage;           // Hình ảnh minh họa cho từ vựng
    private Button btnNext, btnBack;     // Nút chuyển thẻ tiếp theo và nút quay lại

    private com.example.appeng.DatabaseHelper dbHelper; // Công cụ kết nối Database
    private List<Vocab> vocabList;                     // Danh sách từ vựng được tải lên
    private int currentIndex = 0;                      // Chỉ số của từ vựng hiện tại đang học

    private boolean showingFront = true; // Trạng thái: đang hiện mặt trước (true) hay mặt sau (false)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_card);

        // --- 1. Ánh xạ View từ XML ---
        cardFront = findViewById(R.id.cardFront);
        cardBack  = findViewById(R.id.cardBack);
        tvWord    = findViewById(R.id.tvWord);
        tvMeaning = findViewById(R.id.tvMeaning);
        ivImage   = findViewById(R.id.ivImage);
        btnNext   = findViewById(R.id.btnNext);
        btnBack   = findViewById(R.id.btnBack);

        dbHelper = new com.example.appeng.DatabaseHelper(this);
        vocabList = new ArrayList<>();

        // --- 2. Nhận dữ liệu từ Intent ---
        // Lấy categoryId được truyền từ màn hình danh mục
        int categoryId = getIntent().getIntExtra("categoryId", -1);

        if (categoryId != -1) {
            loadVocabByCategory(categoryId); // Tải từ vựng theo chủ đề
        }

        // --- 3. Hiển thị dữ liệu ban đầu ---
        if (!vocabList.isEmpty()) {
            showCard(currentIndex); // Hiển thị từ đầu tiên
        } else {
            tvWord.setText("Không có dữ liệu");
            ivImage.setVisibility(View.GONE);
            btnNext.setEnabled(false);
        }

        // --- 4. Thiết lập hiệu ứng 3D cho thẻ ---
        float scale = getResources().getDisplayMetrics().density;
        // Đặt khoảng cách Camera để hiệu ứng xoay 3D nhìn sâu và chân thực hơn
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);

        // Click vào thẻ để lật (Flip)
        cardFront.setOnClickListener(v -> flipCard());
        cardBack.setOnClickListener(v -> flipCard());

        // Sự kiện các nút bấm
        btnNext.setOnClickListener(v -> nextCard());
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Truy vấn SQLite để lấy danh sách từ vựng theo chủ đề (Category)
     */
    private void loadVocabByCategory(int categoryId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            dbHelper.createDatabase(); // Đảm bảo DB đã được copy vào máy
            db = dbHelper.openDatabase();

            cursor = db.rawQuery("SELECT * FROM vocab WHERE categoryId = ?",
                    new String[]{String.valueOf(categoryId)});

            if (cursor.moveToFirst()) {
                do {
                    // Lấy dữ liệu an toàn từ các cột
                    int id       = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String word  = cursor.getString(cursor.getColumnIndexOrThrow("word"));
                    String meaning  = cursor.getString(cursor.getColumnIndexOrThrow("meaning"));
                    String phonetic = getSafe(cursor, "phonetic");
                    String example  = getSafe(cursor, "example");
                    String synonym  = getSafe(cursor, "synonym");
                    String antonym  = getSafe(cursor, "antonym");
                    String image    = getSafe(cursor, "image");

                    vocabList.add(new Vocab(id, word, meaning, phonetic, example, synonym, antonym, image));
                } while (cursor.moveToNext());
            }

            // Xáo trộn danh sách từ vựng để tăng hiệu quả ghi nhớ
            Collections.shuffle(vocabList);

        } catch (IOException e) {
            Log.e("FlashCard", "Lỗi sao chép DB", e);
        } catch (Exception e) {
            Log.e("FlashCard", "Lỗi tải dữ liệu", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    // Hàm lấy chuỗi an toàn: nếu cột không tồn tại thì trả về chuỗi rỗng thay vì lỗi
    private String getSafe(Cursor c, String col) {
        int i = c.getColumnIndex(col);
        return i == -1 ? "" : c.getString(i);
    }

    /**
     * Cập nhật dữ liệu của từ vựng hiện tại lên thẻ
     */
    private void showCard(int index) {
        if (index < 0 || index >= vocabList.size()) return;

        Vocab vocab = vocabList.get(index);
        tvWord.setText(vocab.getWord());
        tvMeaning.setText(vocab.getMeaning());

        // Xử lý hiển thị hình ảnh minh họa
        if (vocab.getImage() != null && !vocab.getImage().isEmpty()) {
            // Lấy Resource ID từ tên file ảnh lưu trong DB
            int imageResId = getResources().getIdentifier(vocab.getImage(), "drawable", getPackageName());

            if (imageResId != 0) {
                ivImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageResId).into(ivImage); // Dùng Glide để tối ưu bộ nhớ
            } else {
                ivImage.setVisibility(View.GONE);
            }
        } else {
            ivImage.setVisibility(View.GONE);
        }

        // Reset trạng thái: Luôn hiển thị mặt trước khi chuyển thẻ mới
        cardFront.setVisibility(View.VISIBLE);
        cardBack.setVisibility(View.INVISIBLE);
        cardFront.setRotationY(0); // Đưa góc xoay về 0
        showingFront = true;
    }

    /**
     * Logic lật thẻ
     */
    private void flipCard() {
        if (showingFront) {
            animateFlip(cardFront, cardBack);
        } else {
            animateFlip(cardBack, cardFront);
        }
        showingFront = !showingFront;
    }

    /**
     * Hiệu ứng xoay 3D mượt mà
     */
    private void animateFlip(View fromView, View toView) {
        // Giai đoạn 1: Xoay mặt hiện tại từ 0 -> 90 độ (để biến mất)
        ObjectAnimator hide = ObjectAnimator.ofFloat(fromView, "rotationY", 0f, 90f);
        hide.setDuration(300);

        // Giai đoạn 2: Xoay mặt đích từ -90 -> 0 độ (để hiện ra)
        ObjectAnimator show = ObjectAnimator.ofFloat(toView, "rotationY", -90f, 0f);
        show.setDuration(300);

        hide.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Khi mặt cũ xoay xong 90 độ, ẩn nó đi và hiện mặt mới
                fromView.setVisibility(View.INVISIBLE);
                toView.setVisibility(View.VISIBLE);
                show.start(); // Bắt đầu xoay mặt mới ra
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(hide);
        set.start();
    }

    /**
     * Chuyển sang thẻ tiếp theo hoặc báo hoàn thành
     */
    private void nextCard() {
        currentIndex++;

        if (currentIndex < vocabList.size()) {
            showCard(currentIndex);
        } else {
            // Khi học hết danh sách
            tvWord.setText("🎉 Hoàn thành!");
            ivImage.setVisibility(View.GONE);
            tvMeaning.setText("");

            cardFront.setVisibility(View.VISIBLE);
            cardBack.setVisibility(View.INVISIBLE);

            btnNext.setText("Làm lại");
            btnNext.setOnClickListener(v -> {
                Collections.shuffle(vocabList); // Xáo trộn lại
                currentIndex = 0;
                btnNext.setText("Tiếp theo");
                showCard(currentIndex);
                // Gán lại listener gốc sau khi nhấn "Làm lại"
                btnNext.setOnClickListener(v2 -> nextCard());
            });
        }
    }
}