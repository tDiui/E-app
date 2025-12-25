package com.example.appeng;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appeng.Adapter.CategoryAdapter;
import com.example.appeng.Model.Category;
import com.example.appeng.Model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * MainActivity: Màn hình chính hiển thị danh sách chủ đề học tập (Categories).
 * Implement Interface OnItemClickListener để xử lý khi người dùng chọn một chủ đề.
 */
public class MainActivity extends AppCompatActivity implements CategoryAdapter.OnItemClickListener {

    // Khai báo các thành phần UI
    private RecyclerView recyclerView;         // Danh sách các danh mục (Category)
    private CategoryAdapter adapter;          // Bộ nạp dữ liệu cho RecyclerView
    private ArrayList<Category> categoryList; // Danh sách chứa các đối tượng Category
    private DatabaseHelper dbHelper;          // Công cụ tương tác SQLite
    private SQLiteDatabase db;                // Đối tượng kết nối DB hiện tại
    private BottomNavigationView bottomNavigationView; // Thanh điều hướng phía dưới
    private User user;                        // Thông tin người dùng hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. Thiết lập giao diện danh sách (RecyclerView) ---
        recyclerView = findViewById(R.id.recyclerViewCategories);
        // Sử dụng GridLayoutManager để chia danh sách thành 2 cột
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        categoryList = new ArrayList<>();
        // Khởi tạo adapter với listener là chính Activity này (this)
        adapter = new CategoryAdapter(this, categoryList, this);
        recyclerView.setAdapter(adapter);

        // --- 2. Khởi tạo và mở Database ---
        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase(); // Đảm bảo database đã được chép vào máy
            db = dbHelper.openDatabase(); // Mở kết nối để đọc dữ liệu
        } catch (IOException e) {
            e.printStackTrace();
        }

        // --- 3. Kiểm tra quyền truy cập ---
        // Lấy thông tin user từ SessionManager để biết trình độ hiện tại
        user = SessionManager.getUser(this);
        if (user == null) {
            // Nếu chưa đăng nhập, chuyển hướng người dùng về màn hình Login
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Đóng MainActivity
            return;
        }

        // --- 4. Thiết lập thanh điều hướng Bottom Navigation ---
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Đánh dấu đang ở Home

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true; // Đang ở Home rồi thì không làm gì cả
            }
            else if (id == R.id.nav_search) {
                // Chuyển sang màn hình tìm kiếm
                startActivity(new Intent(MainActivity.this, Search.class));
                overridePendingTransition(0, 0); // Tắt hiệu ứng chuyển cảnh để mượt hơn
                return true;
            }
            else if (id == R.id.nav_review) {
                // Chuyển sang màn hình ôn tập lỗi sai
                startActivity(new Intent(MainActivity.this, Review.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if (id == R.id.nav_settings) {
                // Chuyển sang màn hình cài đặt/cá nhân
                startActivity(new Intent(MainActivity.this, Settings.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // Tải dữ liệu danh mục lần đầu
        loadCategories();
    }

    /**
     * Xử lý khi click vào một Category cụ thể.
     */
    @Override
    public void onItemClick(Category category) {
        // Chuyển sang màn hình hiển thị danh sách từ vựng của Category đó
        Intent intent = new Intent(MainActivity.this, WordListActivity.class);
        intent.putExtra("categoryId", category.getId());
        intent.putExtra("categoryName", category.getName());
        startActivity(intent);
    }

    /**
     * Tải danh sách danh mục từ Database dựa trên Level (Trình độ) của user.
     */
    private void loadCategories() {
        categoryList.clear(); // Xóa danh sách cũ

        if (db != null && user != null) {
            String sql;

            // Logic lọc: Chỉ hiện các chủ đề có chứa từ vựng phù hợp với trình độ user
            switch (user.getCurrentLevel()) {
                case "A1":
                    sql = "SELECT DISTINCT c.id, c.name, c.icon FROM category c " +
                            "JOIN vocab v ON v.categoryId = c.id WHERE v.level = 'A1'";
                    break;
                case "A2":
                    // A2 có thể thấy cả chủ đề của A1
                    sql = "SELECT DISTINCT c.id, c.name, c.icon FROM category c " +
                            "JOIN vocab v ON v.categoryId = c.id WHERE v.level IN ('A1','A2')";
                    break;
                case "B1":
                    // B1 thấy tất cả A1, A2, B1
                    sql = "SELECT DISTINCT c.id, c.name, c.icon FROM category c " +
                            "JOIN vocab v ON v.categoryId = c.id WHERE v.level IN ('A1','A2','B1')";
                    break;
                default:
                    sql = "SELECT * FROM category"; // Các trường hợp khác lấy tất cả
            }

            // Thực thi truy vấn
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    // Chuyển dữ liệu từ Cursor sang Object Category
                    categoryList.add(new Category(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("icon"))
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        // Cập nhật lại giao diện RecyclerView sau khi đã có dữ liệu mới
        adapter.notifyDataSetChanged();
    }
}