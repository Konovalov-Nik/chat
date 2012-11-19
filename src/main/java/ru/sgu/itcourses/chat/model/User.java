package ru.sgu.itcourses.chat.model;

import ru.sgu.itcourses.chat.utils.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konovalov_Nik
 */
public class User {
    private String login;
    private String password;
    private List<Channel> channels = new ArrayList<Channel>();
    private volatile long lastPing;

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

    public long getLastPing() {
        return lastPing;
    }

    public void setLastPing(long lastPing) {
        this.lastPing = lastPing;
    }

    public UserInfo getUserInfo() {
        boolean global = false;
        StringBuilder channelsStringBuilder = new StringBuilder();
        for (Channel channel : channels) {
            if (channel.getName().equals(ServerCore.GLOBAL_CHANNEL)) {
                global = true;
            } else  {
                channelsStringBuilder.append(channel.getName());
                channelsStringBuilder.append(", ");
            }
        }
        return new UserInfo(login, !password.equals(""), global, channelsStringBuilder.toString());
    }
}
