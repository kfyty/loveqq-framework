package com.kfyty.loveqq.framework.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 描述: 命令工具
 *
 * @author kfyty725
 * @date 2021/6/17 10:30
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class CommandUtil {

    public static String executeCommand(String commandTemplate, Object... args) {
        return executeCommand(Integer.MAX_VALUE, true, commandTemplate, args);
    }

    public static String executeCommand(long waitTimeout, boolean readInfoOnException, String commandTemplate, Object... args) {
        Process exec = null;
        String command = CommonUtil.format(commandTemplate, args);
        try {
            exec = Runtime.getRuntime().exec(command);
            exec.wait(waitTimeout);
            return printProcess(command, exec);
        } catch (IllegalMonitorStateException e) {
            log.warn("command error: {}", e.getMessage());
            if (readInfoOnException) {
                return printProcess(command, exec);
            }
            return null;
        } catch (IOException | InterruptedException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static String printProcess(String command, Process process) {
        if (process == null) {
            return null;
        }

        String info = IOUtil.toString(process.getInputStream());
        String error = IOUtil.toString(process.getErrorStream());

        if (CommonUtil.notEmpty(info)) {
            log.info("exec {} -> {}", command, info);
        }

        if (CommonUtil.notEmpty(error)) {
            log.error("exec {} -> {}", command, info);
        }

        return info;
    }
}
