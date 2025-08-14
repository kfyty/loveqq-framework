package com.kfyty.loveqq.framework.boot.mvc.server.netty.autoconfig;

import com.kfyty.loveqq.framework.boot.mvc.server.netty.socket.OioBasedLoopResources;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Primary;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * 描述: {@link reactor.netty.http.client.HttpClient} 自动配置
 *
 * @author kfyty725
 * @date 2024/9/28 20:18
 * @email kfyty725@hotmail.com
 */
@Component
public class NettyHttpClientAutoConfig {

    @Primary
    @Bean(resolveNested = false, independent = true)
    public HttpClient reactorHttpClient(@Value("${k.server.virtualThread:false}") Boolean virtualThread,
                                        @Autowired(required = false) ConnectionProvider connectionProvider) {
        HttpClient httpClient = Mapping.from(connectionProvider).notNullMap(HttpClient::create).getOr(HttpClient::create);
        if (virtualThread != null && virtualThread && CommonUtil.VIRTUAL_THREAD_SUPPORTED) {
            return httpClient.runOn(new OioBasedLoopResources());
        }
        return httpClient;
    }
}
