package com.kfyty.database.jdbc.session;

import com.kfyty.database.jdbc.intercept.Interceptor;
import com.kfyty.database.jdbc.intercept.QueryInterceptor;
import com.kfyty.database.jdbc.mapping.TemplateStatement;
import com.kfyty.database.jdbc.sql.dynamic.DynamicProvider;
import com.kfyty.support.autoconfig.annotation.Order;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
    public static final String SELECT_LABEL = "select";
    public static final String EXECUTE_LABEL = "execute";
    public static final String MAPPER_NAMESPACE = "namespace";
    public static final String MAPPER_STATEMENT_ID = "id";
    public static final String INTERCEPTOR_METHOD_NAME = "intercept";
    public static final QueryInterceptor DEFAULT_INTERCEPTOR = new QueryInterceptor() {};

    private DataSource dataSource;
    private DynamicProvider<?> dynamicProvider;
    private List<Interceptor> interceptors;
    private Map<Method, Interceptor> interceptorMethodChain;
    private Map<String, TemplateStatement> templateStatements;

    public DataSource getDataSource() {
        return dataSource;
    }

    public Configuration setDataSource(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
        return this;
    }

    public DynamicProvider<?> getDynamicProvider() {
        return dynamicProvider;
    }

    public Configuration setDynamicProvider(DynamicProvider<?> dynamicProvider, String... paths) {
        this.dynamicProvider = Objects.requireNonNull(dynamicProvider);
        return this.addTemplateStatementPaths(paths);
    }

    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(ofNullable(this.interceptors).orElse(Collections.emptyList()));
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

    public Map<String, TemplateStatement> getTemplateStatements() {
        return Collections.unmodifiableMap(ofNullable(this.templateStatements).orElse(Collections.emptyMap()));
    }

    public Configuration addTemplateStatementPaths(String... paths) {
        if (this.dynamicProvider == null) {
            throw new IllegalStateException("dynamic provider can't null");
        }
        if (CommonUtil.notEmpty(paths)) {
            this.dynamicProvider.resolve(Arrays.asList(paths)).forEach(this::addTemplateStatement);
        }
        return this;
    }

    public Configuration addTemplateStatement(TemplateStatement templateStatement) {
        if (this.templateStatements == null) {
            this.templateStatements = new HashMap<>();
        }
        if (this.templateStatements.put(Objects.requireNonNull(templateStatement.getId()), templateStatement) != null) {
            throw new IllegalArgumentException("template statement already exists of id: " + templateStatement.getId());
        }
        return this;
    }

    public Configuration setTemplateStatements(Map<String, TemplateStatement> templateStatements) {
        this.templateStatements = Objects.requireNonNull(templateStatements);
        return this;
    }

    private void processInterceptorMethodChain() {
        if (CommonUtil.empty(this.interceptors)) {
            this.interceptorMethodChain = Collections.emptyMap();
            return;
        }
        if (this.interceptors.stream().noneMatch(e -> e instanceof QueryInterceptor)) {
            this.addInterceptor(DEFAULT_INTERCEPTOR);
        }
        this.interceptorMethodChain = new TreeMap<>(
                Comparator.comparing((Method e) -> AnnotationUtil.findAnnotation(e, Order.class).value())
                        .thenComparing((Method e) -> ofNullable(AnnotationUtil.findAnnotation(e.getDeclaringClass(), Order.class)).map(Order::value).orElse(Order.LOWEST_PRECEDENCE))
        );
        for (Interceptor interceptor : this.interceptors) {
            List<Method> methods = ReflectUtil.getMethods(interceptor.getClass()).stream().filter(e -> INTERCEPTOR_METHOD_NAME.equals(e.getName()) && AnnotationUtil.hasAnnotation(e, Order.class)).distinct().collect(Collectors.toList());
            for (Method method : methods) {
                this.interceptorMethodChain.put(method, interceptor);
            }
        }
    }
}
