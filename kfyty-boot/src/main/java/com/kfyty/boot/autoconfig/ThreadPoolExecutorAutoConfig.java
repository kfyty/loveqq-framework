package com.kfyty.boot.autoconfig;

import com.kfyty.boot.autoconfig.support.DefaultThreadPoolExecutor;
import com.kfyty.support.autoconfig.BeanCustomizer;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.thread.NamedThreadFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 描述: 默认线程池配置
 *
 * @author kfyty725
 * @date 2022/10/17 21:46
 * @email kfyty725@hotmail.com
 */
@Configuration
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
    @Bean(DEFAULT_THREAD_POOL_EXECUTOR)
    public ThreadPoolExecutor defaultThreadPoolExecutor() {
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
