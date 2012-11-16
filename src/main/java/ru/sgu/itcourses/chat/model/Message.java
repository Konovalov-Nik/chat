package ru.sgu.itcourses.chat.model;

/**
 * @author Konovalov_Nik
 */
public class Message {
    private String from;
    private String[] values;

    public Message(String from, String[] values) {
        this.from = from;
        this.values = values;
    }

    public String getFrom() {
        return from;
    }

    public String[] getValue() {
        return values;
    }
}
