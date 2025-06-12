package com.kfyty.async;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Async;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.kfyty.loveqq.framework.core.event.ContextRefreshedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/26 12:07
 * @email kfyty725@hotmail.com
 */
@EventListener
@BootApplication
public class AsyncProxyTest {
    @Autowired
    private AsyncService asyncService;

    @Test
    public void test() throws Exception {
        K.run(AsyncProxyTest.class);
    }

    @EventListener
    public void onEvent(ContextRefreshedEvent event) throws Exception {
        asyncService.test();

        Assertions.assertEquals(asyncService.testFuture().get(), 1);

        Assertions.assertEquals(asyncService.testCompletionStage().toCompletableFuture().get(), 1);
    }

    @Async
    @Component
    static class AsyncService {

        @Async
        void test() {
            System.out.println(Thread.currentThread().getName());
        }

        @Async
        Future<Integer> testFuture() {
            return CompletableFuture.completedFuture(1);
        }

        @Async
        CompletionStage<Integer> testCompletionStage() {
            return CompletableFuture.completedStage(1);
        }
    }
}
