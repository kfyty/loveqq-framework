package com.kfyty.loveqq.framework.boot.feign.autoconfig.interceptor;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;

/**
 * 描述: {@link ConstantConfig#traceId()} 拦截器
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
@Component
public class TraceIdRequestInterceptor implements RequestInterceptor {
    /**
     * 为了同时兼容命令式和响应式，直接从 mdc 中取值
     *
     * @param template 请求模板
     */
    @Override
    public void apply(RequestTemplate template) {
        String traceId = MDC.get(ConstantConfig.TRACE_ID);
        if (traceId != null && !traceId.isEmpty()) {
            template.header(ConstantConfig.TRACE_ID, traceId);
        }
    }
}
