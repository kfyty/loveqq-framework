package com.kfyty.loveqq.framework.core.autoconfig;

/**
 * 描述: 应用启动完成后执行的初始化命令
 *
 * @author kfyty725
 * @date 2021/7/4 18:49
 * @email kfyty725@hotmail.com
 */
public interface CommandLineRunner {
    /**
     * 运行任务
     *
     * @param args 命令行参数
     * @throws Exception 可能抛出的异常
     */
    void run(String... args) throws Exception;
}
