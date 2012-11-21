package ru.sgu.itcourses.chat.utils;

import ru.sgu.itcourses.chat.model.Channel;
import ru.sgu.itcourses.chat.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konovalov Nikita
 */
public class Utils {
    public static String fillString(int w, char c) {
        if (w <= 0) {
            return "";
        }
        return new String(new char[w]).replace('\0', c);
    }

    public static String[] makeUsersListResponse(List<User> aliveUsers) {
        List<UserInfo> infos = new ArrayList<UserInfo>();
        String[] headers = {"Nickname", "Password", "Global", "Rooms Connected"};
        int maxNameWidth = headers[0].length();
        int maxRoomsWidth = headers[3].length();
        for (User user : aliveUsers) {
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

    public static String[] makeRoomsListResponse(List<Channel> registeredChannels) {

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

    public static String[] getHelp() {
        List<String> result = new ArrayList<>();
        result.add("\t connect <host:port> <username> [<password>]   -   connect to a server or register on it if connected for the first time.");
        result.add("\t disconnect   -   disconnect from current server.");
        result.add("");
        result.add("\t msg @<username>   -   send private message to user.");
        result.add("\t msg #<roomname>   -   send message to all users in a room.");
        result.add("");
        result.add("\t join <roomname> [<password>]   -   join a room or create if room does not exist.");
        result.add("\t leave <roomname>   -   leave room.");
        result.add("");
        result.add("\t list users   -   get list of on-line users on server and rooms they have joined.");
        result.add("\t list rooms   -   get rooms registered on server and number of users in each.");
        result.add("");
        result.add("\t help   -   prints this help.");
        return result.toArray(new String[result.size()]);
    }
}
