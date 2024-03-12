package com.kfyty.boot.feign.autoconfig.factory;

import com.kfyty.boot.feign.autoconfig.annotation.FeignClient;
import com.kfyty.core.autoconfig.annotation.Autowired;
import feign.Feign;
import feign.ribbon.LBClientFactory;
import feign.ribbon.RibbonClient;

/**
 * 描述: 基于 ribbon 创建 feign 代理
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
public class RibbonFeignFactoryBean<T> extends FeignFactoryBean<T> {
    @Autowired(required = false)
    private LBClientFactory lbClientFactory;

    public RibbonFeignFactoryBean(Class<T> feignInterface) {
        super(feignInterface);
    }

    @Override
    protected void afterConfig(FeignClient annotation, Feign.Builder builder) {
        if (this.lbClientFactory == null) {
            super.afterConfig(annotation, builder);
            return;
        }
        RibbonClient ribbonClient = RibbonClient.builder()
                .delegate(this.client)
                .lbClientFactory(this.lbClientFactory)
                .build();
        super.afterConfig(annotation, builder.client(ribbonClient));
    }
}
