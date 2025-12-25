package com.example.appeng;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appeng.Model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Settings Activity: Màn hình quản lý hồ sơ người dùng và cài đặt ứng dụng.
 */
public class Settings extends AppCompatActivity {

    // Khai báo các biến thành phần giao diện (UI Components)
    private TextView tvFullName, tvUserName, tvEmail, tvPhone, tvLevel;
    private ImageButton btnEditProfile; // Nút chuyển sang màn hình sửa thông tin
    private Button btnLogout;           // Nút đăng xuất
    private User user;                  // Đối tượng lưu trữ thông tin user hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // Thiết lập giao diện từ XML

        // --- 1. Ánh xạ View (Kết nối Java với các ID trong XML) ---
        tvFullName = findViewById(R.id.tvFullName);
        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvLevel = findViewById(R.id.tvLevel);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // --- 2. Thiết lập sự kiện Click cho các nút ---

        // Khi nhấn nút Edit (hình bút chì/người): Chuyển sang màn hình EditProfileActivity
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class))
        );

        // Khi nhấn nút Logout (Đăng xuất)
        btnLogout.setOnClickListener(v -> {
            // Xóa dữ liệu người dùng khỏi SharedPreferences thông qua SessionManager
            SessionManager.clearSession(this);

            // Tạo Intent quay về màn hình Login
            Intent intent = new Intent(this, LoginActivity.class);

            // Xóa toàn bộ lịch sử các Activity cũ (Task) để người dùng
            // không thể nhấn nút "Back" trên điện thoại để quay lại màn hình Settings
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
        });

        // Thiết lập sự kiện cho các TextView mục phụ (Help, About)
        findViewById(R.id.tvHelp).setOnClickListener(v ->
                Toast.makeText(this, "Trợ giúp đang được cập nhật.", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.tvAbout).setOnClickListener(v ->
                Toast.makeText(this, "MBApp - Học tiếng Anh\nPhiên bản 1.0", Toast.LENGTH_LONG).show()
        );

        // --- 3. Thiết lập Bottom Navigation Bar (Thanh điều hướng dưới) ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_settings); // Đánh dấu icon Settings đang được chọn

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Chuyển màn hình tương ứng với icon được chọn trên thanh BottomNav
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0); // Tắt hiệu ứng chuyển cảnh để mượt mà hơn
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, Search.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_review) {
                startActivity(new Intent(this, Review.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                return true; // Đang ở màn hình Settings, không cần làm gì
            }
            return false;
        });
    }

    /**
     * onResume: Phương thức này chạy khi người dùng quay lại màn hình Settings
     * (Ví dụ: Sau khi sửa thông tin ở EditProfileActivity và nhấn Lưu/Quay lại).
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Luôn làm mới dữ liệu từ SessionManager để hiển thị thông tin mới nhất
        loadUserInfo();
    }

    /**
     * Lấy dữ liệu user từ SessionManager và gán nội dung vào các TextView tương ứng.
     */
    private void loadUserInfo() {
        // Đọc dữ liệu user đã lưu trong máy thông qua SessionManager
        user = SessionManager.getUser(this);

        if (user != null) {
            // Hiển thị thông tin nếu tìm thấy user trong bộ nhớ
            tvFullName.setText("Họ tên: " + user.getFullname());
            tvUserName.setText("Tên tài khoản: " + user.getUsername());
            tvEmail.setText("Email: " + user.getEmail());
            tvPhone.setText("SĐT: " + user.getPhone());
            tvLevel.setText("Cấp độ: " + user.getCurrentLevel());
        } else {
            // Nếu không thấy (lỗi hệ thống hoặc chưa đăng nhập)
            Toast.makeText(this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
        }
    }
}