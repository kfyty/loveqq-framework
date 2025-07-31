package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import com.kfyty.loveqq.framework.web.core.autoconfig.GatewayRouteProperties;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.DefaultGatewayFilterChain;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.GatewayFilter;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.GatewayPredicate;
import com.kfyty.loveqq.framework.web.core.mapping.gateway.LoadBalanceGatewayFilter;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.Getter;
import lombok.Setter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_STRING_ARRAY;

/**
 * 功能描述: 网关路由
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Setter
public class GatewayRoute implements Route {
    /**
     * 路由属性
     */
    public static final String GATEWAY_ROUTE_ATTRIBUTE = GatewayRoute.class.getName() + ".GATEWAY_ROUTE_ATTRIBUTE";

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
    public boolean isRestful() {
        return false;
    }

    @Override
    public int getRestfulIndex(String path) {
        return -1;
    }

    @Override
    public Pair<String, Integer>[] getRestfulIndex() {
        return null;
    }

    @Override
    public String getProduces() {
        return null;
    }

    @Override
    public void setProduces(String produces) {

    }

    @Override
    public boolean isStream() {
        return false;
    }

    @Override
    public boolean isEventStream() {
        return false;
    }

    @Override
    public Object getController() {
        return null;
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
    public GatewayRoute clone() {
        try {
            return (GatewayRoute) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ResolvableException(e);
        }
    }

    public static GatewayRoute create(BeanFactory beanFactory, GatewayRouteProperties.RouteDefinition routeDefinition) {
        GatewayRoute gatewayRoute = new GatewayRoute();
        gatewayRoute.setUri(URI.create(routeDefinition.getUri()));
        gatewayRoute.setPaths(CommonUtil.split(routeDefinition.getUri(), "[/]").toArray(EMPTY_STRING_ARRAY));
        gatewayRoute.setPredicates(collectGatewayPredicate(beanFactory, routeDefinition.getPredicates()));
        gatewayRoute.setFilters(collectGatewayFilter(beanFactory, routeDefinition.getFilters()));
        return gatewayRoute;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<GatewayPredicate> collectGatewayPredicate(BeanFactory beanFactory, List<GatewayRouteProperties.Predicates> predicates) {
        if (predicates == null) {
            return Collections.emptyList();
        }
        List<GatewayPredicate> gatewayPredicates = new LinkedList<>();
        for (GatewayRouteProperties.Predicates predicate : predicates) {
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
    public static List<GatewayFilter> collectGatewayFilter(BeanFactory beanFactory, List<GatewayRouteProperties.Filters> filters) {
        if (filters == null) {
            return Collections.emptyList();
        }
        List<GatewayFilter> gatewayFilters = new LinkedList<>();
        for (GatewayRouteProperties.Filters filter : filters) {
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
        gatewayFilters.add(beanFactory.getBean(LoadBalanceGatewayFilter.class));
        gatewayFilters.add(beanFactory.getBean(DEFAULT_FORWARD_FILTER_NAME));

        return gatewayFilters.stream().sorted(Comparator.comparing(BeanUtil::getBeanOrder)).collect(Collectors.toList());
    }
}
