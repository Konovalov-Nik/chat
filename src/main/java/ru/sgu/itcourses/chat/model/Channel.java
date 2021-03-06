package ru.sgu.itcourses.chat.model;

import ru.sgu.itcourses.chat.utils.RoomInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konovalov_Nik
 */
public class Channel {
    private String name;
    private String password;
    private List<User> users;

    public Channel(String name, String password) {
        this.name = name;
        this.password = password;
        users = new ArrayList<User>();
    }

    public String getName() {
        return name;
    }

    public List<User> getUsers() {
        return users;
    }

    public String getPassword() {
        return password;
    }

    public RoomInfo getRoomInfo() {
        return new RoomInfo(name, !password.equals(""), users.size());
    }
}
