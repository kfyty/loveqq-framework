package com.kfyty.loveqq.framework.core.support;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 描述: 文件监听器，支持监听单个文件及文件夹
 *
 * @author kfyty725
 * @date 2024/9/26 20:37
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class FileListener {
    /**
     * 要监听的路径
     */
    private final Path path;

    /**
     * 监听服务
     */
    private final WatchService watchService;

    /**
     * 事件监听器
     */
    private final FileEventListener eventListener;

    /**
     * 创建事件
     */
    private BiConsumer<Path, WatchEvent<?>> onCreate;

    /**
     * 溢出事件
     */
    private BiConsumer<Path, WatchEvent<?>> onOverflow;

    /**
     * 修改事件
     */
    private BiConsumer<Path, WatchEvent<?>> onModify;

    /**
     * 删除事件
     */
    private BiConsumer<Path, WatchEvent<?>> onDelete;

    /**
     * 必须单独设置对应事件
     *
     * @param path 监听的文件路径
     */
    public FileListener(Path path) {
        this.path = path;
        this.watchService = IOUtil.newWatchService();
        this.eventListener = new FileEventListener() {

            @Override
            public void onCreate(Path path, WatchEvent<?> event) {
                Mapping.from(onCreate).whenNotNull(e -> e.accept(path, event));
            }

            @Override
            public void onOverflow(Path path, WatchEvent<?> event) {
                Mapping.from(onOverflow).whenNotNull(e -> e.accept(path, event));
            }

            @Override
            public void onModify(Path path, WatchEvent<?> event) {
                Mapping.from(onModify).whenNotNull(e -> e.accept(path, event));
            }

            @Override
            public void onDelete(Path path, WatchEvent<?> event) {
                Mapping.from(onDelete).whenNotNull(e -> e.accept(path, event));
            }
        };
    }

    /**
     * 构造器
     *
     * @param path          监听的文件路径
     * @param eventListener 监听器
     */
    public FileListener(Path path, FileEventListener eventListener) {
        this.path = path;
        this.eventListener = eventListener;
        this.watchService = IOUtil.newWatchService();
    }

    public FileListener register(WatchEvent.Kind<?>... events) {
        try {
            if (this.path.toFile().isDirectory()) {
                this.path.register(this.watchService, events);
            } else {
                this.path.toAbsolutePath().getParent().register(this.watchService, events);
            }
            return this;
        } catch (IOException e) {
            throw new ResolvableException(e);
        }
    }

    public FileListener onCreate(BiConsumer<Path, WatchEvent<?>> consumer) {
        if (this.onCreate != null) {
            throw new ResolvableException("On create is already set for path: " + this.path);
        }
        this.onCreate = Objects.requireNonNull(consumer);
        return this;
    }

    public FileListener onOverflow(BiConsumer<Path, WatchEvent<?>> consumer) {
        if (this.onOverflow != null) {
            throw new ResolvableException("On overflow is already set for path: " + this.path);
        }
        this.onOverflow = Objects.requireNonNull(consumer);
        return this;
    }

    public FileListener onModify(BiConsumer<Path, WatchEvent<?>> consumer) {
        if (this.onModify != null) {
            throw new ResolvableException("On modify is already set for path: " + this.path);
        }
        this.onModify = Objects.requireNonNull(consumer);
        return this;
    }

    public FileListener onDelete(BiConsumer<Path, WatchEvent<?>> consumer) {
        if (this.onDelete != null) {
            throw new ResolvableException("On delete is already set for path: " + this.path);
        }
        this.onDelete = Objects.requireNonNull(consumer);
        return this;
    }

    public void start() {
        FileListenerTask.INSTANCE.registry(new Triple<>(this.path, this.watchService, this.eventListener));
    }

    public void stop() {
        FileListenerTask.INSTANCE.remove(this.path);
    }

    public interface FileEventListener {
        /**
         * 创建事件
         *
         * @param path  path
         * @param event 事件
         */
        default void onCreate(Path path, WatchEvent<?> event) {

        }

        /**
         * 溢出事件
         *
         * @param path  path
         * @param event 事件
         */
        default void onOverflow(Path path, WatchEvent<?> event) {

        }

        /**
         * 更新事件
         *
         * @param path  path
         * @param event 事件
         */
        default void onModify(Path path, WatchEvent<?> event) {

        }

        /**
         * 删除事件
         *
         * @param path  path
         * @param event 事件
         */
        default void onDelete(Path path, WatchEvent<?> event) {

        }
    }
}
