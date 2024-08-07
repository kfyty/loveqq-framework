package com.kfyty.loveqq.framework.web.mvc.servlet.listener;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentScan;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EnableAutoConfiguration;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.lang.reflect.Method;
import java.util.Objects;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_STRING_ARRAY;

/**
 * 描述: mvc 监听器，用于从 tomcat 中启动自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:52
 * @email kfyty725@hotmail.com
 */
@Component
@EnableAutoConfiguration
@ComponentScan(excludeFilter = @ComponentFilter("com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.autoconfig.TomcatAutoConfig"))
public class WebMvcAutoConfigListener implements ServletContextListener, ApplicationContextAware {
    /**
     * ioc 容器启动类
     */
    private static final String LAUNCH_CLASS_NAME = "com.kfyty.loveqq.framework.boot.K";

    /**
     * BeanFactory 属性 key
     *
     * @see DispatcherServlet#BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE
     */
    private static final String BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE = "BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE";

    /**
     * 扫描的基础包路径
     */
    private static final String BASE_PACKAGE_PARAM_NAME = "basePackage";

    /**
     * ioc
     */
    private volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        synchronized (WebMvcAutoConfigListener.class) {
            if (this.applicationContext == null) {
                this.configScanBasePackage(sce.getServletContext());
                this.applicationContext = this.launch(WebMvcAutoConfigListener.class);
            }
            sce.getServletContext().setAttribute(BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE, Objects.requireNonNull(this.applicationContext));
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        synchronized (WebMvcAutoConfigListener.class) {
            if (this.applicationContext != null) {
                IOUtil.close(this.applicationContext);
                sce.getServletContext().removeAttribute(BEAN_FACTORY_SERVLET_CONTEXT_ATTRIBUTE);
            }
        }
    }

    protected void configScanBasePackage(ServletContext servletContext) {
        String basePackage = servletContext.getInitParameter(BASE_PACKAGE_PARAM_NAME);
        ComponentScan annotation = AnnotationUtil.findAnnotation(this, ComponentScan.class);
        AnnotationUtil.setAnnotationValue(annotation, "value", new String[]{basePackage});
    }

    protected ApplicationContext launch(Class<?> launchClass) {
        Method method = ReflectUtil.getMethod(ReflectUtil.load(LAUNCH_CLASS_NAME), "run", Class.class, String[].class);
        return (ApplicationContext) ReflectUtil.invokeMethod(null, method, launchClass, EMPTY_STRING_ARRAY);
    }
}
