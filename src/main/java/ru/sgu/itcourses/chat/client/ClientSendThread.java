package ru.sgu.itcourses.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sgu.itcourses.chat.utils.SynchronizedDataOutputStream;

import javax.swing.text.StringContent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Nikita Konovalov
 */
public class ClientSendThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(ClientSendThread.class);
    private Scanner scanner = new Scanner(System.in);
    private SynchronizedDataOutputStream output;

    @Override
    public void run() {
        while (true) {
            String cmd = scanner.nextLine();
            if (cmd.startsWith("connect")) {
                setupConnection(cmd);
                continue;
            }

            try {
                output.writeUTF(cmd);
                if (cmd.startsWith("disconnect")) {
                    System.out.println("Connection closed.");
                    Client.getInstance().getSocket().close();
                }
            } catch (IOException e) {
                LOG.info("Connection closed.");
            }
        }
    }

    private void setupConnection(String cmd) {
        String[] args = cmd.split(" ");
        String host = args[1].split(":")[0];
        int port = Integer.valueOf(args[1].split(":")[1]);
        String login = args[2];
        String password = "";
        if (args.length > 3) {
            password = args[3];
        }
        try {
            Socket socket = new Socket(host, port);
            System.out.println("Connected");
            output = new SynchronizedDataOutputStream(new DataOutputStream(socket.getOutputStream()));
            output.writeUTF("connect " + login + " " + password);
            Client.getInstance().setSocket(socket);
            Client.getInstance().startReciever(socket);
        } catch (IOException e) {
            LOG.warn("Cant connect to " + host + ":" + port);
        }
    }

    private class PingThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (output != null) {
                    try {
                        output.writeUTF("ping");
                        Thread.sleep(1000);
                    } catch (IOException e) {
                        break;
                    } catch (InterruptedException e) {
                        //
                    }
                }
            }
        }
    }
}
