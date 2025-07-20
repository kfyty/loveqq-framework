package com.kfyty.loveqq.framework.boot.mvc.server.netty.autoconfig;

import com.kfyty.loveqq.framework.boot.mvc.server.netty.resource.ResourceResolver;
import com.kfyty.loveqq.framework.web.core.autoconfig.WebServerProperties;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.mvc.netty.FilterRegistrationBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.SslProvider;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/28 14:52
 * @email kfyty725@hotmail.com
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NettyProperties extends WebServerProperties {
    /**
     * select 线程数
     */
    private Integer selectThreads = 1;

    /**
     * 是否启用 {@link HttpClient}
     */
    private Boolean enableClient;

    /**
     * 是否启用转发
     */
    private Boolean forwarded;

    /**
     * 转发是否严格验证
     */
    private Boolean forwardedStrictValidation;

    /**
     * 是否启用访问日志
     */
    private Boolean accessLog;

    /**
     * 上传文件保持在内存的最大限制
     */
    private Long maxInMemorySize;

    /**
     * 空闲时间
     */
    private Duration idleTimeout;

    /**
     * 最大保持链接存活数量
     */
    private Integer maxKeepAliveRequests;

    /**
     * 读取超时时间
     */
    private Duration readTimeout;

    /**
     * 请求超时时间
     */
    private Duration requestTimeout;

    /**
     * {@link this#sslProvider} 是否允许转发到 https
     */
    private Boolean redirectHttpToHttps = false;

    /**
     * {@link HttpProtocol}
     */
    private HttpProtocol protocol;

    /**
     * {@link reactor.netty.tcp.SslProvider}
     */
    private SslProvider sslProvider;

    /**
     * 资源解析器
     */
    private ResourceResolver resourceResolver;

    /**
     * 自定义配置
     */
    private Consumer<HttpServer> serverConfigure;

    /**
     * 过滤器
     */
    private List<FilterRegistrationBean> webFilters;

    public NettyProperties() {
        this(NettyProperties.class);
    }

    public NettyProperties(Class<?> primarySource) {
        super(primarySource);
        this.compress = false;
        this.forwarded = true;
        this.forwardedStrictValidation = true;
        this.accessLog = false;
        this.webFilters = new LinkedList<>();
        this.addStaticPattern("/static/**");
        this.addDefaultStaticSuffixPattern("/**/*");
    }

    public void addWebFilter(Filter filter) {
        FilterRegistrationBean filterRegistrationBean = (FilterRegistrationBean) new FilterRegistrationBean()
                .setFilter(filter)
                .setFilterName(filter.getClass().getName())
                .setUrlPatterns(Arrays.asList(filter.getPattern()));
        this.addWebFilter(filterRegistrationBean);
    }

    public void addWebFilter(FilterRegistrationBean filter) {
        this.webFilters.add(filter);
    }
}
