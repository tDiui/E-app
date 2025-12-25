package com.example.appeng;

import android.content.Context;
import android.content.SharedPreferences; // Thư viện lưu trữ dữ liệu dạng Key-Value nhẹ của Android

import com.example.appeng.Model.User;

/**
 * SessionManager: Quản lý phiên làm việc của người dùng.
 * Sử dụng SharedPreferences để lưu thông tin User xuống bộ nhớ XML của ứng dụng.
 */
public class SessionManager {

    // Tên của file XML sẽ được tạo trong hệ thống để lưu dữ liệu
    private static final String PREF_NAME = "UserSession";

    /**
     * Lưu thông tin người dùng vào bộ nhớ máy (Login thành công hoặc Cập nhật Profile).
     */
    public static void saveUser(Context context, User user) {
        // Khởi tạo SharedPreferences ở chế độ MODE_PRIVATE (chỉ ứng dụng này mới được đọc)
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit(); // Mở trình chỉnh sửa

        // Đưa dữ liệu vào bộ đệm (Key, Value)
        editor.putInt("id", user.getId());
        editor.putString("username", user.getUsername());
        editor.putString("fullname", user.getFullname());
        editor.putString("email", user.getEmail());
        editor.putString("phone", user.getPhone());
        editor.putString("currentLevel", user.getCurrentLevel());
        editor.putInt("totalScore", user.getTotalScore());
        editor.putString("lastLogin", user.getLastLogin());
        editor.putInt("passCount", user.getPassCount());

        // apply(): Lưu dữ liệu xuống file một cách bất đồng bộ (không gây giật lag giao diện)
        editor.apply();
    }

    /**
     * Lấy thông tin người dùng hiện đang đăng nhập.
     * @return Đối tượng User nếu đã đăng nhập, null nếu chưa.
     */
    public static User getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Kiểm tra xem đã có username lưu trong máy chưa (mặc định trả về null nếu không thấy)
        String username = prefs.getString("username", null);

        // Nếu không có username nghĩa là chưa ai đăng nhập
        if (username == null) return null;

        // Khởi tạo lại đối tượng User từ dữ liệu đã lưu trong SharedPreferences
        User user = new User();
        user.setId(prefs.getInt("id", -1));
        user.setUsername(username);
        user.setFullname(prefs.getString("fullname", ""));
        user.setEmail(prefs.getString("email", ""));
        user.setPhone(prefs.getString("phone", ""));
        user.setCurrentLevel(prefs.getString("currentLevel", "A1"));
        user.setTotalScore(prefs.getInt("totalScore", 0));
        user.setLastLogin(prefs.getString("lastLogin", ""));
        user.setPassCount(prefs.getInt("passCount", 0));

        return user;
    }

    /**
     * Đăng xuất: Xóa sạch toàn bộ thông tin người dùng trong bộ nhớ máy.
     */
    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // clear(): Xóa toàn bộ key-value trong file
        // apply(): Cập nhật thay đổi ngay lập tức
        prefs.edit().clear().apply();
    }
}