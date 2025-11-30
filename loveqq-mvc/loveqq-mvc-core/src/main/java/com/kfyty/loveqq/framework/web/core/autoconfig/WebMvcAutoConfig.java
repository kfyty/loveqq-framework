package com.kfyty.loveqq.framework.web.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Import;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.PropertyContextRefreshedEvent;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.web.core.WebServer;
import com.kfyty.loveqq.framework.web.core.annotation.Controller;
import com.kfyty.loveqq.framework.web.core.cors.CorsConfiguration;
import com.kfyty.loveqq.framework.web.core.cors.CorsFilter;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.handler.RequestMappingHandler;
import com.kfyty.loveqq.framework.web.core.route.GatewayRoute;
import com.kfyty.loveqq.framework.web.core.route.Route;
import com.kfyty.loveqq.framework.web.core.route.RouteRegistry;
import com.kfyty.loveqq.framework.web.core.route.gateway.GatewayFilter;
import com.kfyty.loveqq.framework.web.core.route.gateway.LoadBalanceGatewayFilter;
import com.kfyty.loveqq.framework.web.core.route.gateway.RouteDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.kfyty.loveqq.framework.web.core.route.GatewayRoute.DEFAULT_FORWARD_FILTER_NAME;
import static com.kfyty.loveqq.framework.web.core.route.GatewayRoute.DEFAULT_WEB_SOCKET_FORWARD_FILTER_NAME;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(WebServer.class)
@Import(config = {WebServerProperties.class, GatewayRouteProperties.class, WebMvcAutoConfig.GatewayRouteRefreshEventListener.class})
public class WebMvcAutoConfig implements ContextAfterRefreshed {

    @Bean
    @ConfigurationProperties("k.mvc.cors")
    @ConditionalOnProperty(prefix = "k.mvc.cors", value = "allowOrigin", matchIfNonNull = true)
    public CorsConfiguration defaultCorsConfiguration() {
        return new CorsConfiguration();
    }

    @Bean
    @ConditionalOnBean(CorsConfiguration.class)
    @ConditionalOnMissingBean(CorsFilter.class)
    public Filter defaultCorsFilter(CorsConfiguration configuration) {
        return new CorsFilter(configuration);
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        this.registryControllerRoute(applicationContext);
        this.registryGatewayRoute(applicationContext);
        this.startWebServer(applicationContext);
    }

    protected void registryControllerRoute(ApplicationContext applicationContext) {
        RouteRegistry routeRegistry = applicationContext.getBean(RouteRegistry.class);
        RequestMappingHandler requestMappingHandler = applicationContext.getBean(RequestMappingHandler.class);
        for (Map.Entry<String, BeanDefinition> entry : applicationContext.getBeanDefinitionWithAnnotation(Controller.class, true).entrySet()) {
            List<Route> routes = requestMappingHandler.resolveRequestMappingRoute(entry.getValue().getBeanType(), Lazy.of(() -> applicationContext.getBean(entry.getKey())));
            routeRegistry.registryRoute(routes);
        }
    }

    protected void registryGatewayRoute(ApplicationContext applicationContext) {
        RouteRegistry routeRegistry = applicationContext.getBean(RouteRegistry.class);
        GatewayRouteProperties routeProperties = applicationContext.getBean(GatewayRouteProperties.class);
        Map<String, RouteDefinition> routeDefinitions = applicationContext.getBeanOfType(RouteDefinition.class);
        Map<String, GatewayRoute> gatewayRoutes = applicationContext.getBeanOfType(GatewayRoute.class);

        final List<GatewayRoute> gatewayRoutesList = new ArrayList<>();

        if (routeProperties != null && routeProperties.getRoutes() != null) {
            for (RouteDefinition routeDefinition : routeProperties.getRoutes()) {
                gatewayRoutesList.add(GatewayRoute.create(applicationContext, routeDefinition));
            }
        }

        for (RouteDefinition routeDefinition : routeDefinitions.values()) {
            gatewayRoutesList.add(GatewayRoute.create(applicationContext, routeDefinition));
        }

        // 这里要先判断，避免默认过滤器不存在
        if (!gatewayRoutes.isEmpty()) {
            final List<GatewayFilter> defaultFilters = Arrays.asList(
                    applicationContext.getBean(LoadBalanceGatewayFilter.class),
                    applicationContext.getBean(DEFAULT_WEB_SOCKET_FORWARD_FILTER_NAME),
                    applicationContext.getBean(DEFAULT_FORWARD_FILTER_NAME)
            );
            for (GatewayRoute gatewayRoute : gatewayRoutes.values()) {
                gatewayRoutesList.add(GatewayRoute.addDefaultFilter(gatewayRoute, defaultFilters));
            }
        }

        gatewayRoutesList.stream().sorted(Comparator.comparing(GatewayRoute::getOrder)).forEach(routeRegistry::registryRoute);
    }

    protected void startWebServer(ApplicationContext applicationContext) {
        WebServer server = applicationContext.getBean(WebServer.class);
        if (server != null && !server.isStart()) {
            server.start();
        }
    }

    /**
     * 网关路由刷新事件监听器
     */
    @Component
    @Order(Integer.MAX_VALUE)
    protected class GatewayRouteRefreshEventListener implements ApplicationListener<PropertyContextRefreshedEvent> {

        @Override
        public void onApplicationEvent(PropertyContextRefreshedEvent event) {
            ApplicationContext applicationContext = event.getSource().getApplicationContext();
            RouteRegistry routeRegistry = applicationContext.getBean(RouteRegistry.class);
            routeRegistry.removeRoute(r -> r instanceof GatewayRoute);
            WebMvcAutoConfig.this.registryGatewayRoute(applicationContext);
        }
    }
}
