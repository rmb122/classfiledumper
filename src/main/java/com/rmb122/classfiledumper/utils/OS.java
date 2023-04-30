package com.rmb122.classfiledumper.utils;

public class OS {
    private static Boolean isWindows = null;

    public static boolean isWindows() {
        if (isWindows == null) {
            String osName = System.getProperty("os.name");
            isWindows = (osName != null && osName.toLowerCase().contains("win"));
        }
        return isWindows;
    }
}
