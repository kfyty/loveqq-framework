package com.kfyty.loveqq.framework.web.core.filter;

import com.kfyty.loveqq.framework.core.utils.IOC;
import com.kfyty.loveqq.framework.web.core.filter.internal.FilterTransformer;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import static com.kfyty.loveqq.framework.web.core.request.support.RequestContextHolder.callWithTraceId;

/**
 * 描述: mvc 过滤器
 *
 * @author kfyty725
 * @date 2024/7/5 11:04
 * @email kfyty725@hotmail.com
 */
public interface Filter {
    /**
     * 返回匹配路径
     *
     * @return 匹配路径
     */
    default String[] getPattern() {
        if (IOC.isServletServer()) {
            return new String[]{"/*"};
        }
        return new String[]{"/**"};
    }

    /**
     * 返回是否 websocket 请求
     *
     * @param request 请求
     * @return true if websocket
     */
    default boolean isWebSocket(ServerRequest request) {
        String connection = request.getHeader("connection");
        return connection != null && connection.equalsIgnoreCase("upgrade");
    }

    /**
     * 过滤
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     */
    default Publisher<Void> doFilter(ServerRequest request, ServerResponse response, FilterChain chain) {
        return Mono.fromSupplier(() -> callWithTraceId(request, () -> doFilter(request, response))).flatMap(new FilterTransformer(request, response, chain));
    }

    /**
     * 过滤，主要用于适配 servlet filter
     *
     * @param request  请求
     * @param response 响应
     * @return continue
     */
    default Continue doFilter(ServerRequest request, ServerResponse response) {
        return Continue.TRUE;
    }

    /**
     * 主要是适配 {@link this#doFilter(ServerRequest, ServerResponse)} 无法应用 finally 逻辑
     */
    @RequiredArgsConstructor
    class Continue {
        public static final Continue TRUE = ofTrue();
        public static final Continue FALSE = ofFalse();

        private final boolean _continue_;
        private final Runnable _finally_;

        public boolean _continue_() {
            return _continue_;
        }

        public void _finally_() {
            if (_finally_ != null) {
                _finally_.run();
            }
        }

        public static Continue ofTrue() {
            return new Continue(true, null);
        }

        public static Continue ofTrue(Runnable _finally_) {
            return new Continue(true, _finally_);
        }

        public static Continue ofFalse() {
            return new Continue(false, null);
        }

        public static Continue ofFalse(Runnable _finally_) {
            return new Continue(false, _finally_);
        }
    }
}
