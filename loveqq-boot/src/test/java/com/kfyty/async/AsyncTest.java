package com.kfyty.async;

import com.kfyty.loveqq.framework.boot.K;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.SerialInitialize;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Async;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationEventPublisher;
import com.kfyty.loveqq.framework.core.event.ContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/26 12:07
 * @email kfyty725@hotmail.com
 */
@Getter
@EventListener
@BootApplication
public class AsyncTest implements ContextAfterRefreshed {
    int[] async = new int[2];
    CountDownLatch latch = new CountDownLatch(1);
    AtomicInteger index = new AtomicInteger(0);

    @Autowired
    private AsyncTaskImpl asyncTask;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void asyncTest() {
        K.run(AsyncTest.class);
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        applicationEventPublisher.publishEvent(new AsyncEvent(2));
        Assertions.assertSame(this.asyncTask.getAsyncTest().getLatch(), latch);
        this.async[index.getAndIncrement()] = 1;
    }

    @SneakyThrows
    @EventListener
    public void onContextRefreshed(ContextRefreshedEvent event) {
        latch.await();
        Assertions.assertEquals(this.async[0], 1);
        Assertions.assertEquals(this.async[1], 2);
    }
}

interface AsyncTask {
    void onAsyncEvent(AsyncEvent asyncEvent);
}

class AsyncEvent extends ApplicationEvent<Integer> {

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public AsyncEvent(Integer source) {
        super(source);
    }
}

@Async
@Getter
@Component
@EventListener
class AsyncTaskImpl implements AsyncTask, SerialInitialize {
    @Autowired
    private AsyncTest asyncTest;

    @Async
    @Override
    @EventListener(AsyncEvent.class)
    public void onAsyncEvent(AsyncEvent event) {
        CommonUtil.sleep(1000);
        asyncTest.getAsync()[asyncTest.getIndex().getAndIncrement()] = event.getSource();                               // 代理类，需要通过方法获取
        asyncTest.getLatch().countDown();
    }
}
