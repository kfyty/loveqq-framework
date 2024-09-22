package com.kfyty.loveqq.framework.boot.mvc.server.netty.autoconfig;

import com.kfyty.loveqq.framework.boot.mvc.server.netty.NettyWebServer;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.resource.DefaultResourceResolver;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.resource.ResourceResolver;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnEnableWebMvc;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.web.core.RegistrationBean;
import com.kfyty.loveqq.framework.web.core.autoconfig.WebServerProperties;
import com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.Filter;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.FilterRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.netty.ws.WebSocketHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 描述: netty 服务器自动配置
 *
 * @author kfyty725
 * @date 2024/7/7 11:29
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnEnableWebMvc
public class NettyServerAutoConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebServerProperties webServerProperties;

    @Bean
    @ConditionalOnMissingBean
    public ResourceResolver defaultResourceResolver() {
        return new DefaultResourceResolver();
    }

    @Bean
    @ConfigurationProperties("k.mvc.netty")
    public NettyProperties nettyServerProperties(ResourceResolver resourceResolver) {
        NettyProperties config = this.webServerProperties.copy(new NettyProperties());
        config.setResourceResolver(resourceResolver);
        this.collectAndConfigFilter(config);
        return config;
    }

    @Bean(destroyMethod = "stop", resolveNested = false, independent = true)
    public NettyWebServer nettyWebServer(NettyProperties config, DispatcherHandler dispatcherHandler, @Autowired(required = false) List<WebSocketHandler> webSocketHandlers) {
        Map<String, WebSocketHandler> webSocketHandlerMap = webSocketHandlers.stream().collect(Collectors.toMap(WebSocketHandler::getEndPoint, v -> v));
        return new NettyWebServer(config, dispatcherHandler, webSocketHandlerMap);
    }

    protected void collectAndConfigFilter(NettyProperties config) {
        List<Object> filters = this.collectBeans(Filter.class, FilterRegistrationBean.class);
        for (Object filter : filters) {
            if (filter instanceof FilterRegistrationBean) {
                config.addWebFilter((FilterRegistrationBean) filter);
            } else {
                config.addWebFilter((Filter) filter);
            }
        }
    }

    protected List<Object> collectBeans(Class<?> filterClass, Class<? extends RegistrationBean<?>> filterRegistrationClass) {
        List<Object> beans = new ArrayList<>();
        Collection<?> beansWithAnnotation = this.applicationContext.getBeanOfType(filterClass).values();
        Collection<?> beansWithRegistration = this.applicationContext.getBeanOfType(filterRegistrationClass).values();

        beans.addAll(beansWithAnnotation);
        beans.addAll(beansWithRegistration);

        if (!beansWithAnnotation.isEmpty() && !beansWithRegistration.isEmpty()) {
            return beans.stream().sorted(Comparator.comparing(BeanUtil::getBeanOrder)).collect(Collectors.toList());
        }

        return beans;
    }
}
