package ru.sgu.itcourses.chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sgu.itcourses.chat.model.ClientConnection;
import ru.sgu.itcourses.chat.model.ServerCore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Konovalov_Nik
 */
public class AcceptorThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(AcceptorThread.class);
    private int port;
    private ServerSocket serverSocket;

    public AcceptorThread(int port) {
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            LOG.error("Cant bind to port.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        LOG.info("Server started on port " + port);
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ClientConnection connection = new ClientConnection(socket);
                connection.start();
                ServerCore.getInstance().addConnection(connection);
                LOG.info("New client connected");
            } catch (IOException e) {
                LOG.error("Error while accepting a client.", e);
            }
        }
    }
}
