package com.kfyty.loveqq.framework.boot.feign.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter.LoveqqDecoder;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter.LoveqqEncoder;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter.LoveqqMvcContract;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.factory.LoadBalancerClientFactory;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.listener.ServerListener;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import feign.Client;
import feign.Contract;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.httpclient.ApacheHttpClient;
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
    /**
     * 注册 {@link ZoneAwareLoadBalancer} 不要处理泛型
     */
    static {
        SimpleGeneric.registryIgnoredClass(ZoneAwareLoadBalancer.class);
    }

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Autowired
    private FeignProperties feignProperties;

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public Encoder feignEncoder() {
        if (this.objectMapper == null) {
            return new LoveqqEncoder(new JacksonEncoder());
        }
        return new LoveqqEncoder(new JacksonEncoder(this.objectMapper));
    }

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public Decoder feignDecoder() {
        if (this.objectMapper == null) {
            return new LoveqqDecoder();
        }
        return new LoveqqDecoder(this.objectMapper);
    }

    @ConditionalOnMissingBean
    @Bean(resolveNested = false, independent = true)
    public Client defaultClient() {
        return new Client.Default(null, null);
    }

    @Bean(resolveNested = false)
    @ConditionalOnClass("com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent")
    public ServerListener serverListener() {
        return new ServerListener();
    }

    @Bean(resolveNested = false)
    @ConditionalOnClass("com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent")
    public LBClientFactory loadBalancerClientFactory() {
        return new LoadBalancerClientFactory();
    }

    @Bean(destroyMethod = "shutdown", resolveNested = false, independent = true)
    @ConditionalOnClass("com.kfyty.loveqq.framework.cloud.bootstrap.event.ServerEvent")
    public ZoneAwareLoadBalancer<Server> zoneAwareLoadBalancer() {
        IRule rule = this.feignProperties.getRule();
        ZoneAwareLoadBalancer<Server> loadBalancer = new ZoneAwareLoadBalancer<>();
        if (rule != null) {
            rule.setLoadBalancer(loadBalancer);
            loadBalancer.setRule(rule);
        }
        return loadBalancer;
    }

    @Component
    @ConditionalOnMissingBean(Contract.class)
    @ConditionalOnClass("com.kfyty.loveqq.framework.web.core.annotation.RequestMapping")
    static class LoveqqMvcContractAutoConfig {

        @Bean(resolveNested = false, independent = true)
        public Contract loveqqMvcContract(PlaceholdersResolver placeholdersResolver) {
            return new LoveqqMvcContract(new Contract.Default(), placeholdersResolver);
        }
    }

    @Order
    @Component
    @ConditionalOnClass({"okhttp3.OkHttpClient", "feign.okhttp.OkHttpClient"})
    static class OkHttpClientAutoConfig {
        @Autowired(required = false)
        private okhttp3.OkHttpClient.Builder okHttpBuilder;

        @Bean(resolveNested = false, independent = true)
        public okhttp3.OkHttpClient okHttpClient() {
            if (this.okHttpBuilder == null) {
                return new okhttp3.OkHttpClient();
            }
            return new okhttp3.OkHttpClient(this.okHttpBuilder);
        }

        @Bean(resolveNested = false, independent = true)
        public Client okHttpFeignClient(okhttp3.OkHttpClient okHttpClient) {
            return new OkHttpClient(okHttpClient);
        }
    }

    @Order
    @Component
    @ConditionalOnClass({"org.apache.http.client.HttpClient", "feign.httpclient.ApacheHttpClient"})
    static class HttpClientAutoConfig {

        @Bean(resolveNested = false, independent = true)
        public HttpClient httpClient() {
            return HttpClientBuilder.create().build();
        }

        @Bean(resolveNested = false, independent = true)
        public Client httpClientFeignClient(HttpClient httpClient) {
            return new ApacheHttpClient(httpClient);
        }
    }
}
