package com.kfyty.loveqq.framework.boot.logging.logback.autoconfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.SneakyThrows;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.Objects;

/**
 * 描述: logback 配置
 *
 * @author kfyty725
 * @date 2024/6/18 18:55
 * @email kfyty725@hotmail.com
 */
@Component
public class LogbackAutoConfiguration implements BeanFactoryPreProcessor {
    /**
     * 初始化
     */
    static {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (loggerFactory instanceof LoggerContext) {
            String level = ConstantConfig.LOGGING_ROOT_LEVEL;
            ((LoggerContext) loggerFactory).getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.toLevel(level));
        }
    }

    @Value("${logging.config:}")
    private String config;

    @Value(value = "logging.level", bind = true)
    private Map<String, String> loggingLevel;

    /**
     * {@link LoggerContext}
     */
    private LoggerContext loggerContext;

    @Override
    public void preProcessBeanFactory(BeanFactory beanFactory) {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (!(loggerFactory instanceof LoggerContext)) {
            throw new ResolvableException("LoggerFactory bind failed");
        }
        this.loggerContext = (LoggerContext) loggerFactory;
        this.doConfigure();
    }

    protected void doConfigure() {
        if (CommonUtil.notEmpty(this.config)) {
            URL url = Objects.requireNonNull(this.getClass().getResource(config), "Logback config load failed.");
            this.doConfigure(url);
        }

        if (CommonUtil.notEmpty(this.loggingLevel)) {
            for (Map.Entry<String, String> entry : this.loggingLevel.entrySet()) {
                Logger logger = this.loggerContext.getLogger(entry.getKey());
                if (logger != null) {
                    logger.setLevel(Level.toLevel(entry.getValue()));
                }
            }
        }
    }

    @SneakyThrows(JoranException.class)
    protected void doConfigure(URL url) {
        this.stopAndReset(this.loggerContext);
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(this.loggerContext);
        configurator.doConfigure(url);
    }

    protected void stopAndReset(LoggerContext loggerContext) {
        loggerContext.stop();
        loggerContext.reset();
    }
}
