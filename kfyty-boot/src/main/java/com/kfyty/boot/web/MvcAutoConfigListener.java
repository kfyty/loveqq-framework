package com.kfyty.boot.web;

import com.kfyty.boot.K;
import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.ComponentScan;
import com.kfyty.util.CommonUtil;
import lombok.SneakyThrows;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 描述: mvc 监听器，用于从 tomcat 中启动自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:52
 * @email kfyty725@hotmail.com
 */
@ComponentScan
public class MvcAutoConfigListener implements ServletContextListener {
    private static final String BASE_PACKAGE_PARAM_NAME = "basePackage";

    private ApplicationContext applicationContext;

    @Override
    @SneakyThrows
    public void contextInitialized(ServletContextEvent sce) {
        if(applicationContext == null) {
            synchronized (this) {
                if(applicationContext == null) {
                    String basePackage = sce.getServletContext().getInitParameter(BASE_PACKAGE_PARAM_NAME);
                    ComponentScan annotation = this.getClass().getAnnotation(ComponentScan.class);
                    CommonUtil.setAnnotationValue(annotation, "value", new String[] {basePackage});
                    K.run(MvcAutoConfigListener.class);
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
