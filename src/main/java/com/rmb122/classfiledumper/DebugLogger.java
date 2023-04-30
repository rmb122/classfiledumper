package com.rmb122.classfiledumper;

public class DebugLogger {
    private static final boolean debug = true;

    public static void printStackTrace(Throwable throwable) {
        if (debug) {
            throwable.printStackTrace();
        }
    }
}
