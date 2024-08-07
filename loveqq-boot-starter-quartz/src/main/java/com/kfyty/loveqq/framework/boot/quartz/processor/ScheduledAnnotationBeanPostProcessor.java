package com.kfyty.loveqq.framework.boot.quartz.processor;

import com.kfyty.loveqq.framework.boot.quartz.annotation.Scheduled;
import com.kfyty.loveqq.framework.boot.quartz.exception.ScheduledException;
import com.kfyty.loveqq.framework.boot.quartz.task.DisallowConcurrentScheduledAnnotatedTask;
import com.kfyty.loveqq.framework.boot.quartz.task.ScheduledAnnotatedTask;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.BeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.DestroyBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static com.kfyty.loveqq.framework.boot.quartz.autoconfig.QuartzAutoConfig.TASK_BEAN_KEY;
import static com.kfyty.loveqq.framework.boot.quartz.autoconfig.QuartzAutoConfig.TASK_METHOD_KEY;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * 描述: Scheduled 注解处理器
 *
 * @author kfyty725
 * @date 2021/10/17 20:09
 * @email kfyty725@hotmail.com
 */
@Component
public class ScheduledAnnotationBeanPostProcessor implements BeanPostProcessor, ContextAfterRefreshed, DestroyBean {
    private static final String TRIGGER_GROUP_SUFFIX = "$$Trigger";

    @Autowired
    private Scheduler scheduler;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof Job) {
            this.processScheduledTask((Job) bean);
            return null;
        }
        this.processScheduledTask(bean);
        return null;
    }

    @Override
    public void destroy() {
        try {
            if (this.scheduler != null) {
                this.scheduler.shutdown(true);
            }
        } catch (SchedulerException e) {
            throw new ScheduledException("failed to close the scheduled task !", e);
        }
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        try {
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new ScheduledException("failed to start the scheduled task !", e);
        }
    }

    protected void processScheduledTask(Job job) {
        Scheduled scheduled = AnnotationUtil.findAnnotation(job, Scheduled.class);
        if (scheduled == null) {
            throw new ScheduledException("Job should annotated @Scheduled of job: " + job);
        }
        Method method = ReflectUtil.getMethod(job.getClass(), "execute", JobExecutionContext.class);
        this.buildJob(job, method, scheduled);
    }

    protected void processScheduledTask(Object bean) {
        if (AnnotationUtil.hasAnnotation(bean, Scheduled.class)) {
            return;
        }
        for (Method method : ReflectUtil.getMethods(bean.getClass())) {
            Scheduled scheduled = AnnotationUtil.findAnnotation((Object) method, Scheduled.class);
            if (scheduled != null) {
                this.buildJob(bean, method, scheduled);
            }
        }
    }

    protected void buildJob(Object bean, Method method, Scheduled scheduled) {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(TASK_BEAN_KEY, bean);
            jobDataMap.put(TASK_METHOD_KEY, method);
            JobKey jobKey = this.buildJobKey(bean, method);
            JobDetail jobDetail = this.buildJobDetail(jobKey, jobDataMap, scheduled);
            Trigger trigger = this.buildTrigger(jobKey, scheduled);
            this.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new ScheduledException("failed to resolve the scheduled task !", e);
        }
    }

    protected JobKey buildJobKey(Object bean, Method method) {
        return new JobKey(method.getName(), AopUtil.getTargetClass(bean).getName());
    }

    protected JobDetail buildJobDetail(JobKey jobKey, JobDataMap jobDataMap, Scheduled scheduled) {
        Class<? extends Job> clazz = ScheduledAnnotatedTask.class;
        if (scheduled.fixedDelay() > -1) {
            clazz = DisallowConcurrentScheduledAnnotatedTask.class;
        }
        return JobBuilder.newJob(clazz)
                .withIdentity(jobKey)
                .usingJobData(jobDataMap)
                .build();
    }

    protected Trigger buildTrigger(JobKey jobKey, Scheduled scheduled) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(jobKey.getName(), jobKey.getGroup() + TRIGGER_GROUP_SUFFIX);
        if (CommonUtil.notEmpty(scheduled.cron())) {
            return triggerBuilder
                    .withSchedule(cronSchedule(this.buildCronExpression(scheduled.cron(), scheduled.zone())))
                    .build();
        }
        if (scheduled.initialDelay() < 0 && scheduled.fixedDelay() < 0 && scheduled.fixedRate() < 0) {
            throw new ScheduledException("Scheduled configuration invalid !");
        }
        SimpleScheduleBuilder simpleScheduleBuilder = simpleSchedule();
        if (scheduled.initialDelay() > -1) {
            ZoneId zoneId = CommonUtil.empty(scheduled.zone()) ? ZoneId.systemDefault() : ZoneId.of(scheduled.zone());
            Date future = Date.from(LocalDateTime.now().plusNanos(scheduled.timeUnit().toNanos(scheduled.initialDelay())).atZone(zoneId).toInstant());
            triggerBuilder.startAt(future);
        }
        if (scheduled.fixedDelay() > -1 || scheduled.fixedRate() > -1) {
            simpleScheduleBuilder
                    .repeatForever()
                    .withIntervalInMilliseconds(scheduled.timeUnit().toMillis(Math.max(scheduled.fixedDelay(), scheduled.fixedRate())));
        }
        return triggerBuilder
                .withSchedule(simpleScheduleBuilder)
                .build();
    }

    protected CronExpression buildCronExpression(String cron, String zone) {
        try {
            CronExpression cronExpression = new CronExpression(cron);
            if (CommonUtil.notEmpty(zone)) {
                cronExpression.setTimeZone(TimeZone.getTimeZone(zone));
            }
            return cronExpression;
        } catch (ParseException e) {
            throw new ResolvableException("CronExpression '" + cron + "' is invalid.", e);
        }
    }
}
