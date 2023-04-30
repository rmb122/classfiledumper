package com.rmb122.classfiledumper;

import com.rmb122.classfiledumper.utils.Rand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClassFileDumper implements ClassFileTransformer {
    private static ClassFileDumper theClassFileDumper = null;
    private final Instrumentation instrumentation;
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private ClassFileDumperConfig currConfig;
    private Set<Class<?>> waitingClasses;

    private ClassFileDumper(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public static synchronized ClassFileDumper registerClassfileDumper(Instrumentation instrumentation) {
        if (theClassFileDumper == null) {
            theClassFileDumper = new ClassFileDumper(instrumentation);
        }
        instrumentation.addTransformer(theClassFileDumper, true);
        return theClassFileDumper;
    }

    public synchronized void unregisterClassfileDumper() {
        // 每次 dump 后删除, 这样每次 transformer 都放在 transformer list 的最后面
        this.instrumentation.removeTransformer(this);
    }

    public void dump(ClassFileDumperConfig config) {
        synchronized (this.enabled) {
            this.currConfig = config;
            this.waitingClasses = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());

            for (Class<?> clazz : this.instrumentation.getAllLoadedClasses()) {
                if (this.instrumentation.isModifiableClass(clazz)) {
                    ClassLoader classLoader = clazz.getClassLoader();
                    if (classLoader == null) {
                        classLoader = ClassLoader.getSystemClassLoader();
                    }

                    boolean matched = false;
                    for (ArrayList<String> parentNames : currConfig.parents) {
                        boolean currSlotMatched = true;
                        // slot 中需要每一个都符合
                        for (String parentName : parentNames) {
                            try {
                                Class<?> parent = classLoader.loadClass(parentName);
                                if (!parent.isAssignableFrom(clazz)) {
                                    currSlotMatched = false;
                                    break;
                                }
                            } catch (ClassNotFoundException ignored) {
                                currSlotMatched = false;
                                break;
                            }
                        }

                        if (currSlotMatched) {
                            // 如果符合任意一个, break
                            matched = true;
                            break;
                        }
                    }

                    // 没有使用 -p 选项, 或者满足 -p 之一
                    if (currConfig.parents.size() == 0 || matched) {
                        if (this.currConfig.packagePattern.matcher(clazz.getName()).matches()) {
                            waitingClasses.add(clazz);
                        }
                    }
                }
            }

            this.enabled.set(true);

            HashSet<Class<?>> waitingClassesCopy = new HashSet<Class<?>>(waitingClasses);
            for (Class<?> clazz : waitingClassesCopy) {
                try {
                    // retransformClasses 是支持批量 retransform 的
                    // 但是在某些情况下会出现 java.lang.InternalError: class redefinition failed: invalid class, 甚至 jvm 崩溃, 原因未知
                    // https://bugs.openjdk.org/browse/JDK-8220783
                    // https://github.com/openjdk/jdk11u/blob/cd29d6e96fad6c3f8fb849477ff2ec5f337a990c/src/hotspot/share/prims/jvmtiEnv.cpp#L448-L450
                    // 导致其他的 retransform 失败, 所以还是一个个 retransform
                    // 可以通过 -noverify 启动被 dump 应用来缓解部分问题
                    this.instrumentation.retransformClasses(clazz);
                } catch (Throwable e) {
                    DebugLogger.printStackTrace(e);
                }
            }

            this.enabled.set(false);
        }
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> clazz, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {
        if (this.enabled.get() && className != null) {
            if (waitingClasses.remove(clazz)) {
                if (className.contains("/")) {
                    new File(this.currConfig.outputBaseDir, className.substring(0, className.lastIndexOf('/'))).mkdirs();
                }

                File classFile = new File(this.currConfig.outputBaseDir, className + ".class");
                if (classFile.exists()) {
                    // 避免重名的类覆盖
                    classFile = new File(this.currConfig.outputBaseDir, className + "." + Rand.randomString() + ".class");
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(classFile);
                    fos.write(classBytes);
                } catch (FileNotFoundException e) {
                    DebugLogger.printStackTrace(e);
                } catch (IOException e) {
                    DebugLogger.printStackTrace(e);
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            DebugLogger.printStackTrace(e);
                        }
                    }
                }
            }
        }
        return null;
    }
}
