package com.rmb122.classfiledumper.utils;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Serialize {
    public static void writeInteger(StringBuilder stringBuilder, Integer integer) {
        stringBuilder.append(integer);
        stringBuilder.append("|");
    }

    public static void writeString(StringBuilder stringBuilder, String string) {
        if (string != null) {
            writeInteger(stringBuilder, string.length());
            stringBuilder.append(string);
        } else {
            stringBuilder.append('-');
        }
    }

    public static void writeStringArray(StringBuilder stringBuilder, List<String> stringArray) {
        writeInteger(stringBuilder, stringArray.size());
        for (String string : stringArray) {
            writeString(stringBuilder, string);
        }
    }

    public static void writeBoolean(StringBuilder stringBuilder, boolean value) {
        stringBuilder.append(value ? "+" : "-");
    }

    public static Integer readInteger(StringCharacterIterator iterator) {
        int integer = 0;
        while (iterator.current() != '|') {
            char curr = iterator.current();
            if (curr <= '9' && curr >= '0') {
                integer *= 10;
                integer += curr - '0';
            } else {
                throw new RuntimeException("not valid serialize string");
            }
            iterator.next();
        }
        iterator.next();
        return integer;
    }

    public static String readString(StringCharacterIterator iterator) {
        if (iterator.current() == '-') {
            iterator.next();
            return null;
        } else {
            int stringLength = readInteger(iterator);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < stringLength; i++) {
                stringBuilder.append(iterator.current());
                iterator.next();
            }
            return stringBuilder.toString();
        }
    }

    public static ArrayList<String> readStringArray(StringCharacterIterator iterator) {
        ArrayList<String> stringArray = new ArrayList<String>();
        int arrayLength = readInteger(iterator);
        for (int i = 0; i < arrayLength; i++) {
            stringArray.add(readString(iterator));
        }
        return stringArray;
    }

    public static boolean readBoolean(StringCharacterIterator iterator) {
        char curr = iterator.next();
        return curr == '+';
    }

    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        writeStringArray(stringBuilder, Arrays.asList("x11|asdasdas", "123", "asasdasdasdasdsadasdasdasdasdsadasd", null, "asdas"));

        String output = stringBuilder.toString();
        System.out.println(output);

        StringCharacterIterator iterator = new StringCharacterIterator(output);
        System.out.println(readStringArray(iterator));
    }
}
