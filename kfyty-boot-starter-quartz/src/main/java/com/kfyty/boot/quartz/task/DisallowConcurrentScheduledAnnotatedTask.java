package com.kfyty.boot.quartz.task;

import org.quartz.DisallowConcurrentExecution;

/**
 * 描述: 基于 {@link com.kfyty.boot.quartz.annotation.Scheduled} 注解的任务
 *
 * @author kfyty725
 * @date 2021/10/17 20:05
 * @email kfyty725@hotmail.com
 */
@DisallowConcurrentExecution
public class DisallowConcurrentScheduledAnnotatedTask extends ScheduledAnnotatedTask {
}
