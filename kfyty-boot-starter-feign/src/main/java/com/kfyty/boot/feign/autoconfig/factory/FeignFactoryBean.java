package com.kfyty.boot.feign.autoconfig.factory;

import com.kfyty.boot.feign.autoconfig.annotation.FeignClient;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.FactoryBean;
import com.kfyty.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import feign.Client;
import feign.Feign;
import feign.InvocationHandlerFactory;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.ResponseInterceptor;
import feign.Retryer;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 描述: 创建 feign 代理
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class FeignFactoryBean<T> implements FactoryBean<T> {
    /**
     * feign 接口
     */
    protected final Class<T> feignInterface;

    /**
     * 占位符解析器
     */
    @Autowired
    protected PlaceholdersResolver placeholdersResolver;

    /**
     * 执行客户端
     */
    @Autowired
    protected Client client;

    /**
     * 编码器
     */
    @Autowired
    protected Encoder encoder;

    /**
     * 解码器
     */
    @Autowired
    protected Decoder decoder;

    /**
     * 重试配置
     */
    @Autowired(required = false)
    protected Retryer retryer;

    /**
     * @see InvocationHandlerFactory
     */
    @Autowired(required = false)
    protected InvocationHandlerFactory invocationHandlerFactory;

    /**
     * 请求拦截器
     */
    @Autowired(required = false)
    protected List<RequestInterceptor> requestInterceptors;

    /**
     * 响应拦截器
     */
    @Autowired(required = false)
    protected List<ResponseInterceptor> responseInterceptors;

    @Override
    public Class<?> getBeanType() {
        return this.feignInterface;
    }

    @Override
    public T getObject() {
        Feign.Builder builder = Feign.builder()
                .client(this.client)
                .encoder(this.encoder)
                .decoder(this.decoder)
                .retryer(this.retryer != null ? this.retryer : Retryer.NEVER_RETRY);
        if (this.invocationHandlerFactory != null) {
            builder.invocationHandlerFactory(invocationHandlerFactory);
        }
        if (CommonUtil.notEmpty(this.requestInterceptors)) {
            builder.requestInterceptors(this.requestInterceptors);
        }
        if (CommonUtil.notEmpty(this.responseInterceptors)) {
            builder.responseInterceptors(this.responseInterceptors);
        }

        FeignClient annotation = AnnotationUtil.findAnnotation(this.feignInterface, FeignClient.class);
        this.afterConfig(annotation, builder);
        return this.buildTarget(annotation, builder);
    }

    protected void afterConfig(FeignClient annotation, Feign.Builder builder) {

    }

    protected T buildTarget(FeignClient annotation, Feign.Builder builder) {
        String name = this.placeholdersResolver.resolvePlaceholders(annotation.value());
        String url = this.placeholdersResolver.resolvePlaceholders(annotation.url());
        return builder.target(new FeignTarget<>(this.feignInterface, name, url));
    }

    @RequiredArgsConstructor
    static class FeignTarget<T> implements Target<T> {
        private final Class<T> type;

        private final String name;

        private final String url;

        @Override
        public Class<T> type() {
            return this.type;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public String url() {
            return this.url;
        }

        @Override
        public Request apply(RequestTemplate input) {
            if (!input.url().startsWith("http")) {
                String name = this.name();
                if (CommonUtil.empty(name)) {                       // 未使用注册中心
                    name = this.url();
                } else if (CommonUtil.notEmpty(this.url())) {       // 使用注册中心时，url 作为基础路径
                    name = name + this.url();
                }
                if (!name.startsWith("http")) {
                    name = "http://" + name;
                }
                input.target(name);
            }
            return input.request();
        }
    }
}
