package com.kfyty.loveqq.framework.web.mvc.netty.autoconfig;

import com.kfyty.loveqq.framework.web.core.autoconfig.WebServerProperties;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.Filter;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.FilterRegistrationBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import reactor.netty.http.server.HttpServer;

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
public class NettyServerProperties extends WebServerProperties {
    /**
     * 是否启用压缩
     */
    private Boolean compress;

    /**
     * 是否启用转发
     */
    private Boolean forwarded;

    /**
     * 是否启用访问日志
     */
    private Boolean accessLog;

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
     * 自定义配置
     */
    private Consumer<HttpServer> serverConfigure;

    /**
     * 过滤器
     */
    private List<FilterRegistrationBean> webFilters;

    public NettyServerProperties() {
        this(NettyServerProperties.class);
    }

    public NettyServerProperties(Class<?> primarySource) {
        super(primarySource);
        this.compress = false;
        this.forwarded = true;
        this.accessLog = false;
        this.webFilters = new LinkedList<>();
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
