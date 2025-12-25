package com.example.appeng;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appeng.Model.User;

/**
 * LoginActivity: Quản lý màn hình Đăng nhập và xác thực tài khoản từ SQLite.
 */
public class LoginActivity extends AppCompatActivity {

    // Khai báo các thành phần nhập liệu và nút bấm
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private DatabaseHelper dbHelper; // Đối tượng giúp tương tác với DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Gắn layout giao diện

        dbHelper = new DatabaseHelper(this);

        // --- 1. Khởi tạo Database ---
        try {
            // Đảm bảo file database đã được sao chép vào máy khi lần đầu mở app
            dbHelper.createDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo database!", Toast.LENGTH_SHORT).show();
        }

        // --- 2. Kiểm tra trạng thái đăng nhập (Auto Login) ---
        // Thử lấy thông tin user đã lưu trong SessionManager (SharedPreferences)
        User savedUser = SessionManager.getUser(this);
        if (savedUser != null) {
            // Nếu đã đăng nhập từ trước, nhảy thẳng vào màn hình chính MainActivity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish(); // Đóng LoginActivity để không thể quay lại bằng nút Back
            return;
        }

        // --- 3. Ánh xạ View ---
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Sự kiện khi nhấn nút Đăng nhập
        btnLogin.setOnClickListener(v -> loginUser());

        // Sự kiện khi nhấn nút Đăng ký: Chuyển sang màn hình RegisterActivity
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    /**
     * Logic xử lý đăng nhập người dùng
     */
    private void loginUser() {
        // Lấy chuỗi ký tự từ EditText và loại bỏ khoảng trắng thừa
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra tính hợp lệ cơ bản (không được để trống)
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 4. Truy vấn Database ---
        SQLiteDatabase db = dbHelper.openDatabase();
        // Tìm kiếm bản ghi trong bảng 'user' khớp cả Username và Password
        Cursor cursor = db.rawQuery(
                "SELECT * FROM user WHERE username=? AND password=?",
                new String[]{username, password}
        );

        // Kiểm tra xem có bản ghi nào khớp không
        if (cursor.moveToFirst()) {
            // Đăng nhập đúng: Khởi tạo đối tượng User và đổ dữ liệu từ DB vào
            User user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            user.setFullname(cursor.getString(cursor.getColumnIndexOrThrow("fullname")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            user.setCurrentLevel(cursor.getString(cursor.getColumnIndexOrThrow("currentLevel")));
            user.setTotalScore(cursor.getInt(cursor.getColumnIndexOrThrow("totalScore")));
            user.setLastLogin(cursor.getString(cursor.getColumnIndexOrThrow("lastLogin")));
            user.setPassCount(cursor.getInt(cursor.getColumnIndexOrThrow("passCount")));

            // Đóng cursor và database để giải phóng bộ nhớ
            cursor.close();
            db.close();

            // --- 5. Lưu phiên đăng nhập ---
            // Lưu đối tượng user vào SharedPreferences để dùng cho toàn bộ ứng dụng
            SessionManager.saveUser(this, user);

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            // Chuyển sang màn hình chính
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish(); // Kết thúc màn hình đăng nhập
        } else {
            // Đăng nhập sai
            Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
            cursor.close();
            db.close();
        }
    }
}