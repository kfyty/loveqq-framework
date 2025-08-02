package com.kfyty.loveqq.framework.boot.feign.autoconfig.factory;

import com.kfyty.loveqq.framework.boot.feign.autoconfig.annotation.FeignClient;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.override.LoveqqRibbonClient;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import feign.Feign;

/**
 * 描述: 基于 ribbon 创建 feign 代理
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
public class RibbonFeignFactoryBean<T> extends FeignFactoryBean<T> {
    @Autowired(required = false)
    private LoadBalancerClientFactory lbClientFactory;

    public RibbonFeignFactoryBean(Class<T> feignInterface) {
        super(feignInterface);
    }

    @Override
    protected void afterConfig(FeignClient annotation, Feign.Builder builder) {
        // name 空表示没有使用注册中心，无需 ribbon 包装
        if (this.lbClientFactory == null || annotation.value().isEmpty()) {
            super.afterConfig(annotation, builder);
            return;
        }
        LoveqqRibbonClient ribbonClient = new LoveqqRibbonClient(this.client, this.lbClientFactory);
        super.afterConfig(annotation, builder.client(ribbonClient));
    }
}
