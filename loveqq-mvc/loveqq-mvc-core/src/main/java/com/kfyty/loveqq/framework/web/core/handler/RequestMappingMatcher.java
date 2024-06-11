package com.kfyty.loveqq.framework.web.core.handler;

import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;

import java.util.List;

/**
 * 描述: 请求映射匹配器
 *
 * @author kfyty725
 * @date 2021/6/4 10:05
 * @email kfyty725@hotmail.com
 */
public interface RequestMappingMatcher {
    /**
     * 注册方法映射
     * 实现必须线程安全
     *
     * @param methodMappings 方法映射
     */
    void registryMethodMapping(List<MethodMapping> methodMappings);

    /**
     * 匹配请求
     *
     * @param method 请求方法
     * @param requestURI 请求 URI
     * @return {@link MethodMapping}
     */
    MethodMapping doMatchRequest(RequestMethod method, String requestURI);
}
