package com.rmb122.classfiledumper;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.regex.Pattern;

public class Agent {
    public static void agentmain(String serializeConfig, Instrumentation instrumentation) {
        ClassFileDumper dumper = ClassFileDumper.registerClassfileDumper(instrumentation);
        ClassFileDumperConfig config = ClassFileDumperConfig.deserialize(serializeConfig);
        dumper.dump(config);
        dumper.unregisterClassfileDumper();
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        ClassFileDumper dumper = ClassFileDumper.registerClassfileDumper(instrumentation);
        String[] args = agentArgs.split(":", 2);

        ClassFileDumperConfig config = new ClassFileDumperConfig();
        config.premain = true;
        config.outputBaseDir = new File(args[0]).getAbsolutePath();

        if (args.length == 2) {
            config.packagePattern = Pattern.compile(args[1]);
        } else {
            config.packagePattern = Pattern.compile(".*");
        }

        dumper.dump(config);
    }
}
