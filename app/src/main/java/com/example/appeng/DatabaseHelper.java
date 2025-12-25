package com.example.appeng;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.appeng.Model.Exercise;
import com.example.appeng.Model.User;
import com.example.appeng.Model.Vocab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * DATABASE HELPER - Quản lý toàn bộ cơ sở dữ liệu của ứng dụng.
 * Chức năng: Sao chép DB từ Assets, Quản lý lỗi sai (Review), Truy vấn bài tập và User.
 */
public class DatabaseHelper {

    private final Context context;
    private static final String DB_NAME = "mbapp.db"; // Tên file DB trong assets
    private final String DB_PATH; // Đường dẫn lưu trữ DB trên bộ nhớ máy

    public DatabaseHelper(Context context) {
        this.context = context;
        // Đường dẫn chuẩn: /data/user/0/com.example.appeng/databases/mbapp.db
        this.DB_PATH = context.getDatabasePath(DB_NAME).getPath();
    }

    // ==========================================================
    // 1. KHỞI TẠO VÀ SAO CHÉP DATABASE (ASSETS -> HỆ THỐNG)
    // ==========================================================

    /**
     * Kiểm tra và chuẩn bị database trước khi ứng dụng sử dụng.
     */
    public void createDatabase() throws IOException {
        if (!checkDatabase()) {
            copyDatabase();
        }
        // Luôn mở ra để đảm bảo bảng review được khởi tạo
        SQLiteDatabase db = openDatabase();
        createReviewTable(db);
        db.close();
    }

    // Kiểm tra xem file .db đã tồn tại trong bộ nhớ ứng dụng chưa
    private boolean checkDatabase() {
        File dbFile = new File(DB_PATH);
        return dbFile.exists();
    }

    // Thực hiện sao chép file từ thư mục assets/databases/ vào bộ nhớ trong của máy
    private void copyDatabase() throws IOException {
        InputStream input = context.getAssets().open("databases/" + DB_NAME);
        File dbFile = new File(DB_PATH);

        // Tạo thư mục nếu máy chưa có thư mục databases/
        if (dbFile.getParentFile() != null) dbFile.getParentFile().mkdirs();

        OutputStream output = new FileOutputStream(DB_PATH);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        output.flush();
        output.close();
        input.close();

        // Cấp quyền đọc/ghi cho file hệ thống vừa tạo
        File dbFileWritable = new File(DB_PATH);
        dbFileWritable.setReadable(true, false);
        dbFileWritable.setWritable(true, false);
    }

    /**
     * Mở kết nối Database để thực hiện truy vấn.
     */
    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }

    // ==========================================================
    // 2. QUẢN LÝ LỖI SAI (HỆ THỐNG ÔN TẬP - REVIEW)
    // ==========================================================

    // Tạo bảng 'review' để theo dõi các từ vựng hoặc bài tập làm sai
    private void createReviewTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS review (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId INTEGER NOT NULL," +
                "vocabId INTEGER," +
                "exerciseId INTEGER," +
                "mistakeCount INTEGER DEFAULT 1," + // Đếm số lần làm sai
                "lastMistakeTime TEXT," +           // Lưu thời gian sai mới nhất
                "FOREIGN KEY(userId) REFERENCES user(id)," +
                "FOREIGN KEY(vocabId) REFERENCES vocab(id)," +
                "FOREIGN KEY(exerciseId) REFERENCES exercise(id))");
    }

    /**
     * Lưu vết khi người dùng chọn sai đáp án.
     */
    public void addMistake(String type, int itemId, int userId) {
        SQLiteDatabase db = openDatabase();
        String column = type.equals("vocab") ? "vocabId" : "exerciseId";

        // Kiểm tra xem lỗi này đã có trong danh sách chưa
        Cursor cursor = db.rawQuery(
                "SELECT mistakeCount FROM review WHERE " + column + " = ? AND userId = ?",
                new String[]{String.valueOf(itemId), String.valueOf(userId)}
        );

        if (cursor.moveToFirst()) {
            // Nếu đã tồn tại: Tăng đếm lỗi + Cập nhật thời gian thực
            int count = cursor.getInt(0);
            db.execSQL(
                    "UPDATE review SET mistakeCount = ?, lastMistakeTime = datetime('now','localtime') " +
                            "WHERE " + column + " = ? AND userId = ?",
                    new Object[]{count + 1, itemId, userId}
            );
        } else {
            // Nếu chưa tồn tại: Thêm bản ghi mới
            db.execSQL(
                    "INSERT INTO review (userId, " + column + ", mistakeCount, lastMistakeTime) " +
                            "VALUES (?, ?, 1, datetime('now','localtime'))",
                    new Object[]{userId, itemId}
            );
        }
        cursor.close();
        db.close();
    }

    /**
     * Lấy toàn bộ từ vựng bị sai để hiển thị trong màn hình Ôn Tập.
     */
    public List<Vocab> getAllMistakesVocab(int userId) {
        List<Vocab> list = new ArrayList<>();
        SQLiteDatabase db = openDatabase();
        // JOIN để lấy nghĩa từ vựng từ bảng 'vocab' dựa trên id trong bảng 'review'
        Cursor cursor = db.rawQuery(
                "SELECT v.id AS vid, v.word, v.meaning, r.mistakeCount, r.lastMistakeTime " +
                        "FROM review r JOIN vocab v ON r.vocabId = v.id " +
                        "WHERE r.userId = ? " +
                        "ORDER BY r.lastMistakeTime DESC",
                new String[]{String.valueOf(userId)}
        );

        if (cursor.moveToFirst()) {
            do {
                Vocab vocab = new Vocab();
                vocab.setId(cursor.getInt(cursor.getColumnIndexOrThrow("vid")));
                vocab.setWord(cursor.getString(cursor.getColumnIndexOrThrow("word")));
                vocab.setMeaning(cursor.getString(cursor.getColumnIndexOrThrow("meaning")));
                vocab.setMistakeCount(cursor.getInt(cursor.getColumnIndexOrThrow("mistakeCount")));
                vocab.setLastMistakeTime(cursor.getString(cursor.getColumnIndexOrThrow("lastMistakeTime")));
                list.add(vocab);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    // ==========================================================
    // 3. QUẢN LÝ BÀI TẬP THEO CẤP ĐỘ
    // ==========================================================

    /**
     * Lấy bài tập phù hợp với Level người dùng (bao gồm cả các Level thấp hơn).
     */
    public List<Exercise> getExercisesByLevelWithLower(String level) {
        List<Exercise> list = new ArrayList<>();
        SQLiteDatabase db = openDatabase();

        String query = buildExerciseLevelQuery(level);
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Exercise ex = new Exercise();
                ex.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                ex.setQuestion(cursor.getString(cursor.getColumnIndexOrThrow("question")));
                ex.setOptionA(cursor.getString(cursor.getColumnIndexOrThrow("optionA")));
                ex.setOptionB(cursor.getString(cursor.getColumnIndexOrThrow("optionB")));
                ex.setOptionC(cursor.getString(cursor.getColumnIndexOrThrow("optionC")));
                ex.setOptionD(cursor.getString(cursor.getColumnIndexOrThrow("optionD")));
                ex.setCorrectAnswer(cursor.getString(cursor.getColumnIndexOrThrow("correctAnswer")));
                ex.setLevel(cursor.getString(cursor.getColumnIndexOrThrow("level")));
                list.add(ex);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    private String buildExerciseLevelQuery(String level) {
        switch (level) {
            case "A1":
                return "SELECT * FROM exercise WHERE level = 'A1'";
            case "A2":
                return "SELECT * FROM exercise WHERE level IN ('A1','A2')";
            case "B1":
                return "SELECT * FROM exercise WHERE level IN ('A1','A2','B1')";
            default:
                return "SELECT * FROM exercise";
        }
    }

    // ==========================================================
    // 4. QUẢN LÝ NGƯỜI DÙNG (USER)
    // ==========================================================

    /**
     * Cập nhật thông tin hồ sơ người dùng.
     */
    public boolean updateUser(User user) {
        SQLiteDatabase db = openDatabase();
        try {
            db.execSQL(
                    "UPDATE user SET fullname=?, email=?, phone=? WHERE id=?",
                    new Object[]{user.getFullname(), user.getEmail(), user.getPhone(), user.getId()}
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    /**
     * Đăng ký tài khoản mới. Trả về false nếu username đã tồn tại.
     */
    public boolean addUser(String username, String password, String fullname, String email, String phone, String level) {
        SQLiteDatabase db = openDatabase();

        // Kiểm tra xem username đã tồn tại chưa
        Cursor cursor = db.rawQuery("SELECT id FROM user WHERE username = ?", new String[]{username});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            db.close();
            return false;
        }

        // Chèn dữ liệu mới kèm thời gian đăng nhập lần đầu
        db.execSQL("INSERT INTO user (username, password, fullname, email, phone, currentLevel, passCount, totalScore, lastLogin) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))",
                new Object[]{username, password, fullname, email, phone, level, 0, 0});

        db.close();
        return true;
    }
}