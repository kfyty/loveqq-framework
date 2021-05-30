package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.WebServer;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.mvc.servlet.HandlerInterceptor;
import com.kfyty.mvc.tomcat.TomcatConfig;
import com.kfyty.mvc.tomcat.TomcatWebServer;
import com.kfyty.support.autoconfig.BeanRefreshComplete;
import com.kfyty.support.autoconfig.ConfigurableContext;
import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;

import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/28 14:53
 * @email kfyty725@hotmail.com
 */
@Configuration
public class TomcatAutoConfig implements BeanRefreshComplete, DestroyBean {
    @Autowired
    private ConfigurableContext configurableContext;

    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorChain;

    @Bean
    public TomcatConfig tomcatConfig() {
        return new TomcatConfig(configurableContext.getPrimarySource());
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        if(this.interceptorChain != null) {
            dispatcherServlet.getInterceptorChains().addAll(this.interceptorChain);
        }
        return dispatcherServlet;
    }

    @Bean
    public TomcatWebServer tomcatWebServer(TomcatConfig config, DispatcherServlet dispatcherServlet) {
        return new TomcatWebServer(config, dispatcherServlet);
    }

    @Override
    public void onComplete(Class<?> primarySource) {
        WebServer server = configurableContext.getBean(WebServer.class);
        if(server != null) {
            server.start();
        }
    }

    @Override
    public void onDestroy() {
        WebServer server = configurableContext.getBean(WebServer.class);
        if(server != null) {
            server.stop();
        }
    }
}
