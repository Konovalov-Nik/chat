package ru.sgu.itcourses.chat.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sgu.itcourses.chat.utils.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Konovalov_Nik
 */
public class ServerCore {
    private static final Logger LOG  = LoggerFactory.getLogger(ServerCore.class);
    public static final String GLOBAL_CHANNEL = "global";
    private static final int processorsCount = 2;

    private static final ServerCore instance = new ServerCore();
    public static final int ALIVE_PERIOD = 5000;
    private List<MessageProcessor> processors = new ArrayList<MessageProcessor>(processorsCount);

    private ServerCore() {
        registeredChannels.add(new Channel(GLOBAL_CHANNEL, ""));
    }

    public static ServerCore getInstance() {
        return instance;
    }

    public void startProcessors() {
        for (int i = 0; i < processorsCount; i++) {
            MessageProcessor processor = new MessageProcessor();
            processor.setDaemon(true);
            processor.start();
            LOG.debug("Processor " + processor + " started");
            processors.add(processor);
        }
    }

    private List<User> registeredUsers = new ArrayList<User>();
    private List<Channel> registeredChannels = new ArrayList<Channel>();
    private List<ClientConnection> connections = new ArrayList<ClientConnection>();
    private ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<Message>();

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
    private Message pollMessage() {
        return messages.poll();
    }

    public User register(String login, String password) {
        User user = new User(login, password);
        registeredUsers.add(user);
        LOG.info("User " + login + " registered.");
        addUserToChannel(user, findChannel(GLOBAL_CHANNEL));
        return user;
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
        LOG.warn("Could not find channel " + channelName);
        return null;
    }

    private class MessageProcessor extends Thread {
        @Override
        public void run() {
            while (true) {
                process();
            }
        }

        private void process() {
            Message message = ServerCore.getInstance().pollMessage();
            if (message == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    ///
                }
                return;
            }
            switch (message.getValue()[0]) {
                case "msg" : {
                    processMsgCommand(message);
                    break;
                }
                case "ping" : {
                    processPingCommand(message);
                    break;
                }
                case "disconnect": {
                    processDisconnectCommand(message);
                    break;
                }
                case "list": {
                    switch (message.getValue()[1]) {
                        case "users": {
                            processListUsersCommand(message);
                            break;
                        }
                        case "rooms": {
                            processListRoomsCommand(message);
                            break;
                        }
                    }
                    break;
                }
            }
        }


    }

    private void processListRoomsCommand(Message message) {
        ClientConnection connection = findConnection(message.getFrom());
    }

    private void processListUsersCommand(Message message) {
        ClientConnection connection = findConnection(message.getFrom());
        if (connection == null) {
            LOG.warn("Connection to " + message.getFrom() + " not found");
            return;
        }
        String[] response = makeUsersList();
        connection.send(response);
    }

    private void processDisconnectCommand(Message message) {
        ClientConnection connection = findConnection(message.getFrom());
        connection.close();
    }

    private void processPingCommand(Message message) {
        String name = message.getFrom();
        User user = findUser(name);
        user.setLastPing(System.currentTimeMillis());
    }

    private void processMsgCommand(Message message) {
        String from = message.getFrom();
        String to = message.getValue()[1];
        if (to.startsWith("@")) { //private
            String name = to.substring(1);
            sendPrivate(from, name, message.getValue()[2]);
        }
        if (to.startsWith("#")) { //channel
            String channel = to.substring(1);
            sendChannel(from, channel, message.getValue()[2]);
        }
    }

    private User findUser(String name) {
        for (User user : registeredUsers) {
            if (user.getLogin().equals(name)) {
                return user;
            }
        }
        LOG.warn("Cant find user " + name);
        return null;
    }

    private void sendChannel(String from, String channelName, String text) {
        String finalText = "msg from @" + from + " in #" + channelName + ": " + text;
        Channel channel = findChannel(channelName);
        for (User user : channel.getUsers()) {
            ClientConnection connection = findConnection(user.getLogin());
            if (connection == null) {
                LOG.warn("Message was not delivered to " + user.getLogin() + ". Connection not found.");
                continue;
            }
            connection.send(finalText);
        }
    }

    private void sendPrivate(String from, String to, String text) {
        ClientConnection connection = findConnection(to);
        if (connection == null) {
            LOG.warn("Message was not delivered to " + to + ". Connection not found.");
            return;
        }
        String finalText = "private msg from @" + from + ": " + text;
        connection.send(finalText);
    }

    private String[] makeUsersList() {
        List<User> connected = getConnectedUsers();
        List<UserInfo> infos = new ArrayList<>();
        int maxNameWidth = 10;
        int maxRoomsWidth = 12;
        for (User user : connected) {
            UserInfo info = user.getUserInfo();
            infos.add(info);
            maxNameWidth = Math.max(maxNameWidth, info.getNickname().length());
            maxRoomsWidth = Math.max(maxRoomsWidth, info.getRooms().length());
        }
        //todo
    }

    private List<User> getConnectedUsers() {
        List<User> result = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (User user : registeredUsers) {
            if (now - user.getLastPing() < ALIVE_PERIOD) {
                result.add(user);
            }
        }
        return result;
    }

    private ClientConnection findConnection(String name) {
        for (ClientConnection connection : connections) {
            if (connection.getUser().getLogin().equals(name)) {
                return connection;
            }
        }
        LOG.warn("Cant find connection to " + name);
        return null;
    }


}
