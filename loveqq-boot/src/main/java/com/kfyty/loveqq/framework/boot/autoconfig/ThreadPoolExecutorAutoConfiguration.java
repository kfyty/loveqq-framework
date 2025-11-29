package com.kfyty.loveqq.framework.boot.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ApplicationScope;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.thread.VirtualThreadCallableDecorator;
import com.kfyty.loveqq.framework.core.thread.VirtualThreadRunnableDecorator;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.DecorateExecutorService;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.DecorateScheduledExecutorService;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.VirtualThreadExecutorHolder;
import com.kfyty.loveqq.framework.core.thread.NamedThreadFactory;
import com.kfyty.loveqq.framework.core.thread.TraceCallDecorator;
import com.kfyty.loveqq.framework.core.thread.TraceTaskDecorator;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.DEFAULT_SCHEDULED_THREAD_POOL_EXECUTOR;
import static com.kfyty.loveqq.framework.core.lang.ConstantConfig.DEFAULT_THREAD_POOL_EXECUTOR;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.CPU_CORE;

/**
 * 描述: 默认线程池配置
 *
 * @author kfyty725
 * @date 2022/10/17 21:46
 * @email kfyty725@hotmail.com
 */
@Configuration
@ApplicationScope
public class ThreadPoolExecutorAutoConfiguration implements InitializingBean, DestroyBean {
    /**
     * 默认的线程池
     * 虚拟线程池是单例模式，所以这里不设置销毁方法，而是 {@link #destroy()} 处理
     *
     * @return 线程池
     */
    @ApplicationScope
    @Bean(value = DEFAULT_THREAD_POOL_EXECUTOR, resolveNested = false, independent = true)
    public ExecutorService defaultThreadPoolExecutor() {
        if (CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            return VirtualThreadExecutorHolder.getInstance();
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CPU_CORE,
                CPU_CORE << 1,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2 << 20),                                 // 100万级别，更多任务请自求多福
                new NamedThreadFactory("default-task-executor")
        );
        DecorateExecutorService decorate = new DecorateExecutorService(executor);
        decorate.setTaskDecorator(TraceTaskDecorator.INSTANCE);
        decorate.setCallDecorator(TraceCallDecorator.INSTANCE);
        return decorate;
    }

    /**
     * 默认可调度的线程池
     *
     * @return 线程池
     */
    @ApplicationScope
    @Bean(value = DEFAULT_SCHEDULED_THREAD_POOL_EXECUTOR, destroyMethod = "shutdown", resolveNested = false, independent = true)
    public ScheduledExecutorService defaultScheduledThreadPoolExecutor() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(CPU_CORE, new NamedThreadFactory("default-scheduled-executor"));
        DecorateScheduledExecutorService decorate = new DecorateScheduledExecutorService(executor);
        decorate.setTaskDecorator(TraceTaskDecorator.INSTANCE);
        decorate.setCallDecorator(TraceCallDecorator.INSTANCE);
        if (CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            decorate.andThenDelegateTask(VirtualThreadRunnableDecorator.INSTANCE);
            decorate.andThenDelegateCall(VirtualThreadCallableDecorator.INSTANCE);
        }
        return decorate;
    }

    @Override
    @SuppressWarnings({"resource", "ExpressionComparedToItself"})
    public void afterPropertiesSet() {
        if (this.defaultScheduledThreadPoolExecutor() != this.defaultScheduledThreadPoolExecutor()) {
            throw new IllegalStateException("@Configuration configure error, please check your configuration.");
        }
    }

    @Override
    public void destroy() {
        if (!CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            ExecutorService executorService = IOC.getBean(DEFAULT_THREAD_POOL_EXECUTOR);
            executorService.shutdown();
        }
    }
}
