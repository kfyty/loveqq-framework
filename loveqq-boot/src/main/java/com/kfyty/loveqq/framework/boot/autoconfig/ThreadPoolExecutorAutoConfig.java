package com.kfyty.loveqq.framework.boot.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.BeanCustomizer;
import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.lang.VirtualThreadCallableDecorator;
import com.kfyty.loveqq.framework.core.lang.VirtualThreadRunnableDecorator;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.DecorateScheduledExecutorService;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.VirtualThreadExecutorHolder;
import com.kfyty.loveqq.framework.core.thread.NamedThreadFactory;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.CPU_CORE;

/**
 * 描述: 默认线程池配置
 *
 * @author kfyty725
 * @date 2022/10/17 21:46
 * @email kfyty725@hotmail.com
 */
@Configuration
public class ThreadPoolExecutorAutoConfig implements InitializingBean, DestroyBean {
    /**
     * 默认线程池名称
     */
    public static final String DEFAULT_THREAD_POOL_EXECUTOR = "defaultThreadPoolExecutor";

    /**
     * 默认线程池名称
     */
    public static final String DEFAULT_SCHEDULED_THREAD_POOL_EXECUTOR = "defaultScheduledThreadPoolExecutor";

    /**
     * 默认的线程池
     * 虚拟线程池是单例模式，所以这里不设置销毁方法，而是 {@link #destroy()} 处理
     *
     * @return 线程池
     */
    @Bean(value = DEFAULT_THREAD_POOL_EXECUTOR, resolveNested = false, independent = true)
    public ExecutorService defaultThreadPoolExecutor() {
        if (CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            return VirtualThreadExecutorHolder.getInstance();
        }
        return new ThreadPoolExecutor(
                CPU_CORE,
                CPU_CORE << 1,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2 << 20),                     // 100万级别，更多任务请自求多福
                new NamedThreadFactory("default-task-executor")
        );
    }

    /**
     * 默认可调度的线程池
     *
     * @return 线程池
     */
    @Bean(value = DEFAULT_SCHEDULED_THREAD_POOL_EXECUTOR, destroyMethod = "shutdown", resolveNested = false, independent = true)
    public ScheduledExecutorService defaultScheduledThreadPoolExecutor() {
        DecorateScheduledExecutorService executor = new DecorateScheduledExecutorService(CPU_CORE, new NamedThreadFactory("default-scheduled-executor"));
        if (CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            executor.setTaskDecorator(VirtualThreadRunnableDecorator.INSTANCE);
            executor.setCallDecorator(VirtualThreadCallableDecorator.INSTANCE);
        }
        return executor;
    }

    /**
     * 自定义线程池工厂配置
     *
     * @return 配置
     */
    @Bean(resolveNested = false, independent = true)
    public BeanCustomizer<ThreadPoolExecutor> threadPoolExecutorCustomizer() {
        return new BeanCustomizer<ThreadPoolExecutor>() {

            @Override
            public void customize(ThreadPoolExecutor bean) {
                // nothing
            }

            @Override
            public void customize(String name, ThreadPoolExecutor bean) {
                bean.setThreadFactory(new NamedThreadFactory(name));
            }
        };
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
