package com.kfyty.boot.quartz.task;

import com.kfyty.support.utils.ReflectUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.Method;

import static com.kfyty.boot.quartz.autoconfig.QuartzAutoConfig.TASK_BEAN_KEY;
import static com.kfyty.boot.quartz.autoconfig.QuartzAutoConfig.TASK_METHOD_KEY;

/**
 * 描述: 基于 {@link com.kfyty.boot.quartz.annotation.Scheduled} 注解的任务
 *
 * @author kfyty725
 * @date 2021/10/17 20:05
 * @email kfyty725@hotmail.com
 */
public class ScheduledTask implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Object bean = jobDataMap.get(TASK_BEAN_KEY);
        Method method = (Method) jobDataMap.get(TASK_METHOD_KEY);
        ReflectUtil.invokeMethod(bean, method);
    }
}
