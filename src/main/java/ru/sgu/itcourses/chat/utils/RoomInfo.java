package ru.sgu.itcourses.chat.utils;

public class RoomInfo {
    private String name;
    private boolean password;
    private int userCount;

    public RoomInfo(String name, boolean password, int userCount) {
        this.name = name;
        this.password = password;
        this.userCount = userCount;
    }

    public String getName() {
        return name;
    }

    public boolean isPassword() {
        return password;
    }

    public int getUserCount() {
        return userCount;
    }
}
