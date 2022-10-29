package com.kfyty.boot.quartz.autoconfig;

import com.kfyty.boot.quartz.exception.ScheduledException;
import com.kfyty.boot.quartz.processor.ScheduledAnnotationBeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Import;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * 描述: quartz 配置
 *
 * @author kfyty725
 * @date 2021/10/17 19:59
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = ScheduledAnnotationBeanPostProcessor.class)
public class QuartzAutoConfig {
    public static final String TASK_BEAN_KEY = "__TASK_BEAN_KEY__";
    public static final String TASK_METHOD_KEY = "__TASK_METHOD_KEY__";

    @Bean
    @ConditionalOnMissingBean
    public SchedulerFactory schedulerFactory() {
        return new StdSchedulerFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public Scheduler scheduler(SchedulerFactory schedulerFactory) {
        try {
            return schedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            throw new ScheduledException("failed to get scheduler !", e);
        }
    }
}
