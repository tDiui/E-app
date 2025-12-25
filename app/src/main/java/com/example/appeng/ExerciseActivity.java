package com.example.appeng;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appeng.Adapter.ExerciseAdapter;
import com.example.appeng.Model.Exercise;
import com.example.appeng.Model.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * ExerciseActivity: Quản lý logic làm bài tập trắc nghiệm.
 */
public class ExerciseActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện
    private RecyclerView recyclerViewExercise; // Danh sách hiển thị các câu hỏi
    private ExerciseAdapter adapter;           // Bộ nạp dữ liệu cho RecyclerView
    private DatabaseHelper dbHelper;           // Công cụ tương tác SQLite
    private User user;                         // Đối tượng người dùng hiện tại

    private List<Exercise> exerciseList;       // Danh sách các câu hỏi lấy từ DB
    private TextView tvTimer;                  // Hiển thị đồng hồ đếm ngược
    private Button btnSubmit, btnDoneReview;   // Nút Nộp bài và nút Hoàn thành xem lại

    private CountDownTimer timer;              // Đối tượng quản lý thời gian đếm ngược

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // --- 1. Ánh xạ View và khởi tạo công cụ ---
        recyclerViewExercise = findViewById(R.id.recyclerViewExercise);
        tvTimer = findViewById(R.id.tvTimer);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnDoneReview = findViewById(R.id.btnDoneReview);

        // Ẩn nút "Xong" (nút này chỉ hiện sau khi đã nộp bài và xem lại lỗi)
        btnDoneReview.setVisibility(View.GONE);

        dbHelper = new DatabaseHelper(this);
        recyclerViewExercise.setLayoutManager(new LinearLayoutManager(this));

        // --- 2. Kiểm tra đăng nhập ---
        user = SessionManager.getUser(this);
        if (user == null) {
            Toast.makeText(this, "Bạn cần đăng nhập!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --- 3. Lấy dữ liệu câu hỏi ---
        // Lấy bài tập dựa trên cấp độ hiện tại của user (ví dụ: A1, A2...)
        exerciseList = dbHelper.getExercisesByLevelWithLower(user.getCurrentLevel());

        // Xáo trộn ngẫu nhiên danh sách câu hỏi
        Collections.shuffle(exerciseList);

        // Giới hạn tối đa mỗi bài kiểm tra chỉ có 15 câu
        if (exerciseList.size() > 15)
            exerciseList = exerciseList.subList(0, 15);

        // Thiết lập Adapter để hiển thị danh sách câu hỏi lên màn hình
        adapter = new ExerciseAdapter(this, exerciseList);
        recyclerViewExercise.setAdapter(adapter);

        // --- 4. Bắt đầu đếm ngược và sự kiện nút bấm ---
        startTimer();

        // Khi nhấn Nộp bài, hiện Popup xác nhận
        btnSubmit.setOnClickListener(v -> showSubmitPopup());
    }

    /**
     * Thiết lập đồng hồ đếm ngược 20 phút (20 * 60 * 1000 miligiây)
     */
    private void startTimer() {
        timer = new CountDownTimer(20 * 60 * 1000, 1000) {
            public void onTick(long ms) {
                // Chuyển đổi miligiây còn lại sang định dạng Phút:Giây
                int sec = (int) (ms / 1000);
                tvTimer.setText(String.format("⏳ %02d:%02d", sec / 60, sec % 60));
            }

            public void onFinish() {
                // Tự động nộp bài khi hết giờ
                submitExam();
            }
        }.start();
    }

    /**
     * Hiển thị hộp thoại hỏi người dùng có chắc chắn muốn nộp bài không
     */
    private void showSubmitPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Nộp bài?")
                .setMessage("Bạn có chắc chắn muốn nộp bài không?")
                .setPositiveButton("Nộp ngay", (dialog, which) -> submitExam())
                .setNegativeButton("Làm tiếp", null)
                .show();
    }

    /**
     * Logic chấm điểm và xử lý sau khi nộp bài
     */
    private void submitExam() {
        timer.cancel(); // Dừng đồng hồ

        // Lấy danh sách câu trả lời mà người dùng đã chọn từ Adapter
        HashMap<Integer, String> answers = adapter.getUserAnswers();

        int correct = 0;

        // Duyệt qua danh sách câu hỏi ban đầu để đối chiếu đáp án
        for (Exercise ex : exerciseList) {
            String userAns = answers.get(ex.getId());

            // Nếu trả lời đúng (không phân biệt chữ hoa chữ thường)
            if (userAns != null && userAns.equalsIgnoreCase(ex.getCorrectAnswer())) {
                correct++;
            } else {
                // Nếu sai: Lưu câu hỏi này vào bảng 'review' để học lại sau
                dbHelper.addMistake("exercise", ex.getId(), user.getId());
            }
        }

        // Tính toán phần trăm điểm số
        int score = (correct * 100) / exerciseList.size();

        // Ẩn nút nộp bài và đồng hồ sau khi hoàn thành
        btnSubmit.setVisibility(View.GONE);
        tvTimer.setVisibility(View.GONE);

        // Chuyển Adapter sang chế độ "Xem lại" (Hiển thị đáp án đúng/sai trên giao diện)
        adapter.setReviewMode(true, answers);
        adapter.notifyDataSetChanged(); // Yêu cầu danh sách vẽ lại giao diện

        Toast.makeText(this, "Bạn đạt " + score + "%", Toast.LENGTH_LONG).show();

        // Xử lý cộng dồn để thăng cấp level
        handleLevel(score);

        // Lưu thông tin user mới (level, passCount) vào Session
        SessionManager.saveUser(this, user);

        // Hiện nút "Xong" để thoát màn hình bài tập
        btnDoneReview.setVisibility(View.VISIBLE);
        btnDoneReview.setOnClickListener(v -> finish());
    }

    /**
     * Logic thăng cấp: Nếu đạt >= 80 điểm trong 3 lần liên tiếp sẽ được lên cấp
     */
    private void handleLevel(int score) {
        if (score >= 80) {
            // Nếu đạt điểm giỏi, tăng số lần vượt qua liên tiếp
            user.setPassCount(user.getPassCount() + 1);
        } else {
            // Nếu dưới 80 điểm, reset chuỗi thắng về 0
            user.setPassCount(0);
        }

        // Kiểm tra nếu đã đủ 3 lần vượt qua liên tiếp
        if (user.getPassCount() >= 3) {
            user.setPassCount(0); // Reset chuỗi để tính cho cấp tiếp theo

            String old = user.getCurrentLevel();

            // Logic thăng cấp theo thứ tự: A1 -> A2 -> B1 -> B2
            switch (old) {
                case "A1": user.setCurrentLevel("A2"); break;
                case "A2": user.setCurrentLevel("B1"); break;
                case "B1": user.setCurrentLevel("B2"); break;
            }

            Toast.makeText(this,
                    "🎉 Chúc mừng! Bạn đã lên cấp " + user.getCurrentLevel(),
                    Toast.LENGTH_LONG).show();
        }

        // Cập nhật thông tin User mới vào Cơ sở dữ liệu SQLite
        dbHelper.updateUser(user);
        // Lưu lại vào SharedPreferences để các màn hình khác nhận diện level mới
        SessionManager.saveUser(this, user);
    }
}