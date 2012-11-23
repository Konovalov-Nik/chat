package ru.sgu.itcourses.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * @author Nikita Konovalov
 */
public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private static final Client instance = new Client();
    private Socket socket;
    private ClientRecieveThread recieveThread;
    private ClientSendThread sendThread;

    private Client() {}

    public static Client getInstance() {
        return instance;
    }

    private void start() {
        sendThread = new ClientSendThread();
        sendThread.start();
        System.out.println("Type 'connect <host:port> <username> [<password>]' to connect to a server.");
    }

    public static void main(String[] args) {
        getInstance().start();
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void startReciever(Socket socket) {
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            if (recieveThread != null) {
                recieveThread.interrupt();
            }
            recieveThread = new ClientRecieveThread(inputStream);
            recieveThread.setDaemon(true);
            recieveThread.start();
        } catch (IOException e) {
            LOG.error("Cant get input stream");
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
