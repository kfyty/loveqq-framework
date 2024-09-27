package com.kfyty.loveqq.framework.core.support;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

/**
 * 描述: 文件变更监听任务，单例模式
 *
 * @author kfyty725
 * @date 2024/9/26 20:37
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class FileListenerTask implements Runnable {
    /**
     * 是否已启动
     */
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * 实例
     */
    public static final FileListenerTask INSTANCE = new FileListenerTask();

    /**
     * 监听队列
     */
    private final Map<String, Triple<Path, WatchService, FileListener.FileEventListener>> watchServices;

    private FileListenerTask() {
        this.watchServices = new ConcurrentHashMap<>(4);
    }

    public FileListenerTask registry(Triple<Path, WatchService, FileListener.FileEventListener> watchService) {
        if (!STARTED.get()) {
            this.start();
        }
        this.watchServices.putIfAbsent(watchService.getKey().toAbsolutePath().toString(), requireNonNull(watchService));
        synchronized (this) {
            this.notify();                                                                                              // 通知继续
        }
        return this;
    }

    public FileListenerTask remove(Path path) {
        return this.remove(path.toAbsolutePath().toString());
    }

    public FileListenerTask remove(String path) {
        this.watchServices.remove(path);
        return this;
    }

    public void start() {
        if (STARTED.compareAndSet(false, true)) {
            new Thread(this, "thread-file-listener").start();
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        } else {
            throw new ResolvableException("start FileListenerTask failed.");
        }
    }

    public void stop() {
        if (!STARTED.compareAndSet(true, false)) {
            throw new ResolvableException("close FileListenerTask failed.");
        }
    }

    @Override
    public void run() {
        while (STARTED.get()) {
            try {
                if (this.watchServices.isEmpty()) {
                    synchronized (this) {
                        this.wait(3000);                                                                     // 空的时候等待注册新的监听
                    }
                }
                for (Triple<Path, WatchService, FileListener.FileEventListener> watchService : this.watchServices.values()) {
                    WatchKey watchKey = watchService.getValue().poll(100, TimeUnit.MILLISECONDS);
                    if (watchKey == null) {
                        continue;
                    }
                    final boolean isDirectory = watchService.getKey().toFile().isDirectory();
                    final String targetFile = isDirectory ? null : watchService.getKey().getFileName().toString();
                    final List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                    for (WatchEvent<?> event : watchEvents) {
                        final Object context = event.context();
                        if (!(context instanceof Path)) {
                            continue;
                        }
                        final boolean isTargetFileEvent = !isDirectory && Objects.equals(targetFile, ((Path) context).getFileName().toString());
                        if ((isDirectory || isTargetFileEvent) && event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            watchService.getTriple().onCreate(watchService.getKey(), event);
                            continue;
                        }
                        if ((isDirectory || isTargetFileEvent) && event.kind() == StandardWatchEventKinds.OVERFLOW) {
                            watchService.getTriple().onOverflow(watchService.getKey(), event);
                            continue;
                        }
                        if ((isDirectory || isTargetFileEvent) && event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            watchService.getTriple().onModify(watchService.getKey(), event);
                            continue;
                        }
                        if ((isDirectory || isTargetFileEvent) && event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                            watchService.getTriple().onDelete(watchService.getKey(), event);
                            continue;
                        }
                    }
                    if (!watchKey.reset()) {
                        throw new ResolvableException("Reset failed: " + watchService);
                    }
                }
            } catch (InterruptedException e) {
                throw new ResolvableException("FileListenerTask is interrupted.");
            } catch (Exception e) {
                log.error("FileListenerTask error.", e);
            }
        }
        this.watchServices.values().forEach(e -> IOUtil.close(e.getValue()));
    }
}
