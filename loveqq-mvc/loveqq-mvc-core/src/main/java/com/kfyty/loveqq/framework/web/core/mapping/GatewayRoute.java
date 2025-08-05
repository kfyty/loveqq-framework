package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.autoconfig.Ordered;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.DefaultGatewayFilterChain;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.GatewayFilter;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.GatewayPredicate;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.LoadBalanceGatewayFilter;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.RouteDefinition;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.Getter;
import lombok.Setter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_STRING_ARRAY;

/**
 * 功能描述: 网关路由
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @see RouteDefinition
 * @see GatewayRoute#create(BeanFactory, RouteDefinition)
 * @since JDK 1.8
 */
@Setter
public class GatewayRoute implements Route, Ordered {
    /**
     * 路由属性
     */
    public static final String GATEWAY_ROUTE_ATTRIBUTE = GatewayRoute.class.getName() + ".GATEWAY_ROUTE_ATTRIBUTE";

    /**
     * 默认的转发过滤器 bean name
     */
    public static final String DEFAULT_WEB_SOCKET_FORWARD_FILTER_NAME = "defaultWebSocketForwardRouteGatewayFilter";

    /**
     * 默认的转发过滤器 bean name
     */
    public static final String DEFAULT_FORWARD_FILTER_NAME = "defaultForwardRouteGatewayFilter";

    /**
     * 路由 uri
     */
    private URI uri;

    /**
     * uri 路径
     */
    @Getter
    private String[] paths;

    /**
     * 路由断言
     */
    private List<GatewayPredicate> predicates;

    /**
     * 路由过滤器
     */
    private List<GatewayFilter> filters;

    /**
     * 排序
     */
    private Integer order;

    public void setUri(URI uri) {
        this.uri = uri;
        this.paths = CommonUtil.split(uri.toString(), "[/]").toArray(EMPTY_STRING_ARRAY);
    }

    public List<GatewayPredicate> getPredicates() {
        return Collections.unmodifiableList(this.predicates);
    }

    public List<GatewayFilter> getFilters() {
        return Collections.unmodifiableList(this.filters);
    }

    public URI getURI() {
        return this.uri;
    }

    @Override
    public String getUri() {
        return this.uri.toString();
    }

    @Override
    public RequestMethod getRequestMethod() {
        return RequestMethod.TRACE;
    }

    @Override
    public int getLength() {
        return this.paths.length;
    }

    @Override
    public Pair<MethodParameter, Object> applyRoute(ServerRequest request, ServerResponse response, AbstractDispatcher<?> dispatcher) {
        throw new UnsupportedOperationException("GatewayRoute.applyRoute");
    }

    @Override
    public Publisher<Pair<MethodParameter, Object>> applyRouteAsync(ServerRequest request, ServerResponse response, AbstractDispatcher<?> dispatcher) {
        GatewayRoute cloned = this.clone();
        request.setAttribute(GATEWAY_ROUTE_ATTRIBUTE, cloned);
        return new DefaultGatewayFilterChain(this.getFilters()).doFilter(request, response).then(Mono.empty());
    }

    @Override
    public int getOrder() {
        return this.order != null ? this.order : Ordered.super.getOrder();
    }

    @Override
    public GatewayRoute clone() {
        try {
            return (GatewayRoute) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ResolvableException(e);
        }
    }

    public static GatewayRoute create(BeanFactory beanFactory, RouteDefinition routeDefinition) {
        GatewayRoute gatewayRoute = new GatewayRoute();
        gatewayRoute.setUri(URI.create(routeDefinition.getUri()));
        gatewayRoute.setPaths(CommonUtil.split(routeDefinition.getUri(), "[/]").toArray(EMPTY_STRING_ARRAY));
        gatewayRoute.setPredicates(collectGatewayPredicate(beanFactory, routeDefinition.getPredicates()));
        gatewayRoute.setFilters(collectGatewayFilter(beanFactory, routeDefinition.getFilters()));
        gatewayRoute.setOrder(routeDefinition.getOrder());
        return gatewayRoute;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<GatewayPredicate> collectGatewayPredicate(BeanFactory beanFactory, List<RouteDefinition.Predicates> predicates) {
        if (predicates == null) {
            return Collections.emptyList();
        }
        List<GatewayPredicate> gatewayPredicates = new ArrayList<>();
        for (RouteDefinition.Predicates predicate : predicates) {
            GatewayPredicate bean = beanFactory.getBean(predicate.getId());
            if (bean == null) {
                throw new IllegalArgumentException("The bean of name doesn't exists: " + predicate.getId());
            }
            if (bean.getConfigClass() != null) {
                bean.setConfig(BeanUtil.bindProperties((Map) predicate.getArgs(), bean.getConfigClass()), predicate.getArgs());
            }
            gatewayPredicates.add(bean);
        }
        return gatewayPredicates.stream().sorted(Comparator.comparing(BeanUtil::getBeanOrder)).collect(Collectors.toList());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<GatewayFilter> collectGatewayFilter(BeanFactory beanFactory, List<RouteDefinition.Filters> filters) {
        if (filters == null) {
            filters = Collections.emptyList();
        }
        List<GatewayFilter> gatewayFilters = new ArrayList<>();
        for (RouteDefinition.Filters filter : filters) {
            GatewayFilter bean = beanFactory.getBean(filter.getId());
            if (bean == null) {
                throw new IllegalArgumentException("The bean of name doesn't exists: " + filter.getId());
            }
            if (bean.getConfigClass() != null) {
                bean.setConfig(BeanUtil.bindProperties((Map) filter.getArgs(), bean.getConfigClass()), filter.getArgs());
            }
            gatewayFilters.add(bean);
        }

        // 添加默认的过滤器
        Mapping.from(beanFactory.getBean(LoadBalanceGatewayFilter.class)).whenNotNull(gatewayFilters::add);
        Mapping.from(beanFactory.getBean(DEFAULT_WEB_SOCKET_FORWARD_FILTER_NAME)).whenNotNull(e -> gatewayFilters.add((GatewayFilter) e));
        Mapping.from(beanFactory.getBean(DEFAULT_FORWARD_FILTER_NAME)).whenNotNull(e -> gatewayFilters.add((GatewayFilter) e));

        return gatewayFilters.stream().sorted(Comparator.comparing(BeanUtil::getBeanOrder)).collect(Collectors.toList());
    }
}
