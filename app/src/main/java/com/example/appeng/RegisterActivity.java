package com.example.appeng;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * RegisterActivity: Quản lý màn hình Đăng ký tài khoản mới.
 */
public class RegisterActivity extends AppCompatActivity {

    // Khai báo các thành phần nhập liệu (EditText) và nút bấm (Button)
    private EditText edtUsername, edtPassword, edtConfirmPassword, edtFullName, edtEmail, edtPhone;
    private Button btnRegister, btnBackLogin;

    // Khai báo đối tượng hỗ trợ thao tác với SQLite
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Kết nối với file giao diện XML

        // Khởi tạo công cụ hỗ trợ Database
        dbHelper = new DatabaseHelper(this);

        // --- 1. Ánh xạ View (Kết nối biến Java với ID trong XML) ---
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackLogin = findViewById(R.id.btnBackLogin);

        // --- 2. Thiết lập sự kiện cho các nút bấm ---

        // Khi nhấn nút Đăng ký
        btnRegister.setOnClickListener(v -> registerUser());

        // Khi nhấn nút Quay lại Đăng nhập
        btnBackLogin.setOnClickListener(v -> {
            // Chuyển hướng về LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            // Đóng RegisterActivity để giải phóng bộ nhớ
            finish();
        });
    }

    /**
     * Phương thức xử lý logic Đăng ký người dùng
     */
    private void registerUser() {
        // Lấy dữ liệu từ các ô nhập liệu và xóa khoảng trắng dư thừa ở hai đầu
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirm = edtConfirmPassword.getText().toString().trim();
        String fullname = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        // --- 3. Kiểm tra tính hợp lệ của dữ liệu (Validation) ---

        // Kiểm tra các trường bắt buộc không được để trống
        if (username.isEmpty() || password.isEmpty() || fullname.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return; // Dừng hàm nếu thiếu thông tin
        }

        // Kiểm tra xem mật khẩu nhập lại có khớp với mật khẩu ban đầu không
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return; // Dừng hàm nếu mật khẩu không khớp
        }

        // Mặc định tất cả người dùng mới đều bắt đầu ở trình độ A1
        String level = "A1";

        // --- 4. Thực hiện thêm người dùng vào Cơ sở dữ liệu ---
        // Gọi hàm addUser từ DatabaseHelper để thực hiện lệnh INSERT vào bảng user
        boolean success = dbHelper.addUser(username, password, fullname, email, phone, level);

        if (success) {
            // Nếu đăng ký thành công (không trùng Username)
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

            // Chuyển người dùng sang màn hình Đăng nhập để họ tiến hành vào App
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            // Nếu hàm addUser trả về false (thường là do tên đăng nhập đã có người dùng)
            Toast.makeText(this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
        }
    }
}