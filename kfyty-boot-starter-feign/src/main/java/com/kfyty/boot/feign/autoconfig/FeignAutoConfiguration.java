package com.kfyty.boot.feign.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Order;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnClass;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import feign.Client;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
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
