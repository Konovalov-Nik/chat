package ru.sgu.itcourses.chat.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * @author Konovalov_Nik
 */
public class ClientConnection extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(ClientConnection.class);
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private User user;

    public ClientConnection(Socket socket) {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            LOG.error("Cant get input stream", e);
            throw new RuntimeException(e);
        }
        try {
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            LOG.error("Cant get output stream", e);
            throw new RuntimeException(e);
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String s = in.readUTF();
                processCommand(s.trim());
            } catch (IOException e) {
                LOG.error("Cant read line from input", e);
            }
        }
    }

    private void processCommand(String s) {
        String[] splitted = s.split(" ");
        if ("connect".equals(splitted[0])) {
            String password;
            if (splitted.length < 3) {
                password = "";
            } else {
                password = splitted[2];
            }
            processConnect(splitted[1], password);
        } else {
            ServerCore.getInstance().addMessage(new Message(user.getLogin(), splitted));
        }
    }

    private void processConnect(String login, String password) {
        if (ServerCore.getInstance().isRegistered(login)) {
            User user = ServerCore.getInstance().checkPasswordAndGet(login, password);
            if (user != null) {
                this.user = user;
            } else {
                try {
                    out.writeUTF("Wrong password");
                } catch (IOException e) {
                    LOG.error("Cant send error message", e);
                }
            }
        } else {
            ServerCore.getInstance().register(login, password);
        }
    }
}
