package ru.sgu.itcourses.chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sgu.itcourses.chat.model.Channel;
import ru.sgu.itcourses.chat.model.ClientConnection;
import ru.sgu.itcourses.chat.model.Message;
import ru.sgu.itcourses.chat.model.User;
import ru.sgu.itcourses.chat.utils.RoomInfo;
import ru.sgu.itcourses.chat.utils.UserInfo;

import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static ru.sgu.itcourses.chat.utils.Utils.fillString;

/**
 * @author Konovalov_Nik
 */
public class ServerCore {
    private static final Logger LOG = LoggerFactory.getLogger(ServerCore.class);
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

    private List<User> registeredUsers = Collections.synchronizedList(new ArrayList<User>());
    private List<Channel> registeredChannels = Collections.synchronizedList(new ArrayList<Channel>());
    private List<ClientConnection> connections = Collections.synchronizedList(new ArrayList<ClientConnection>());
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
        if (!channelContainsUser(channel, user)) {
            user.getChannels().add(channel);
            channel.getUsers().add(user);
            LOG.info("User " + user.getLogin() + " joined channel " + channel.getName());
        } else {
            findConnection(user.getLogin()).send("Already in room " + channel.getName() + ".");
        }
    }

    private boolean channelContainsUser(Channel channel, User user) {
        for (User u : channel.getUsers()) {
            if (u.getLogin().equals(user.getLogin())) {
                return true;
            }
        }
        return false;
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
                case "msg": {
                    processMsgCommand(message);
                    break;
                }
                case "ping": {
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
                case "join": {
                    processJoinCommand(message);
                    break;
                }
                case "leave": {
                    processLeaveCommand(message);
                    break;
                }
            }
        }


    }

    private void processLeaveCommand(Message message) {
        String roomName = message.getValue()[1];
        String userName = message.getFrom();
        User user = findUser(userName);
        Channel channel = findChannel(roomName);
        if (channel == null) {
            findConnection(userName).send("Cant leave room " + roomName + ".");
            return;
        }
        removeUserFromChannel(user, channel);

    }

    private void removeUserFromChannel(User user, Channel channel) {
        for (Channel c : new ArrayList<>(user.getChannels())) {
            if (c.getName().equals(channel.getName())) {
                user.getChannels().remove(c);
                break;
            }
        }

        for (User u : new ArrayList<>(channel.getUsers())) {
            if (u.getLogin().equals(u.getLogin())) {
                channel.getUsers().remove(u);
                break;
            }
        }

        removeIfEmptyChannel(channel);
    }

    private void removeIfEmptyChannel(Channel channel) {
        if (channel.getName().equals(GLOBAL_CHANNEL)) {
            return;
        }
        if (channel.getUsers().size() == 0) {
            registeredChannels.remove(channel);
        }
    }

    private void processJoinCommand(Message message) {
        String roomName = message.getValue()[1];
        String password = "";
        if (message.getValue().length > 2) {
            password = message.getValue()[2];
        }
        Channel channel = findChannel(roomName);
        User user = findUser(message.getFrom());
        if (channel == null) {
            Channel nc = createChannel(roomName, password);
            addUserToChannel(user, nc);
        } else {
            if (channel.getPassword().equals(password)) {
                addUserToChannel(user, channel);
            } else {
                findConnection(user.getLogin()).send("wrong password");
            }
        }
    }

    private Channel createChannel(String roomName, String password) {
        Channel channel = new Channel(roomName, password);
        registeredChannels.add(channel);
        return channel;
    }

    private void processListRoomsCommand(Message message) {
        ClientConnection connection = findConnection(message.getFrom());
        if (connection == null) {
            LOG.warn("Connection to " + message.getFrom() + " not found");
            return;
        }
        String[] response = makeRoomsList();
        connection.send(response);
    }

    private String[] makeRoomsList() {

        List<RoomInfo> infos = new ArrayList<RoomInfo>();
        String[] headers = {"Room name", "Password", "User count"};
        int maxNameWidth = headers[0].length();
        for (Channel channel : registeredChannels) {
            RoomInfo info = channel.getRoomInfo();
            infos.add(info);
            maxNameWidth = Math.max(maxNameWidth, info.getName().length());
        }
        int[] colWidth = new int[3];
        colWidth[0] = maxNameWidth;
        colWidth[1] = headers[1].length();
        colWidth[2] = headers[2].length();

        StringBuilder separatorBuilder = new StringBuilder();
        separatorBuilder.append("+");
        for (int w : colWidth) {
            separatorBuilder.append(fillString(w, '-'));
            separatorBuilder.append('+');
        }
        String separator = separatorBuilder.toString();
        List<String> result = new ArrayList<String>();
        result.add(separator);
        StringBuilder headerBuilder = new StringBuilder();

        for (int i = 0; i < headers.length; i++) {
            headerBuilder.append("|");
            headerBuilder.append(headers[i]);
            headerBuilder.append(fillString(colWidth[i] - headers[i].length(), ' '));
        }
        headerBuilder.append("|");
        result.add(headerBuilder.toString());
        result.add(separator);
        for (RoomInfo info : infos) {
            StringBuilder lineBuilder = new StringBuilder();
            lineBuilder.append("|");
            lineBuilder.append(info.getName());
            lineBuilder.append(fillString(colWidth[0] - info.getName().length(), ' '));

            lineBuilder.append("|");
            String paswd = info.isPassword() ? "Yes" : "No";
            lineBuilder.append(paswd);
            lineBuilder.append(fillString(colWidth[1] - paswd.length(), ' '));

            lineBuilder.append("|");
            String cnt = String.valueOf(info.getUserCount());
            lineBuilder.append(cnt);
            lineBuilder.append(fillString(colWidth[2] - cnt.length(), ' '));
            lineBuilder.append("|");

            result.add(lineBuilder.toString());
            result.add(separator);
        }


        return result.toArray(new String[result.size()]);

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
        connections.remove(connection);
    }

    private void processPingCommand(Message message) {
        String name = message.getFrom();
        User user = findUser(name);
        user.setLastPing(System.currentTimeMillis());
    }

    private void processMsgCommand(Message message) {
        String from = message.getFrom();
        String to = message.getValue()[1];
        StringBuilder textBuilder = new StringBuilder();
        for (int i = 2; i < message.getValue().length; i++) {
            textBuilder.append(message.getValue()[i]);
            textBuilder.append(" ");
        }
        if (to.startsWith("@")) { //private
            String name = to.substring(1);
            sendPrivate(from, name, textBuilder.toString());
        }
        if (to.startsWith("#")) { //channel
            String channel = to.substring(1);
            sendChannel(from, channel, textBuilder.toString());
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
        User sender = findUser(from);
        Channel channel = findChannel(channelName);
        ClientConnection senderConnection = findConnection(from);
        if (channel == null) {
            senderConnection.send("No such channel " + channelName + ".");
        }
        if (!channelContainsUser(channel ,sender)) {
            senderConnection.send("Not a member of " + channelName + ".");
            return;
        }
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
        List<UserInfo> infos = new ArrayList<UserInfo>();
        String[] headers = {"Nickname", "Password", "Global", "Rooms Connected"};
        int maxNameWidth = headers[0].length();
        int maxRoomsWidth = headers[3].length();
        for (User user : connected) {
            UserInfo info = user.getUserInfo();
            infos.add(info);
            maxNameWidth = Math.max(maxNameWidth, info.getNickname().length());
            maxRoomsWidth = Math.max(maxRoomsWidth, info.getRooms().length());
        }
        int[] colWidth = new int[4];
        colWidth[0] = maxNameWidth;
        colWidth[1] = headers[1].length();
        colWidth[2] = headers[2].length();
        colWidth[3] = maxRoomsWidth;
        StringBuilder separatorBuilder = new StringBuilder();
        separatorBuilder.append("+");
        for (int w : colWidth) {
            separatorBuilder.append(fillString(w, '-'));
            separatorBuilder.append('+');
        }
        String separator = separatorBuilder.toString();
        List<String> result = new ArrayList<String>();
        result.add(separator);
        StringBuilder headerBuilder = new StringBuilder();

        for (int i = 0; i < headers.length; i++) {
            headerBuilder.append("|");
            headerBuilder.append(headers[i]);
            headerBuilder.append(fillString(colWidth[i] - headers[i].length(), ' '));
        }
        headerBuilder.append("|");
        result.add(headerBuilder.toString());
        result.add(separator);
        for (UserInfo info : infos) {
            StringBuilder lineBuilder = new StringBuilder();
            lineBuilder.append("|");
            lineBuilder.append(info.getNickname());
            lineBuilder.append(fillString(colWidth[0] - info.getNickname().length(), ' '));

            lineBuilder.append("|");
            String paswd = info.isPassword() ? "Yes" : "No";
            lineBuilder.append(paswd);
            lineBuilder.append(fillString(colWidth[1] - paswd.length(), ' '));

            lineBuilder.append("|");
            String glob = info.isGlobal() ? "Yes" : "No";
            lineBuilder.append(glob);
            lineBuilder.append(fillString(colWidth[2] - glob.length(), ' '));

            lineBuilder.append("|");
            lineBuilder.append(info.getRooms());
            lineBuilder.append(fillString(colWidth[3] - info.getRooms().length(), ' '));
            lineBuilder.append("|");
            result.add(lineBuilder.toString());
            result.add(separator);
        }


        return result.toArray(new String[result.size()]);

    }


    private List<User> getConnectedUsers() {
        List<User> result = new ArrayList<User>();
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