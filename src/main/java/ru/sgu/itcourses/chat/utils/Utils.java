package ru.sgu.itcourses.chat.utils;

/**
 * @author Konovalov Nikita
 */
public class Utils {
    public static String fillString(int w, char c) {
        return new String(new char[w]).replace('\0', c);
    }
}
