package com.kfyty.web.mvc.servlet.autoconfig;

import com.kfyty.core.autoconfig.InitializingBean;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.aware.BeanFactoryAware;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.web.mvc.servlet.ServerEndpointExporter;
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
@ComponentFilter(annotations = ServerEndpoint.class)
public class WebServletSocketAutoConfig implements InitializingBean, BeanFactoryAware {
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
    public void afterPropertiesSet() {
        ServerEndpointExporter serverEndpointExporter = this.beanFactory.getBean(ServerEndpointExporter.class);
        for (Object value : this.beanFactory.getBeanWithAnnotation(ServerEndpoint.class).values()) {
            serverEndpointExporter.addEndpointClass(value.getClass());
        }
        serverEndpointExporter.registerEndpoints();
    }
}
