package com.kfyty.loveqq.framework.core.lang.instrument;

import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import lombok.SneakyThrows;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 描述: 基于类加载实现 {@link ClassFileTransformer} 的功能，子类在获取到字节码时，调用 {@link this#transform(String, byte[])} 即可
 * 将 {@link ClassFileTransformer} 的实现类的全限定名称配置在 META-INF/k.factories 下即可
 * 这里不使用 {@link com.kfyty.loveqq.framework.core.io.FactoriesLoader} 是为了减少类加载
 * eg: java.lang.instrument.ClassFileTransformer=com.kfyty.core.lang.LoggerClassFileTransformer
 *
 * @author kfyty725
 * @date 2023/4/17 16:32
 * @email kfyty725@hotmail.com
 */
public abstract class ClassFileTransformerClassLoader extends URLClassLoader {
    /**
     * @see this#obtainClassFileTransformer()
     */
    private volatile List<ClassFileTransformer> classFileTransformers;

    public ClassFileTransformerClassLoader(URL[] urls) {
        super(urls);
    }

    public ClassFileTransformerClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public ClassFileTransformerClassLoader(String name, URL[] urls, ClassLoader parent) {
        super(name, urls, parent);
    }

    public ClassFileTransformerClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    public ClassFileTransformerClassLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(name, urls, parent, factory);
    }

    /**
     * 执行自定义字节码逻辑
     */
    protected byte[] transform(String className, byte[] classBytes) throws ClassNotFoundException {
        if (!ConstantConfig.LOAD_TRANSFORMER) {
            return classBytes;
        }
        try {
            className = className.replace('.', '/');
            List<ClassFileTransformer> classFileTransformers = this.obtainClassFileTransformer();
            for (ClassFileTransformer classFileTransformer : classFileTransformers) {
                byte[] changed = classFileTransformer.transform(this, className, null, null, classBytes);
                classBytes = changed == null ? classBytes : changed;
            }
            return classBytes;
        } catch (IllegalClassFormatException e) {
            throw new ClassNotFoundException(className, e);
        }
    }

    protected List<ClassFileTransformer> obtainClassFileTransformer() throws ClassNotFoundException {
        if (this.classFileTransformers != null) {
            return this.classFileTransformers;
        }
        synchronized (ClassFileTransformerClassLoader.class) {
            if (this.classFileTransformers == null) {
                this.classFileTransformers = new LinkedList<>();
                Set<String> factories = this.loadClassFileTransformer(ClassFileTransformer.class.getName());
                for (String className : factories) {
                    try {
                        this.classFileTransformers.add((ClassFileTransformer) Class.forName(className).getDeclaredConstructor().newInstance());
                    } catch (ClassNotFoundException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new ClassNotFoundException(className, e);
                    }
                }
            }
            return this.classFileTransformers;
        }
    }

    @SneakyThrows(IOException.class)
    protected Set<String> loadClassFileTransformer(String key) {
        Set<String> transformers = new HashSet<>();
        Enumeration<URL> resources = this.getClass().getClassLoader().getResources("META-INF/k.factories");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            Properties properties = new Properties();
            properties.load(url.openStream());
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                if (entry.getKey().toString().equals(key)) {
                    transformers.addAll(Arrays.asList(entry.getValue().toString().split(",")));
                }
            }
        }
        return transformers;
    }
}
