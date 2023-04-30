package com.rmb122.classfiledumper;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void agentmain(String serializeConfig, Instrumentation instrumentation) {
        ClassFileDumper dumper = ClassFileDumper.registerClassfileDumper(instrumentation);
        ClassFileDumperConfig config = ClassFileDumperConfig.deserialize(serializeConfig);
        dumper.dump(config);
        dumper.unregisterClassfileDumper();
    }
}
