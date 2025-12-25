package com.example.appeng;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appeng.Model.User;

/**
 * Lớp EditProfileActivity: Quản lý màn hình chỉnh sửa thông tin cá nhân.
 */
public class EditProfileActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện (UI)
    private EditText edtUsername, edtFullname, edtEmail, edtPhone;
    private Button btnSave, btnCancel;

    // Khai báo công cụ hỗ trợ
    private DatabaseHelper dbHelper; // Dùng để cập nhật dữ liệu vào SQLite
    private User user;               // Đối tượng lưu thông tin người dùng hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile); // Gắn giao diện từ file XML

        // Khởi tạo đối tượng DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Lấy thông tin người dùng đang đăng nhập từ SessionManager (SharedPreferences)
        user = SessionManager.getUser(this);

        // Kiểm tra bảo mật: Nếu chưa đăng nhập (user null) thì đá ra ngoài màn hình chính
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity này ngay lập tức
            return;
        }

        // Ánh xạ các biến Java với các ID thành phần trong file XML
        edtUsername = findViewById(R.id.edtUsername);
        edtFullname = findViewById(R.id.edtFullname);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Hiển thị thông tin hiện tại của User lên các ô nhập liệu (EditText)
        edtUsername.setText(user.getUsername());
        edtFullname.setText(user.getFullname());
        edtEmail.setText(user.getEmail());
        edtPhone.setText(user.getPhone());

        // Thiết lập sự kiện khi nhấn nút Lưu
        btnSave.setOnClickListener(v -> saveUserInfo());

        // Thiết lập sự kiện khi nhấn nút Hủy (chỉ đơn giản là đóng màn hình hiện tại)
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Phương thức xử lý việc lưu thông tin người dùng.
     */
    private void saveUserInfo() {
        // Lấy dữ liệu mới từ các ô EditText và loại bỏ khoảng trắng thừa ở 2 đầu (.trim())
        String fullname = edtFullname.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào (Validation)
        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Họ tên không được để trống!", Toast.LENGTH_SHORT).show();
            return; // Dừng hàm nếu họ tên trống
        }

        // Cập nhật các giá trị mới vào đối tượng 'user' hiện tại
        user.setFullname(fullname);
        user.setEmail(email);
        user.setPhone(phone);

        // Gọi hàm updateUser trong DatabaseHelper để ghi dữ liệu vào SQLite
        boolean success = dbHelper.updateUser(user);

        if (success) {
            // Nếu lưu vào DB thành công, cập nhật lại cả Session (Bộ nhớ tạm)
            SessionManager.saveUser(this, user);

            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

            // Đóng màn hình chỉnh sửa và quay về màn hình trước đó
            finish();
        } else {
            // Nếu có lỗi (Ví dụ: DB bị khóa, lỗi SQL...)
            Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
        }
    }
}