package ru.sgu.itcourses.chat.utils;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Konovalov_Nik
 */
public class SynchronizedDataInputStream {
    private DataInputStream inputStream;

    public SynchronizedDataInputStream(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public synchronized String readUTF() throws IOException {
        return inputStream.readUTF();
    }
}
