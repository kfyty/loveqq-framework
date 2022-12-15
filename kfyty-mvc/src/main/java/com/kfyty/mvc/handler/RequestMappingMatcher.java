package com.kfyty.mvc.handler;

import com.kfyty.mvc.mapping.MethodMapping;

import javax.servlet.http.HttpServletRequest;
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
     * @param request {@link HttpServletRequest}
     * @return {@link MethodMapping}
     */
    MethodMapping doMatchRequest(HttpServletRequest request);
}
