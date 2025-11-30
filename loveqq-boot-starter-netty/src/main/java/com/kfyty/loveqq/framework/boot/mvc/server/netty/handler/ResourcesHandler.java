package com.kfyty.loveqq.framework.boot.mvc.server.netty.handler;

import com.kfyty.loveqq.framework.boot.mvc.server.netty.autoconfig.NettyProperties;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.http.NettyServerRequest;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.http.NettyServerResponse;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.core.utils.PathUtil;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.filter.reactor.DefaultFilterChain;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import com.kfyty.loveqq.framework.web.core.request.resolver.AbstractResponseBodyHandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.core.request.support.AcceptRange;
import com.kfyty.loveqq.framework.web.core.request.support.FileRandomAccessStream;
import com.kfyty.loveqq.framework.web.core.request.support.InputStreamRandomAccessStream;
import com.kfyty.loveqq.framework.web.core.request.support.RandomAccessStream;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.resolver.ReactiveHandlerMethodReturnValueProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.HttpOperations;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerState;

import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static reactor.netty.ReactorNetty.format;

/**
 * 描述: 文件资源处理器
 *
 * @author kfyty725
 * @date 2024/7/5 11:37
 * @email kfyty725@hotmail.com
 */
@Slf4j
@RequiredArgsConstructor
public class ResourcesHandler implements ConnectionObserver {
    /**
     * 服务器属性配置
     */
    private final NettyProperties config;

    /**
     * 路径匹配器
     */
    private final PatternMatcher patternMatcher;

    /**
     * 过滤器
     */
    private final List<Filter> filters;

    @Override
    public void onStateChange(Connection connection, State newState) {
        if (newState != HttpServerState.REQUEST_RECEIVED) {
            return;
        }
        try {
            HttpOperations<?, ?> operations = (HttpOperations<?, ?>) connection;
            HttpServerRequest request = (HttpServerRequest) connection;
            HttpServerResponse response = (HttpServerResponse) connection;

            // 获取请求资源路径
            String uri = request.fullPath();

            // 匹配项目资源
            for (String pattern : this.config.getStaticPattern()) {
                if (this.patternMatcher.matches(pattern, uri)) {
                    URL resolved = this.config.getResourceResolver().resolve(uri);
                    if (resolved != null) {
                        this.sendResource(request, response, operations, resolved);
                        return;
                    }
                }
            }

            // 匹配本地磁盘路径
            for (Pair<String, String> resource : this.config.getResources()) {
                if (uri.startsWith(resource.getKey())) {
                    URL resolved = this.config.getResourceResolver().resolveNative(uri, resource);
                    if (resolved != null) {
                        this.sendResource(request, response, operations, resolved);
                        return;
                    }
                }
            }
        } catch (Throwable e) {
            log.error(format(connection.channel(), ""), e);
            connection.channel().close();
        }
    }

    protected void sendResource(HttpServerRequest request, HttpServerResponse response, HttpOperations<?, ?> operations, URL url) {
        // 设置 content-type
        final String contentType;
        String extension = IOUtil.getFileExtension(url.getFile());
        if (extension != null && extension.contains("css")) {
            contentType = "text/css";
        } else if (extension != null && extension.equals("js")) {
            contentType = "application/javascript";
        } else {
            contentType = URLConnection.getFileNameMap().getContentTypeFor(url.getFile());
        }

        // 构建通用请求/响应对象
        ServerRequest serverRequest = new NettyServerRequest(request);
        ServerResponse serverResponse = new NettyServerResponse(response);

        // 范围请求头预处理
        RandomAccessStream stream = url.getProtocol().equals("file") ? new FileRandomAccessStream(contentType, PathUtil.getPath(url).toFile()) : new InputStreamRandomAccessStream(contentType, IOUtil.newInputStream(url));
        List<AcceptRange> ranges = AbstractResponseBodyHandlerMethodReturnValueProcessor.prepareRandomAccessStream(serverRequest, serverResponse, stream);

        // 预检请求不发送实际数据
        if (RequestMethod.matchRequestMethod(serverRequest.getMethod()) == RequestMethod.HEAD) {
            new DefaultFilterChain(this.patternMatcher, this.filters, (req, res) -> response.send()).doFilter(serverRequest, serverResponse).subscribe(operations.disposeSubscriber());
            return;
        }

        // 构建发布者
        serverRequest.setAttribute(AbstractResponseBodyHandlerMethodReturnValueProcessor.MULTIPART_BYTE_RANGES_ATTRIBUTE, ranges);
        ReactiveHandlerMethodReturnValueProcessor.RandomAccessStreamByteBufPublisher publisher = new ReactiveHandlerMethodReturnValueProcessor.RandomAccessStreamByteBufPublisher(serverRequest, serverResponse, stream);

        // 订阅处理请求
        new DefaultFilterChain(this.patternMatcher, this.filters, (req, res) -> response.send(publisher))
                .doFilter(serverRequest, serverResponse)
                .subscribe(operations.disposeSubscriber());
    }
}
