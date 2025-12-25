package com.example.appeng.Model;

import java.io.Serializable;

public class User implements Serializable {

    private int id;
    private String username;
    private String password;
    private String fullname;
    private String phone;
    private String email;
    private String currentLevel;
    private int totalScore;
    private String lastLogin;
    private int passCount;

    public User() {}

    // ⭐ Constructor đầy đủ – đã FIX passedCount
    public User(int id, String username, String password, String fullname, String phone, String email,
                String currentLevel, int totalScore, String lastLogin, int passCount) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.phone = phone;
        this.email = email;
        this.currentLevel = currentLevel;
        this.totalScore = totalScore;
        this.lastLogin = lastLogin;
        this.passCount = passCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPassCount() { return passCount; }
    public void setPassCount(int passCount) { this.passCount = passCount; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(String currentLevel) { this.currentLevel = currentLevel; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
}
