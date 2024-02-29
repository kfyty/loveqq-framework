package com.kfyty.boot.autoconfig;

import com.kfyty.boot.autoconfig.support.DefaultThreadPoolExecutor;
import com.kfyty.core.autoconfig.BeanCustomizer;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.thread.NamedThreadFactory;
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
@Configuration
public class ThreadPoolExecutorAutoConfig {
    /**
     * 默认线程池名称
     */
    public static final String DEFAULT_THREAD_POOL_EXECUTOR = "defaultThreadPoolExecutor";

    /**
     * 是否支持虚拟线程
     */
    public static boolean VIRTUAL_THREAD_SUPPORTED = false;

    static {
        try {
            Class.forName("java.lang.BaseVirtualThread", false, ThreadPoolExecutorAutoConfig.class.getClassLoader());
            VIRTUAL_THREAD_SUPPORTED = true;
        } catch (Throwable e) {
            log.warn("virtual thread doesn't supported");
        }
    }

    /**
     * 默认线程池
     *
     * @return 线程池
     */
    @Bean(DEFAULT_THREAD_POOL_EXECUTOR)
    public ExecutorService defaultThreadPoolExecutor() {
        if (VIRTUAL_THREAD_SUPPORTED) {
            return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("thread-handler").factory());
        }
        return new DefaultThreadPoolExecutor();
    }

    /**
     * 自定义线程池工厂配置
     *
     * @return 配置
     */
    @Bean
    public BeanCustomizer<ThreadPoolExecutor> threadPoolExecutorBeanCustomizer() {
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
