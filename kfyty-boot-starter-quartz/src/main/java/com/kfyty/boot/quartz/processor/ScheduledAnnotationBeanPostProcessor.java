package com.kfyty.boot.quartz.processor;

import com.kfyty.boot.quartz.annotation.Scheduled;
import com.kfyty.boot.quartz.exception.ScheduledException;
import com.kfyty.boot.quartz.task.ScheduledTask;
import com.kfyty.support.autoconfig.BeanPostProcessor;
import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.EventListener;
import com.kfyty.support.event.ContextRefreshedEvent;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import com.kfyty.support.utils.ReflectUtil;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import java.lang.reflect.Method;

import static com.kfyty.boot.quartz.autoconfig.QuartzAutoConfig.TASK_BEAN_KEY;
import static com.kfyty.boot.quartz.autoconfig.QuartzAutoConfig.TASK_METHOD_KEY;

/**
 * 描述: Scheduled 注解处理器
 *
 * @author kfyty725
 * @date 2021/10/17 20:09
 * @email kfyty725@hotmail.com
 */
@Configuration
public class ScheduledAnnotationBeanPostProcessor implements BeanPostProcessor, DestroyBean {
    private static final String TRIGGER_GROUP_SUFFIX = "$$Trigger";

    private boolean hasTask;

    @Autowired
    private Scheduler scheduler;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        this.processScheduledTask(bean);
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            if (this.hasTask) {
                this.scheduler.shutdown(true);
            }
        } catch (SchedulerException e) {
            throw new ScheduledException("failed to close the scheduled task !", e);
        }
    }

    @EventListener
    public void onContextRefreshed(ContextRefreshedEvent event) {
        try {
            if (this.hasTask) {
                this.scheduler.start();
            } else {
                this.scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            throw new ScheduledException("failed to start the scheduled task !", e);
        }
    }

    protected void processScheduledTask(Object bean) {
        try {
            Scheduled scheduled = null;
            for (Method method : ReflectUtil.getMethods(bean.getClass())) {
                if ((scheduled = AnnotationUtil.findAnnotation(method, Scheduled.class)) == null) {
                    continue;
                }
                JobKey jobKey = new JobKey(method.getName(), AopUtil.getSourceClass(bean).getName());
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put(TASK_BEAN_KEY, bean);
                jobDataMap.put(TASK_METHOD_KEY, method);
                JobDetail jobDetail = JobBuilder.newJob(ScheduledTask.class)
                        .withIdentity(jobKey)
                        .usingJobData(jobDataMap)
                        .build();
                CronTrigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(jobKey.getName(), jobKey.getGroup() + TRIGGER_GROUP_SUFFIX)
                        .withSchedule(CronScheduleBuilder.cronSchedule(scheduled.cron()))
                        .build();
                this.scheduler.scheduleJob(jobDetail, trigger);
                this.hasTask = true;
            }
        } catch (SchedulerException e) {
            throw new ScheduledException("failed to parse the scheduled task !", e);
        }
    }
}
