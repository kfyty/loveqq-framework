package com.kfyty.loveqq.framework.boot.mvc.server.netty.autoconfig;

import com.kfyty.loveqq.framework.boot.mvc.server.netty.builder.HttpClientBuilder;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.socket.OioBasedLoopResources;
import com.kfyty.loveqq.framework.core.autoconfig.BeanCustomizer;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * 描述: {@link HttpClient} 自动配置
 *
 * @author kfyty725
 * @date 2024/9/28 20:18
 * @email kfyty725@hotmail.com
 */
@Component
public class NettyClientAutoConfig {
    /**
     * {@link HttpClient} 构建器配置
     * <p>
     * 如需自定义 {@link HttpClient}，可以自定义 {@link BeanCustomizer<HttpClientBuilder>} 进行修改
     *
     * @param virtualThread      是否启用虚拟线程
     * @param connectionProvider 连接提供者
     * @return 构建器
     */
    @Bean(resolveNested = false, independent = true)
    public HttpClientBuilder reactorHttpClientBuilder(@Value("${k.server.virtualThread:false}") Boolean virtualThread,
                                                      @Autowired(required = false) ConnectionProvider connectionProvider) {
        final HttpClientBuilder builder = connectionProvider == null ? HttpClientBuilder.builder() : HttpClientBuilder.builder(connectionProvider);
        if (virtualThread && CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            builder.configure(client -> client.runOn(new OioBasedLoopResources()));
        }
        return builder;
    }

    /**
     * {@link HttpClient} 配置
     *
     * @param builder 构建器
     * @return {@link HttpClient}
     */
    @Bean(resolveNested = false, independent = true)
    public HttpClient reactorHttpClient(HttpClientBuilder builder) {
        return builder.build();
    }
}
