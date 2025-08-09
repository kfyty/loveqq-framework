package com.kfyty.loveqq.framework.web.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.EventListener;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Import;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.kfyty.loveqq.framework.core.event.PropertyConfigRefreshedEvent;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
@Import(config = {WebServerProperties.class, GatewayRouteProperties.class})
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

    @EventListener
    public void onPropertiesRefresh(PropertyConfigRefreshedEvent event) {
        ApplicationContext applicationContext = event.getSource();
        RouteRegistry routeRegistry = applicationContext.getBean(RouteRegistry.class);
        routeRegistry.removeRoute(r -> r instanceof GatewayRoute);
        this.registryGatewayRoute(applicationContext);
    }

    protected void registryControllerRoute(ApplicationContext applicationContext) {
        RouteRegistry routeRegistry = applicationContext.getBean(RouteRegistry.class);
        RequestMappingHandler requestMappingHandler = applicationContext.getBean(RequestMappingHandler.class);
        for (Map.Entry<String, BeanDefinition> entry : applicationContext.getBeanDefinitionWithAnnotation(Controller.class, true).entrySet()) {
            List<Route> routes = requestMappingHandler.resolveRequestMappingRoute(entry.getValue().getBeanType(), new Lazy<>(() -> applicationContext.getBean(entry.getKey())));
            routeRegistry.registryRoute(routes);
        }
    }

    protected void registryGatewayRoute(ApplicationContext applicationContext) {
        RouteRegistry routeRegistry = applicationContext.getBean(RouteRegistry.class);
        GatewayRouteProperties routeProperties = applicationContext.getBean(GatewayRouteProperties.class);
        Map<String, RouteDefinition> routeDefinitions = applicationContext.getBeanOfType(RouteDefinition.class);
        Map<String, GatewayRoute> gatewayRoutes = applicationContext.getBeanOfType(GatewayRoute.class);

        final List<GatewayRoute> gatewayRoutesList = new ArrayList<>();

        if (routeProperties.getRoutes() != null) {
            for (RouteDefinition routeDefinition : routeProperties.getRoutes()) {
                gatewayRoutesList.add(GatewayRoute.create(applicationContext, routeDefinition));
            }
        }

        for (RouteDefinition routeDefinition : routeDefinitions.values()) {
            gatewayRoutesList.add(GatewayRoute.create(applicationContext, routeDefinition));
        }

        // 这里要先判断，避免默认过滤器不存在
        if (!gatewayRoutes.isEmpty()) {
            final GatewayFilter balanceGatewayFilter = applicationContext.getBean(LoadBalanceGatewayFilter.class);
            final GatewayFilter defaultWebSocketForwardFilter = applicationContext.getBean(DEFAULT_WEB_SOCKET_FORWARD_FILTER_NAME);
            final GatewayFilter defaultForwardFilter = applicationContext.getBean(DEFAULT_FORWARD_FILTER_NAME);
            for (GatewayRoute gatewayRoute : gatewayRoutes.values()) {
                this.addDefaultFilter(gatewayRoute, balanceGatewayFilter, defaultWebSocketForwardFilter, defaultForwardFilter);
                gatewayRoutesList.add(gatewayRoute);
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

    protected void addDefaultFilter(GatewayRoute gatewayRoute,
                                    GatewayFilter balanceGatewayFilter,
                                    GatewayFilter defaultWebSocketForwardFilter,
                                    GatewayFilter defaultForwardFilter) {
        boolean hasLoadBalanceFilter = false;
        boolean hasWebsocketForwardFilter = false;
        boolean hasForwardFilter = false;
        if (gatewayRoute.getFilters() != null) {
            for (GatewayFilter filter : gatewayRoute.getFilters()) {
                if (balanceGatewayFilter != null && filter == balanceGatewayFilter) {
                    hasLoadBalanceFilter = true;
                    continue;
                }
                if (filter == defaultWebSocketForwardFilter) {
                    hasWebsocketForwardFilter = true;
                    continue;
                }
                if (filter == defaultForwardFilter) {
                    hasForwardFilter = true;
                    continue;
                }
            }
        }
        if (balanceGatewayFilter != null && !hasLoadBalanceFilter || !hasWebsocketForwardFilter || !hasForwardFilter) {
            List<GatewayFilter> filters = new ArrayList<>(Optional.ofNullable(gatewayRoute.getFilters()).orElse(Collections.emptyList()));
            if (balanceGatewayFilter != null && !hasLoadBalanceFilter) {
                Mapping.from(balanceGatewayFilter).whenNotNull(filters::add);
            }
            if (!hasWebsocketForwardFilter) {
                Mapping.from(defaultWebSocketForwardFilter).whenNotNull(filters::add);
            }
            if (!hasForwardFilter) {
                Mapping.from(defaultForwardFilter).whenNotNull(filters::add);
            }
            gatewayRoute.setFilters(filters.stream().sorted(Comparator.comparing(BeanUtil::getBeanOrder)).collect(Collectors.toList()));
        }
    }
}
