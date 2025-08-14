package com.kfyty.loveqq.framework.web.core.route.gateway;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.route.Route;

import java.util.Map;

/**
 * 功能描述: 网关路由断言
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
public interface GatewayPredicate {
    /**
     * 断言该路由是否符合
     *
     * @param route the input argument
     * @return true/false
     */
    boolean test(Route route, ServerRequest request);

    /**
     * 获取配置 class
     *
     * @return 配置 class
     */
    default Class<?> getConfigClass() {
        return null;
    }

    /**
     * 设置配置
     *
     * @param config   配置
     * @param metadata 配置元数据
     */
    default void setConfig(Object config, Map<String, String> metadata) {

    }
}
