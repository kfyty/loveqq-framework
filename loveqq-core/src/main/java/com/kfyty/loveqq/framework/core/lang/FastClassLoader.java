package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

/**
 * 描述: 快速类加载器
 *
 * @author kfyty725
 * @date 2023/4/17 16:32
 * @email kfyty725@hotmail.com
 */
public class FastClassLoader extends URLClassLoader {
    /**
     * {@link ClassLoader#parallelLockMap} 同一个引用
     */
    protected Map<String, Object> parallelLockMap;

    /**
     * 注册并行能力
     */
    static {
        registerAsParallelCapable();
    }

    public FastClassLoader(URL[] urls) {
        super(urls);
        this.init();
    }

    public FastClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.init();
    }

    public FastClassLoader(String name, URL[] urls, ClassLoader parent) {
        super(name, urls, parent);
        this.init();
    }

    public FastClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        this.init();
    }

    public FastClassLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(name, urls, parent, factory);
        this.init();
    }

    /**
     * 初始化
     */
    protected void init() {
        try {
            Method method = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);

            method.setAccessible(true);
            Field[] fields = (Field[]) method.invoke(ClassLoader.class, false);
            method.setAccessible(false);

            int completed = 0;
            for (Field field : fields) {
                if (field.getName().equals("parallelLockMap")) {
                    field.setAccessible(true);
                    this.parallelLockMap = (Map<String, Object>) field.get(this);
                    field.setAccessible(false);
                    completed++;
                }
                if (field.getName().equals("classes")) {
                    field.setAccessible(true);
                    field.set(this, field.getType() == Vector.class ? new EmptyVector<>() : new EmptyArrayList<>());
                    field.setAccessible(false);
                    completed++;
                }
                if (completed >= 2) {
                    break;
                }
            }
        } catch (Throwable e) {
            throw new ResolvableException(e);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (this.parallelLockMap.get(name) instanceof Class<?> clazz) {
            return clazz;
        }
        return this.afterLoadClass(name, super.loadClass(name, resolve));
    }

    /**
     * 将 class 放入 {@link this#parallelLockMap}
     *
     * @param name  类名称
     * @param clazz class
     * @return class
     */
    protected Class<?> afterLoadClass(String name, Class<?> clazz) {
        this.parallelLockMap.put(name, clazz);
        return clazz;
    }

    /**
     * 读取数据到字节数组
     *
     * @param in 输入流
     * @return 字节数组
     */
    protected static byte[] read(InputStream in) throws IOException {
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
        /**
         * 构造器
         */
        public FastByteArrayOutputStream() {
            super();
        }

        /**
         * 构造器
         */
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

    /**
     * 空数组，不会添加元素
     */
    protected static class EmptyArrayList<E> extends ArrayList<E> {

        @Override
        public boolean add(E e) {
            return true;
        }

        @Override
        public void add(int index, E element) {

        }

        @Override
        public void addFirst(E element) {

        }

        @Override
        public void addLast(E element) {

        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return true;
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            return true;
        }
    }

    /**
     * 空数组，不会添加元素
     */
    protected static class EmptyVector<E> extends Vector<E> {

        @Override
        public void addElement(E obj) {

        }

        @Override
        public void addLast(E e) {

        }

        @Override
        public void addFirst(E e) {

        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return true;
        }

        @Override
        public void add(int index, E element) {

        }

        @Override
        public boolean add(E e) {
            return true;
        }
    }
}
