package com.rmb122.classfiledumper;

import com.rmb122.classfiledumper.utils.OS;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Main {
    public static final String MODE_LIST = "list";
    public static final String MODE_DUMP = "dump";

    public static String getJarPath() throws URISyntaxException {
        String jarPath = Agent.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        if (OS.isWindows() && jarPath.startsWith("/")) {
            // 在 windows 底下是 /C:/a/b/c/d, 需要去掉开头的 /
            jarPath = jarPath.substring(1);
        }
        return jarPath;
    }

    public static String getJarName() throws URISyntaxException {
        String name = getJarPath();
        return name.substring(name.lastIndexOf("/") + 1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java -jar " + getJarName() + " mode args...");
            System.out.println("\tAvailable mode: list, dump");
            System.out.println("\tDump example:");
            System.out.println("\t\tdump -p interfacesOrParents attachTarget outputBaseDir packagePattern");
            System.out.println("\t\tdump -p I1,I2 -p I3,I4 -p P1 -p P2 114514 dumps/ 'com\\.rmb122\\.demo\\..*'");
            return;
        }

        if (MODE_LIST.equals(args[0])) {
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            for (VirtualMachineDescriptor vm : vms) {
                System.out.printf("pid: %s\tcmdline: %s%n", vm.id(), vm.displayName());
            }
        } else if (MODE_DUMP.equals(args[0])) {
            ClassFileDumperConfig config = new ClassFileDumperConfig();

            config.premain = false;
            String attachTarget = null;

            for (int i = 1; i < args.length; i++) {
                if (args[i].equals("-p") && i + 1 < args.length) {
                    config.parents.add(new ArrayList<String>(Arrays.asList(args[i + 1].split(","))));
                    i++;
                } else if (attachTarget == null) {
                    attachTarget = args[i];
                } else if (config.outputBaseDir == null) {
                    config.outputBaseDir = new File(args[i]).getAbsolutePath();
                } else if (config.packagePattern == null) {
                    config.packagePattern = Pattern.compile(args[i]);
                } else {
                    System.out.println("too much argument given");
                    return;
                }
            }

            if (attachTarget == null) {
                System.out.println("you must specify a attachTarget");
                return;
            }
            if (config.outputBaseDir == null) {
                System.out.println("you must specify a outputBaseDir");
                return;
            }
            if (config.packagePattern == null) {
                config.packagePattern = Pattern.compile(".*");
            }

            VirtualMachine vm = VirtualMachine.attach(attachTarget);
            vm.loadAgent(Main.getJarPath(), config.serialize());
            vm.detach();
        } else {
            System.out.println(args[0] + " is not valid mode, available: list, dump");
        }
    }
}
