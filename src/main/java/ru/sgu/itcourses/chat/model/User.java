package ru.sgu.itcourses.chat.model;

import java.util.List;

/**
 * @author Konovalov_Nik
 */
public class User {
    private String login;
    private String password;
    private List<Channel> channels;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public List<Channel> getChannels() {
        return channels;
    }
}
