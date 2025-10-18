package com.kfyty.loveqq.framework.core.lang.agent;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.util.EnumerationIterator;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.sun.tools.attach.VirtualMachine;
import javassist.ClassPool;
import javassist.CtClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * 描述: 热部署
 *
 * @author kfyty725
 * @date 2023/4/17 16:32
 * @email kfyty725@hotmail.com
 */
public class HotSwapAgent {
    /**
     * {@link Instrumentation}
     */
    private static Instrumentation instrumentation = null;

    /**
     * premain
     */
    public static void premain(String agentArgs, Instrumentation inst) throws Throwable {
        agentmain(agentArgs, inst);
    }

    /**
     * agentmain
     */
    public static void agentmain(String agentArgs, Instrumentation inst) throws Throwable {
        if (!inst.isRedefineClassesSupported()) {
            throw new RuntimeException("This JVM doesn't support redefinition of classes.");
        }
        HotSwapAgent.instrumentation = inst;
    }

    public static void startAgent() {
        if (instrumentation == null) {
            startAgent(createAgentJarFile(HotSwapAgent.class));
        }
    }

    public static void startAgent(File agentJar) {
        if (instrumentation != null) {
            return;
        }

        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        String pid = vmName.substring(0, vmName.indexOf('@'));

        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(agentJar.getAbsolutePath(), null);
            vm.detach();
        } catch (Exception e) {
            throw new ResolvableException("Hotswap attach agent failed: " + agentJar.getAbsolutePath(), e);
        }

        for (int sec = 0; sec < 100; sec++) {
            if (instrumentation != null) {
                return;
            }
            CommonUtil.sleep(100);
        }

        throw new ResolvableException("Hotswap agent (timeout)");
    }

    /**
     * 重定义 class
     *
     * @param oldClass 旧的 class
     * @param newClass 新的 class 字节码
     */
    public static void redefine(Class<?> oldClass, byte[] newClass) {
        startAgent();
        try {
            HotSwapAgent.instrumentation.redefineClasses(new ClassDefinition(oldClass, newClass));
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            throw new ResolvableException("Redefine classes failed.", e);
        }
    }

    /**
     * 重定义 class
     *
     * @param classes 旧的 class 以及对应的字节码
     */
    public static void redefine(List<Pair<Class<?>, byte[]>> classes) {
        if (classes.isEmpty()) {
            return;
        }

        startAgent();

        ClassDefinition[] classDefinitions = new ClassDefinition[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            Pair<Class<?>, byte[]> classPair = classes.get(i);
            classDefinitions[i] = new ClassDefinition(classPair.getKey(), classPair.getValue());
        }

        try {
            HotSwapAgent.instrumentation.redefineClasses(classDefinitions);
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            throw new ResolvableException("Redefine classes failed.", e);
        }
    }

    /***
     * 重定义 class
     * @param jarFile jar file
     */
    public static void redefine(JarFile jarFile) {
        List<Pair<Class<?>, byte[]>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (JarEntry jarEntry : new EnumerationIterator<>(jarFile.entries())) {
            String name = jarEntry.getName();
            if (name.endsWith(".class")) {
                try (InputStream stream = jarFile.getInputStream(jarEntry)) {
                    Class<?> clazz = classLoader.loadClass(name.substring(0, name.length() - 6).replace('/', '.'));
                    classes.add(new Pair<>(clazz, IOUtil.read(stream)));
                } catch (ClassNotFoundException e) {
                    // ignored
                } catch (IOException e) {
                    throw new ResolvableException("Redefine classes failed: " + name, e);
                }
            }
        }
        redefine(classes);
    }

    /***
     * 重定义 class
     * @param jarFile jar file
     */
    public static void redefine(Collection<JarFile> jarFile) {
        for (JarFile file : jarFile) {
            redefine(file);
        }
    }

    /**
     * 创建 agent jar file
     *
     * @param agentClass agent class
     */
    public static File createAgentJarFile(Class<?> agentClass) {
        try {
            // 创建临时文件
            File jar = File.createTempFile("agent", ".jar");
            jar.deleteOnExit();

            // 创建 Manifest 文件
            Manifest manifest = new Manifest();
            Attributes attrs = manifest.getMainAttributes();
            attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            attrs.put(new Attributes.Name("Premain-Class"), agentClass.getName());
            attrs.put(new Attributes.Name("Agent-Class"), agentClass.getName());
            attrs.put(new Attributes.Name("Can-Retransform-Classes"), "true");
            attrs.put(new Attributes.Name("Can-Redefine-Classes"), "true");

            try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar), manifest)) {
                String className = agentClass.getName();
                CtClass clazz = ClassPool.getDefault().get(className);
                JarEntry jarEntry = new JarEntry(className.replace('.', '/') + ".class");
                jos.putNextEntry(jarEntry);
                jos.write(clazz.toBytecode());
                jos.closeEntry();
                return jar;
            }
        } catch (Throwable e) {
            throw new RuntimeException("Create agent jar file failed: " + agentClass, e);
        }
    }
}
