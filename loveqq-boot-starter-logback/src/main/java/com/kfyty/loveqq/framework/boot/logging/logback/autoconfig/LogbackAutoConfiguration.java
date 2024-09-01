package com.kfyty.loveqq.framework.boot.logging.logback.autoconfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.SneakyThrows;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.Objects;

import static com.kfyty.loveqq.framework.core.generic.SimpleGeneric.from;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.getField;

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

    /**
     * {@link LoggerContext}
     */
    private LoggerContext loggerContext;

    /**
     * 帮助构建泛型
     */
    private Map<String, String> helperMap;

    @Override
    public void preProcessBeanFactory(BeanFactory beanFactory) {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (!(loggerFactory instanceof LoggerContext)) {
            throw new ResolvableException("LoggerFactory bind failed");
        }
        this.loggerContext = (LoggerContext) loggerFactory;
        this.doConfigure(beanFactory.getBean(GenericPropertiesContext.class));
    }

    protected void doConfigure(GenericPropertiesContext propertiesContext) {
        String config = propertiesContext.getProperty("logging.config");
        Map<String, String> levelMap = propertiesContext.getProperty("logging.level", from(getField(this.getClass(), "helperMap")));

        if (CommonUtil.notEmpty(config)) {
            URL url = Objects.requireNonNull(this.getClass().getResource(config), "Logback config load failed");
            this.doConfigure(url);
        }

        if (CommonUtil.notEmpty(levelMap)) {
            for (Map.Entry<String, String> entry : levelMap.entrySet()) {
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

    private void stopAndReset(LoggerContext loggerContext) {
        loggerContext.stop();
        loggerContext.reset();
    }
}
