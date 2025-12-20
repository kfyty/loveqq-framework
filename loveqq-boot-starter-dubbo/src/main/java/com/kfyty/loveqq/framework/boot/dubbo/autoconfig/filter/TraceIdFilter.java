package com.kfyty.loveqq.framework.boot.dubbo.autoconfig.filter;

import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.MDC;

/**
 * 描述: {@link ConstantConfig#TRACE_ID} 过滤器
 *
 * @author kfyty725
 * @date 2024/10/29 20:31
 * @email kfyty725@hotmail.com
 */
@Activate(group = {"provider", "consumer"}, order = Integer.MIN_VALUE)
public class TraceIdFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext rpcContext = RpcContext.getContext();
        String prevTraceId = MDC.get(ConstantConfig.TRACE_ID);
        String prevAttachment = rpcContext.getAttachment(ConstantConfig.TRACE_ID);
        String traceId = prevTraceId != null ? prevTraceId : (prevAttachment != null ? prevAttachment : ConstantConfig.traceId());
        try {
            MDC.put(ConstantConfig.TRACE_ID, traceId);
            rpcContext.setAttachment(ConstantConfig.TRACE_ID, traceId);
            return invoker.invoke(invocation);
        } finally {
            if (prevTraceId == null) {
                MDC.remove(ConstantConfig.TRACE_ID);
            } else {
                MDC.put(ConstantConfig.TRACE_ID, prevTraceId);
            }
            if (prevAttachment == null) {
                rpcContext.removeAttachment(ConstantConfig.TRACE_ID);
            } else {
                rpcContext.setAttachment(ConstantConfig.TRACE_ID, prevAttachment);
            }
        }
    }
}
