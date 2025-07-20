package com.kfyty.loveqq.framework.core.utils.reactor;

import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 描述: 响应式工具
 *
 * @author kfyty725
 * @date 2023/11/15 17:50
 * @email kfyty725@hotmail.com
 */
public abstract class ReactiveUtil {
    /**
     * 堵塞获取发布者的值，避免出现 {@link IllegalStateException}
     *
     * @param mono 发布者
     * @return 值
     */
    public static <T> T block(Mono<T> mono) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> reference = new AtomicReference<>();

        mono.subscribe(e -> {
            reference.set(e);
            latch.countDown();
        });

        try {
            latch.await();
            return reference.get();
        } catch (InterruptedException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 堵塞消费发布者的值，避免出现 {@link IllegalStateException}
     *
     * @param mono     发布者
     * @param consumer 消费者
     */
    public static <T> void block(Mono<T> mono, Consumer<T> consumer) {
        consumer.accept(block(mono));
    }
}
