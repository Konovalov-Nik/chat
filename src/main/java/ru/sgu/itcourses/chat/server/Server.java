package ru.sgu.itcourses.chat.server;

import ru.sgu.itcourses.chat.server.ServerCore;

/**
 * @author Konovalov_Nik
 */
public class Server {
    public static void main(String[] args) {
        AcceptorThread acceptorThread = new AcceptorThread(5000);
        acceptorThread.start();
        ServerCore.getInstance().startProcessors();
    }
}
