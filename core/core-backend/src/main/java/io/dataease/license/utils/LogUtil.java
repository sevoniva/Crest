package io.dataease.license.utils;

public class LogUtil {
    public static void info(String msg) {
        System.out.println(msg);
    }

    public static void error(String msg) {
        System.err.println(msg);
    }

    public static void error(String msg, Throwable e) {
        System.err.println(msg);
        if (e != null) {
            e.printStackTrace();
        }
    }
}
