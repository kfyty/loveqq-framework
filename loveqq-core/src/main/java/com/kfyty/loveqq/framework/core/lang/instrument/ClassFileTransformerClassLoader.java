package com.kfyty.loveqq.framework.core.lang.instrument;

import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
     * 注册并行能力
     */
    static {
        try {
            registerAsParallelCapable();
        } catch (Throwable e) {
            // ignored
        }
    }

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
     *
     * @param className  class name, eg: java/util/List
     * @param classBytes class byte code
     */
    protected byte[] transform(String className, byte[] classBytes) throws ClassNotFoundException {
        if (!ConstantConfig.LOAD_TRANSFORMER) {
            return classBytes;
        }
        try {
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

    /**
     * 读取数据到字节数组
     *
     * @param in 输入流
     * @return 字节数组
     */
    protected byte[] read(InputStream in) throws IOException {
        // 确定缓冲区大小
        int n = -1;
        int available = in.available();
        byte[] buffer = new byte[available > 0 ? available : ConstantConfig.IO_STREAM_READ_BUFFER_SIZE];

        // 开始读取
        available = 0;
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(buffer.length);
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
            available += n;
        }
        return out.toByteArray(available);
    }

    /**
     * 快速字节输出流，主要用于类加载，避免字节数组的复制
     */
    protected static class FastByteArrayOutputStream extends ByteArrayOutputStream {

        public FastByteArrayOutputStream() {
        }

        public FastByteArrayOutputStream(int size) {
            super(size);
        }

        /**
         * 当请求字节数组大小与缓冲区大小一致时，直接返回，而不复制
         * 在类加载时很有用，因为类加载一般可以预知字节数组的大小
         *
         * @param targetSize 请求的字节数组大小
         * @return 字节数组
         */
        public byte[] toByteArray(int targetSize) {
            if (targetSize == buf.length) {
                return buf;
            }
            return Arrays.copyOf(buf, count);
        }
    }
}
