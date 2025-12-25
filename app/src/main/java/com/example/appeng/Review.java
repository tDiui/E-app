package com.example.appeng;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appeng.Adapter.ReviewAdapter;
import com.example.appeng.Model.User;
import com.example.appeng.Model.Vocab;
import com.example.appeng.Model.Exercise;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Review Activity: Màn hình thống kê lỗi sai và theo dõi tiến trình lên cấp.
 */
public class Review extends AppCompatActivity {

    // Khai báo các TextView hiển thị chỉ số thống kê lỗi
    private TextView tvTotalMistakes, tvFrequentMistakes, tvRecentMistakes, tvImproved;

    // Khai báo các thành phần hiển thị tiến trình Level
    private TextView tvCurrentLevel, tvLevelProgressDetail;
    private ProgressBar progressLevel; // Thanh tiến trình (0/3 bài đạt chuẩn)

    private RecyclerView recyclerView; // Danh sách hiển thị chi tiết các từ bị sai
    private ReviewAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Vocab> reviewList;     // Danh sách các từ vựng lấy từ bảng 'review'
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // --- 1. Kiểm tra đăng nhập ---
        user = SessionManager.getUser(this);
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // --- 2. Ánh xạ các View từ Layout XML ---
        tvTotalMistakes = findViewById(R.id.tvTotalMistakes);
        tvFrequentMistakes = findViewById(R.id.tvFrequentMistakes);
        tvRecentMistakes = findViewById(R.id.tvRecentMistakes);
        tvImproved = findViewById(R.id.tvImproved);

        tvCurrentLevel = findViewById(R.id.tvCurrentLevel);
        tvLevelProgressDetail = findViewById(R.id.tvLevelProgressDetail);
        progressLevel = findViewById(R.id.progressLevel);

        recyclerView = findViewById(R.id.recyclerViewReview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);

        // --- 3. Tải và xử lý dữ liệu lỗi sai từ SQLite ---
        // Lấy danh sách từ vựng mà User này đã từng làm sai
        reviewList = dbHelper.getAllMistakesVocab(user.getId());

        adapter = new ReviewAdapter(this, reviewList);
        recyclerView.setAdapter(adapter);

        // Khởi tạo các biến tính toán thống kê
        int totalMistakes = 0;    // Tổng cộng số lần sai (cộng dồn)
        int frequentMistakes = 0; // Số từ bị sai từ 3 lần trở lên
        int recentMistakes = reviewList.size(); // Số lượng từ bị sai duy nhất trong danh sách

        for (Vocab v : reviewList) {
            totalMistakes += v.getMistakeCount(); // Cộng dồn số lần sai của mỗi từ
            if (v.getMistakeCount() >= 3) {
                frequentMistakes++; // Đánh dấu đây là lỗi sai thường xuyên
            }
        }

        // Hiển thị các con số thống kê lên giao diện
        tvTotalMistakes.setText(totalMistakes + "\nTổng lỗi sai");
        tvFrequentMistakes.setText(frequentMistakes + "\nLỗi sai thường xuyên");
        tvRecentMistakes.setText(recentMistakes + "\nLỗi sai gần đây");
        tvImproved.setText("0\nTừ đã cải thiện");

        // --- 4. Cập nhật thanh tiến trình lên cấp ---
        updateLevelProgress();

        // --- 5. Sự kiện nút bấm luyện tập ---
        Button btnExercise = findViewById(R.id.btnExercise);
        btnExercise.setOnClickListener(v -> {
            // Chuyển sang màn hình làm bài tập để khắc phục lỗi
            startActivity(new Intent(Review.this, ExerciseActivity.class));
        });

        // --- 6. Thiết lập Bottom Navigation Bar ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_review); // Đánh dấu icon Review đang được chọn

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, Search.class));
                overridePendingTransition(0,0);
                return true;
            } else if (id == R.id.nav_review) {
                return true; // Đang ở đây rồi
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                overridePendingTransition(0,0);
                return true;
            }
            return false;
        });
    }

    /**
     * Hàm tính toán và hiển thị tiến trình lên cấp của người dùng.
     * Dựa trên quy tắc: Cần 3 bài tập đạt điểm >= 80% để lên cấp tiếp theo.
     */
    private void updateLevelProgress() {
        String level = user.getCurrentLevel(); // Lấy level hiện tại (A1, A2...)
        int pass = user.getPassCount();        // Lấy số bài đã đạt chuẩn (0, 1, 2)

        tvCurrentLevel.setText("Cấp hiện tại: " + level);
        tvLevelProgressDetail.setText("Bạn đã hoàn thành " + pass + "/3 bài ≥ 80%");

        // Cập nhật thanh ProgressBar (Max thường được đặt là 3 trong XML)
        progressLevel.setProgress(pass);

        if (pass < 3) {
            int remain = 3 - pass;
            // Dùng append để thêm dòng chữ mà không xóa nội dung cũ
            tvLevelProgressDetail.append("\nCần thêm " + remain + " bài nữa để lên cấp.");
        } else {
            tvLevelProgressDetail.append("\n🎉 Bạn đã đủ điều kiện lên level!");
        }
    }
}