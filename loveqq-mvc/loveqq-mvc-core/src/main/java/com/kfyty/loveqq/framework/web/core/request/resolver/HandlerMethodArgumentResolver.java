package com.kfyty.loveqq.framework.web.core.request.resolver;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

/**
 * 描述: 处理器方法参数解析器
 *
 * @author kfyty725
 * @date 2021/6/4 9:45
 * @email kfyty725@hotmail.com
 */
public interface HandlerMethodArgumentResolver {
    /**
     * 是否支持解析该参数
     *
     * @param parameter 方法参数
     * @return true/false
     */
    boolean supportsParameter(MethodParameter parameter);

    /**
     * 解析方法参数
     *
     * @param parameter 方法参数
     * @param mapping   方法映射
     * @param request   请求
     * @return 解析后的参数
     */
    Object resolveArgument(MethodParameter parameter, MethodMapping mapping, ServerRequest request);
}
