package com.kfyty.loveqq.framework.data.jdbc.session;

import com.kfyty.database.jdbc.mapping.TemplateStatement;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.jdbc.transaction.Transaction;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.data.jdbc.intercept.Interceptor;
import com.kfyty.loveqq.framework.data.jdbc.intercept.InterceptorChain;
import com.kfyty.loveqq.framework.data.jdbc.intercept.QueryInterceptor;
import com.kfyty.loveqq.framework.data.jdbc.sql.dynamic.DynamicProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;
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
    /**
     * xml 查询标签
     */
    public static final String SELECT_LABEL = "select";

    /**
     * xml 执行标签
     */
    public static final String EXECUTE_LABEL = "execute";

    /**
     * xml 命名空间标签
     */
    public static final String MAPPER_NAMESPACE = "namespace";

    /**
     * xml 查询/执行 id 属性
     */
    public static final String MAPPER_STATEMENT_ID = "id";

    /**
     * 拦截器方法名称
     */
    public static final String INTERCEPTOR_METHOD_NAME = "intercept";

    /**
     * 默认的拦截器
     * 由于更新 sql 执行时，如果没有查询拦截器，后续不会执行，因此当存在拦截器但是没有查询拦截器时，添加一个默认的拦截器
     */
    public static final QueryInterceptor DEFAULT_INTERCEPTOR = new QueryInterceptor() {
        @Override
        public Object intercept(PreparedStatement ps, ResultSet rs, Object retValue, List<MethodParameter> parameters, InterceptorChain chain) {
            return chain.proceed();
        }
    };

    /**
     * 数据源
     */
    @Getter
    private DataSource dataSource;

    /**
     * 资源匹配器
     */
    @Getter
    private PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver;

    /**
     * 事务工厂
     */
    @Getter
    private Supplier<Transaction> transactionFactory;

    /**
     * 动态 SQL 提供者
     */
    @Getter
    private DynamicProvider<?> dynamicProvider;

    /**
     * 拦截器
     */
    private List<Interceptor> interceptors;

    /**
     * 排序后的拦截器
     */
    private Map<Method, Interceptor> interceptorMethodChain;

    /**
     * 解析的动态模板 statement
     */
    private Map<String, TemplateStatement> templateStatements;

    /**
     * 设置数据源
     *
     * @param dataSource 数据源
     * @return this
     */
    public Configuration setDataSource(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
        return this;
    }

    /**
     * 设置资源匹配器
     *
     * @param pathMatchingResourcePatternResolver 资源匹配器
     * @return this
     */
    public Configuration setPathMatchingResourcePatternResolver(PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver) {
        this.pathMatchingResourcePatternResolver = Objects.requireNonNull(pathMatchingResourcePatternResolver);
        return this;
    }

    /**
     * 设置事务工厂
     *
     * @param transactionFactory 事务工厂
     * @return this
     */
    public Configuration setTransactionFactory(Supplier<Transaction> transactionFactory) {
        this.transactionFactory = Objects.requireNonNull(transactionFactory);
        return this;
    }

    /**
     * 设置动态 SQL 提供者
     *
     * @param dynamicProvider 动态 SQL 提供者
     * @param paths           模板路径
     * @return this
     */
    public Configuration setDynamicProvider(DynamicProvider<?> dynamicProvider, String... paths) {
        this.dynamicProvider = Objects.requireNonNull(dynamicProvider);
        this.dynamicProvider.setConfiguration(this);
        return this.addTemplateStatementPaths(paths);
    }

    /**
     * 设置动态模板
     *
     * @param templateStatements 动态模板
     * @return 动态模板
     */
    public Configuration setTemplateStatements(Map<String, TemplateStatement> templateStatements) {
        this.templateStatements = Objects.requireNonNull(templateStatements);
        return this;
    }

    /**
     * 设置拦截器
     *
     * @param interceptors 拦截器
     * @return this
     */
    public Configuration setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = Objects.requireNonNull(interceptors);
        if (!interceptors.isEmpty()) {
            this.processInterceptorMethodChain();
            log.info("add interceptors success: {}", interceptors);
        }
        return this;
    }

    /**
     * 获取解析的动态模板
     *
     * @return 动态模板
     */
    public Map<String, TemplateStatement> getTemplateStatements() {
        return Collections.unmodifiableMap(ofNullable(this.templateStatements).orElse(Collections.emptyMap()));
    }

    /**
     * 获取拦截器
     *
     * @return 拦截器
     */
    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(ofNullable(this.interceptors).orElse(Collections.emptyList()));
    }

    /**
     * 获取排序后的拦截链
     *
     * @return 拦截链
     */
    public Map<Method, Interceptor> getInterceptorMethodChain() {
        if (this.interceptorMethodChain == null) {
            this.processInterceptorMethodChain();
        }
        return this.interceptorMethodChain;
    }

    /**
     * 添加拦截器
     *
     * @param interceptor 拦截器
     * @return this
     */
    public Configuration addInterceptor(Interceptor interceptor) {
        if (this.interceptors == null) {
            this.interceptors = new ArrayList<>(4);
        }
        this.interceptors.add(Objects.requireNonNull(interceptor));
        log.info("add interceptor success: {}", interceptor);
        return this;
    }

    /**
     * 添加模板路径
     *
     * @param paths 路径
     * @return this
     */
    public Configuration addTemplateStatementPaths(String... paths) {
        if (this.dynamicProvider == null) {
            throw new IllegalStateException("dynamic provider can't null");
        }
        if (CommonUtil.notEmpty(paths)) {
            this.dynamicProvider.resolve(Arrays.asList(paths)).forEach(this::addTemplateStatement);
        }
        return this;
    }

    /**
     * 添加动态模板
     *
     * @param templateStatement 动态模板
     * @return this
     */
    @SuppressWarnings("UnusedReturnValue")
    public Configuration addTemplateStatement(TemplateStatement templateStatement) {
        if (this.templateStatements == null) {
            this.templateStatements = new HashMap<>();
        }
        if (this.templateStatements.put(Objects.requireNonNull(templateStatement.getId()), templateStatement) != null) {
            throw new IllegalArgumentException("template statement already exists of id: " + templateStatement.getId());
        }
        return this;
    }

    /**
     * 处理拦截器链
     */
    protected void processInterceptorMethodChain() {
        if (CommonUtil.empty(this.interceptors)) {
            this.interceptorMethodChain = Collections.emptyMap();
            return;
        }
        if (this.interceptors.stream().noneMatch(e -> e instanceof QueryInterceptor)) {
            this.addInterceptor(DEFAULT_INTERCEPTOR);
        }
        this.interceptorMethodChain = new TreeMap<>(
                Comparator.comparing((Method e) -> ofNullable(findAnnotation(e.getDeclaringClass(), Order.class)).map(Order::value).orElse(Order.DEFAULT_PRECEDENCE))
                        .thenComparing((Method e) -> findAnnotation(e, Order.class).value())
        );
        for (Interceptor interceptor : this.interceptors) {
            Predicate<Method> interceptorTest = method -> !method.isDefault() && INTERCEPTOR_METHOD_NAME.equals(method.getName()) && hasAnnotation(method, Order.class);
            Collection<Method> methods = Arrays.stream(ReflectUtil.getMethods(interceptor.getClass())).filter(interceptorTest).distinct().collect(Collectors.toList());
            for (Method method : methods) {
                this.interceptorMethodChain.put(method, interceptor);
            }
        }
    }
}
