package com.kfyty.loveqq.framework.boot.autoconfig;

import com.kfyty.loveqq.framework.boot.autoconfig.support.DefaultThreadPoolExecutor;
import com.kfyty.loveqq.framework.core.autoconfig.BeanCustomizer;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.thread.NamedThreadFactory;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 描述: 默认线程池配置
 *
 * @author kfyty725
 * @date 2022/10/17 21:46
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Component
public class ThreadPoolExecutorAutoConfig {
    /**
     * 默认线程池名称
     */
    public static final String DEFAULT_THREAD_POOL_EXECUTOR = "defaultThreadPoolExecutor";

    /**
     * 默认线程池
     *
     * @return 线程池
     */
    @Bean(value = DEFAULT_THREAD_POOL_EXECUTOR, resolveNested = false, independent = true)
    public ExecutorService defaultThreadPoolExecutor() {
        if (CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("thread-handler-", 0).factory());
        }
        DefaultThreadPoolExecutor executor = new DefaultThreadPoolExecutor();
        executor.setThreadFactory(new NamedThreadFactory("default-task-executor"));
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
}
