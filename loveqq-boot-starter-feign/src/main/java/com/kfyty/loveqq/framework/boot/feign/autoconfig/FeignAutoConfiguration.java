package com.kfyty.loveqq.framework.boot.feign.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.factory.LoadBalancerClientFactory;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.listener.ServerListener;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import feign.Client;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.ribbon.LBClientFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * 描述: feign 自动配置
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
@Configuration
public class FeignAutoConfiguration {
    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Autowired
    private FeignProperties feignProperties;

    @Bean
    @ConditionalOnMissingBean
    public Encoder feignEncoder() {
        if (this.objectMapper == null) {
            return new JacksonEncoder();
        }
        return new JacksonEncoder(this.objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public Decoder feignDecoder() {
        if (this.objectMapper == null) {
            return new JacksonDecoder();
        }
        return new JacksonDecoder(this.objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public Client defaultClient() {
        return new Client.Default(null, null);
    }

    @Bean
    @ConditionalOnClass("com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent")
    public ServerListener serverListener() {
        return new ServerListener();
    }

    @Bean
    @ConditionalOnClass("com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent")
    public LBClientFactory loadBalancerClientFactory() {
        return new LoadBalancerClientFactory();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnClass("com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent")
    public ZoneAwareLoadBalancer<Server> zoneAwareLoadBalancer() {
        ZoneAwareLoadBalancer<Server> loadBalancer = new ZoneAwareLoadBalancer<>();
        if (CommonUtil.notEmpty(this.feignProperties.getRule())) {
            IRule rule = (IRule) ReflectUtil.newInstance(ReflectUtil.load(this.feignProperties.getRule()));
            rule.setLoadBalancer(loadBalancer);
        }
        return loadBalancer;
    }

    @Order
    @Component
    @ConditionalOnClass({"okhttp3.OkHttpClient", "feign.okhttp.OkHttpClient"})
    static class OkHttpClientAutoConfig {
        @Autowired(required = false)
        private okhttp3.OkHttpClient.Builder okHttpBuilder;

        @Bean
        public okhttp3.OkHttpClient okHttpClient() {
            if (this.okHttpBuilder == null) {
                return new okhttp3.OkHttpClient();
            }
            return new okhttp3.OkHttpClient(this.okHttpBuilder);
        }

        @Bean
        public Client okHttpFeignClient(okhttp3.OkHttpClient okHttpClient) {
            return new OkHttpClient(okHttpClient);
        }
    }

    @Order
    @Component
    @ConditionalOnClass({"org.apache.http.client.HttpClient", "feign.httpclient.ApacheHttpClient"})
    static class HttpClientAutoConfig {

        @Bean
        public HttpClient httpClient() {
            return HttpClientBuilder.create().build();
        }

        @Bean
        public Client httpClientFeignClient(HttpClient httpClient) {
            return new ApacheHttpClient(httpClient);
        }
    }
}
