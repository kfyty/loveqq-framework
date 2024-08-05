package com.kfyty.loveqq.framework.web.mvc.servlet.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentScan;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.web.mvc.servlet.ServerEndpointExporter;
import jakarta.servlet.ServletContext;
import jakarta.websocket.server.ServerEndpoint;

/**
 * 描述: websocket 自动配置
 *
 * @author kfyty725
 * @date 2021/6/4 18:29
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(ServletContext.class)
@ComponentScan(includeFilter = @ComponentFilter(annotations = ServerEndpoint.class))
public class WebServletSocketAutoConfig implements ContextAfterRefreshed, BeanFactoryAware {
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter(ServletContext servletContext) {
        return new ServerEndpointExporter(servletContext);
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        ServerEndpointExporter serverEndpointExporter = this.beanFactory.getBean(ServerEndpointExporter.class);
        for (Object value : this.beanFactory.getBeanWithAnnotation(ServerEndpoint.class).values()) {
            serverEndpointExporter.addEndpointClass(value.getClass());
        }
        serverEndpointExporter.registerEndpoints();
    }
}
