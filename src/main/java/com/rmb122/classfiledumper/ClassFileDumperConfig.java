package com.rmb122.classfiledumper;

import com.rmb122.classfiledumper.utils.Serialize;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ClassFileDumperConfig {
    public boolean premain;
    public String outputBaseDir;
    public Pattern packagePattern;
    public ArrayList<ArrayList<String>> parents = new ArrayList<ArrayList<String>>();

    public String serialize() {
        StringBuilder stringBuilder = new StringBuilder();
        Serialize.writeBoolean(stringBuilder, premain);
        Serialize.writeString(stringBuilder, outputBaseDir);
        if (packagePattern == null) {
            Serialize.writeString(stringBuilder, null);
        } else {
            Serialize.writeString(stringBuilder, packagePattern.toString());
        }

        Serialize.writeInteger(stringBuilder, parents.size());
        for (ArrayList<String> anInterface : parents) {
            Serialize.writeStringArray(stringBuilder, anInterface);
        }
        return stringBuilder.toString();
    }

    public static ClassFileDumperConfig deserialize(String serializedConfig) {
        StringCharacterIterator iterator = new StringCharacterIterator(serializedConfig);
        ClassFileDumperConfig classFileDumperConfig = new ClassFileDumperConfig();

        classFileDumperConfig.premain = Serialize.readBoolean(iterator);
        String outputBaseDir = Serialize.readString(iterator);
        if (outputBaseDir == null) {
            classFileDumperConfig.outputBaseDir = "./dumps/";
        } else {
            classFileDumperConfig.outputBaseDir = outputBaseDir;
        }

        String patternString = Serialize.readString(iterator);
        if (patternString == null) {
            classFileDumperConfig.packagePattern = Pattern.compile(".*");
        } else {
            classFileDumperConfig.packagePattern = Pattern.compile(patternString);
        }

        int arraySize = Serialize.readInteger(iterator);
        for (int i = 0; i < arraySize; i++) {
            classFileDumperConfig.parents.add(Serialize.readStringArray(iterator));
        }
        return classFileDumperConfig;
    }

    public static void main(String[] args) {
        ClassFileDumperConfig config = new ClassFileDumperConfig();
        config.outputBaseDir = "/tmp/dumps";

        System.out.println(config.serialize());
        ClassFileDumperConfig.deserialize(config.serialize());
    }
}
