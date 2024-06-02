package com.kfyty.async;

import com.kfyty.boot.K;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.core.autoconfig.SerialInitialize;
import com.kfyty.core.autoconfig.annotation.Async;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.BootApplication;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.EventListener;
import com.kfyty.core.event.ApplicationEvent;
import com.kfyty.core.event.ApplicationEventPublisher;
import com.kfyty.core.event.ContextRefreshedEvent;
import com.kfyty.core.utils.CommonUtil;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/26 12:07
 * @email kfyty725@hotmail.com
 */
@BootApplication
public class AsyncTest implements ContextAfterRefreshed {
    int[] async = new int[2];
    CountDownLatch latch = new CountDownLatch(1);
    AtomicInteger index = new AtomicInteger(0);

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void asyncTest() {
        K.run(AsyncTest.class);
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        applicationEventPublisher.publishEvent(new AsyncEvent(2));
        this.async[index.getAndIncrement()] = 1;
    }

    @SneakyThrows
    @EventListener
    public void onContextRefreshed(ContextRefreshedEvent event) {
        latch.await();
        Assert.assertEquals(this.async[0], 1);
        Assert.assertEquals(this.async[1], 2);
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

@Component
class AsyncTaskImpl implements AsyncTask, SerialInitialize {
    @Autowired
    private AsyncTest asyncTest;

    @Async
    @Override
    @EventListener(AsyncEvent.class)
    public void onAsyncEvent(AsyncEvent event) {
        CommonUtil.sleep(1000);
        asyncTest.async[asyncTest.index.getAndIncrement()] = event.getSource();
        asyncTest.latch.countDown();
    }
}
