package ru.sgu.itcourses.chat.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

/**
 * @author Konovalov_Nik
 */
public class ServerCore {
    private static final Logger LOG  = LoggerFactory.getLogger(ServerCore.class);
    private static final String GLOBAL_CHANNEL = "global";

    private static final ServerCore instance = new ServerCore();
    private ServerCore() {
        registeredChannels.add(new Channel(GLOBAL_CHANNEL, ""));
    }

    public static ServerCore getInstance() {
        return instance;
    }

    private List<User> registeredUsers = new ArrayList<User>();
    private List<Channel> registeredChannels = new ArrayList<Channel>();
    private List<ClientConnection> connections = new ArrayList<ClientConnection>();
    private SynchronousQueue<Message> messages = new SynchronousQueue<Message>();

    public void addConnection(ClientConnection clientConnection) {
        connections.add(clientConnection);
    }

    public boolean isRegistered(String login) {
        for (User user : registeredUsers) {
            if (user.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public User checkPasswordAndGet(String login, String password) {
        for (User user : registeredUsers) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void register(String login, String password) {
        User user = new User(login, password);
        registeredUsers.add(user);
        addUserToChannel(user, findChannel(GLOBAL_CHANNEL));
    }

    private void addUserToChannel(User user, Channel channel) {
        user.getChannels().add(channel);
        channel.getUsers().add(user);
        LOG.info("User " + user.getLogin() + " joined channel "+ channel.getName());
    }

    private Channel findChannel(String channelName) {
        for (Channel registeredChannel : registeredChannels) {
            if (registeredChannel.getName().equals(channelName)) {
                return registeredChannel;
            }
        }
        return null;
    }
}
