package com.kfyty.loveqq.framework.core.support.io;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.support.Triple;
import com.kfyty.loveqq.framework.core.thread.SingleThreadTask;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
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

import static java.util.Objects.requireNonNull;

/**
 * 描述: 文件变更监听任务，单例模式
 *
 * @author kfyty725
 * @date 2024/9/26 20:37
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class FileListenerTask extends SingleThreadTask {
    /**
     * 实例
     */
    public static final FileListenerTask INSTANCE = new FileListenerTask();

    /**
     * 监听队列
     */
    private final Map<String, Triple<Path, WatchService, FileListener.FileEventListener>> watchServices;

    private FileListenerTask() {
        super("file-listener");
        this.watchServices = new ConcurrentHashMap<>(4);
    }

    public FileListenerTask registry(Triple<Path, WatchService, FileListener.FileEventListener> watchService) {
        this.watchServices.putIfAbsent(watchService.getKey().toAbsolutePath().toString(), requireNonNull(watchService));
        return this;
    }

    public FileListenerTask remove(Path path) {
        return this.remove(path.toAbsolutePath().toString());
    }

    public FileListenerTask remove(String path) {
        this.watchServices.remove(path);
        return this;
    }

    @Override
    public void run() {
        super.run();
        this.watchServices.values().forEach(e -> IOUtil.close(e.getValue()));
    }

    @Override
    protected void sleep() {
        CommonUtil.sleep(100);
    }

    @Override
    protected void doRun() {
        try {
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
                    try {
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
                    } catch (Throwable e) {
                        log.error("file listener consumer error: {}", e.getMessage(), e);
                    }
                }
                if (!watchKey.reset()) {
                    throw new ResolvableException("Reset file listen watch key failed: " + watchService);
                }
            }
        } catch (InterruptedException e) {
            throw new ResolvableException("file listen task is interrupted.", e);
        } catch (Throwable e) {
            log.error("file listen task running error.", e);
        }
    }
}
