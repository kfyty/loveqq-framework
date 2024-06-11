package com.kfyty.loveqq.framework.boot.quartz.task;

import com.kfyty.loveqq.framework.boot.quartz.annotation.Scheduled;
import org.quartz.DisallowConcurrentExecution;

/**
 * 描述: 基于 {@link Scheduled} 注解的任务
 *
 * @author kfyty725
 * @date 2021/10/17 20:05
 * @email kfyty725@hotmail.com
 */
@DisallowConcurrentExecution
public class DisallowConcurrentScheduledAnnotatedTask extends ScheduledAnnotatedTask {
}
