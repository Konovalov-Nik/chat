package ru.sgu.itcourses.chat.utils;

/**
 * @author Nikita Konovalov
 */
public class UserInfo {
    private String nickname;
    private boolean password;
    private boolean global;
    private String rooms;

    public UserInfo(String nickname, boolean password, boolean global, String rooms) {
        this.nickname = nickname;
        this.password = password;
        this.global = global;
        this.rooms = rooms;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isPassword() {
        return password;
    }

    public boolean isGlobal() {
        return global;
    }

    public String getRooms() {
        return rooms;
    }
}
