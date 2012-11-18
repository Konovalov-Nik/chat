package ru.sgu.itcourses.chat.utils;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Konovalov_Nik
 */
public class SynchronizedDataOutputStream {
    private DataOutputStream outputStream;

    public SynchronizedDataOutputStream(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public synchronized void writeUTF(String line) throws IOException {
        outputStream.writeUTF(line);
    }

    public synchronized void writeUTF(String[] lines) throws IOException {
        for (String line : lines) {
            outputStream.writeUTF(line);
        }
    }
}
