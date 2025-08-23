package com.kfyty.loveqq.framework.boot.logging.logback.autoconfig;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

public class DefaultConsoleConfiguration extends BasicConfigurator {

    public ExecutionStatus configure(LoggerContext loggerContext) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%date{yyyy-MM-dd HH:mm:ss.SSS} %highlight(%p) [%t] [%X{traceId}] %boldGreen(%50logger{50} %-4L):  %m %n");
        encoder.start();

        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setName("console");
        appender.setContext(loggerContext);
        appender.setEncoder(encoder);
        appender.start();

        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);

        return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
    }
}
