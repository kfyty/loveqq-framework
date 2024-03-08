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
import feign.RequestInterceptor;
import feign.ResponseInterceptor;
import feign.Retryer;
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
    private final Class<T> feignInterface;

    /**
     * 占位符解析器
     */
    @Autowired
    protected PlaceholdersResolver placeholdersResolver;

    /**
     * 执行客户端
     */
    @Autowired
    private Client client;

    /**
     * 编码器
     */
    @Autowired
    private Encoder encoder;

    /**
     * 解码器
     */
    @Autowired
    private Decoder decoder;

    /**
     * 重试配置
     */
    @Autowired(required = false)
    private Retryer retryer;

    /**
     * @see InvocationHandlerFactory
     */
    @Autowired(required = false)
    private InvocationHandlerFactory invocationHandlerFactory;

    /**
     * 请求拦截器
     */
    @Autowired(required = false)
    private List<RequestInterceptor> requestInterceptors;

    /**
     * 响应拦截器
     */
    @Autowired(required = false)
    private List<ResponseInterceptor> responseInterceptors;

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

        return builder.target(this.feignInterface, this.placeholdersResolver.resolvePlaceholders(annotation.url()));
    }

    protected void afterConfig(FeignClient annotation, Feign.Builder builder) {

    }
}
