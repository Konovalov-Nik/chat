package ru.sgu.itcourses.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Nikita Konovalov
 */
public class ClientRecieveThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(ClientRecieveThread.class);
    private DataInputStream input;

    public ClientRecieveThread(DataInputStream input) {
        this.input = input;
    }

    @Override
    public void run() {
        while(true) {
            if (Thread.interrupted()) {
                try {
                    input.close();
                } catch (IOException e) {

                }
                break;
            }
            try {
                String s = input.readUTF();
                System.out.println(s);
            } catch (IOException e) {
                LOG.debug("Reciever connection dropped.");
                break;
            }
        }
    }
}
