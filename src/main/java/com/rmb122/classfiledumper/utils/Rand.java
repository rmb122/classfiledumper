package com.rmb122.classfiledumper.utils;

import java.util.Random;

public class Rand {
    private static final Random theRandom = new Random();

    public static String randomString() {
        int random = theRandom.nextInt();
        if (random < 0) {
            random = -random;
        }
        return Integer.toString(random);
    }
}
