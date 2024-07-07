package com.kfyty.loveqq.framework.web.mvc.netty.request.resolver;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.web.core.request.support.ModelViewContainer;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.removePrefix;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:29
 * @email kfyty725@hotmail.com
 */
@Component
@Order(Integer.MIN_VALUE)
public class RedirectViewHandlerMethodReturnValueProcessor implements ServerHandlerMethodReturnValueProcessor {

    @Override
    public boolean supportsReturnType(Object returnValue, MethodParameter returnType) {
        return returnType == null || returnValue instanceof String && ((String) returnValue).startsWith("redirect:");
    }

    @Override
    public Object processReturnValue(Object returnValue, MethodParameter returnType, ModelViewContainer container) throws Exception {
        String view = returnValue.toString();
        return container.getResponse().sendRedirect(removePrefix("redirect:", view));
    }
}
