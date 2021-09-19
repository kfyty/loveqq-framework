package com.kfyty.database.jdbc.session;

import com.kfyty.database.jdbc.intercept.Interceptor;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * 描述: 全局配置
 *
 * @author kfyty725
 * @date 2021/8/8 10:42
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class Configuration {
    private static final String INTERCEPTOR_METHOD_NAME = "intercept";

    private DataSource dataSource;
    private List<Interceptor> interceptors;
    private Map<Method, Interceptor> interceptorMethodChain;

    public DataSource getDataSource() {
        return dataSource;
    }

    public Configuration setDataSource(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
        return this;
    }

    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(this.interceptors);
    }

    public Configuration addInterceptor(Interceptor interceptor) {
        if (this.interceptors == null) {
            this.interceptors = new ArrayList<>(4);
        }
        this.interceptors.add(Objects.requireNonNull(interceptor));
        log.info("add interceptor success: {}", interceptor);
        return this;
    }

    public Configuration setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = Objects.requireNonNull(interceptors);
        this.processInterceptorMethodChain();
        log.info("add interceptors success: {}", interceptors);
        return this;
    }

    public Map<Method, Interceptor> getInterceptorMethodChain() {
        if (this.interceptorMethodChain == null) {
            this.processInterceptorMethodChain();
        }
        return interceptorMethodChain;
    }

    private void processInterceptorMethodChain() {
        if (this.interceptors == null) {
            this.interceptorMethodChain = Collections.emptyMap();
            return;
        }
        this.interceptorMethodChain = new TreeMap<>(this.interceptorMethodComparator());
        for (Interceptor interceptor : this.interceptors) {
            List<Method> methods = ReflectUtil.getMethods(interceptor.getClass()).stream().filter(e -> INTERCEPTOR_METHOD_NAME.equals(e.getName()) && !e.isDefault() && AnnotationUtil.hasAnnotation(e, Order.class)).collect(Collectors.toList());
            for (Method method : methods) {
                this.interceptorMethodChain.put(method, interceptor);
            }
        }
    }

    private Comparator<Method> interceptorMethodComparator() {
        return Comparator.comparing((Method e) -> AnnotationUtil.findAnnotation(e, Order.class).value())
                .thenComparing((Method e) -> ofNullable(AnnotationUtil.findAnnotation(e.getDeclaringClass(), Order.class)).map(Order::value).orElse(Order.LOWEST_PRECEDENCE));
    }
}
