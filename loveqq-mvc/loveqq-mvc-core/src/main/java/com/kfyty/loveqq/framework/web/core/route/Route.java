package com.kfyty.loveqq.framework.web.core.route;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import org.reactivestreams.Publisher;

/**
 * 描述: 路由
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
public interface Route extends Cloneable {
    /**
     * 路由目标 uri
     *
     * @return uri
     */
    String getUri();

    /**
     * 路由路径，即按 '/' 分割后的路径
     *
     * @return 路径
     */
    String[] getPaths();

    /**
     * 路由方法
     *
     * @return 路由方法
     */
    RequestMethod getRequestMethod();

    /**
     * 路由长度
     *
     * @return 路由长度
     */
    int getLength();

    /**
     * 是否 restful 风格路由
     *
     * @return true/false
     */
    default boolean isRestful() {
        return false;
    }

    /**
     * 获取指定路径参数的 restful 索引
     *
     * @param path 路径参数
     * @return 索引
     */
    default int getRestfulIndex(String path) {
        return -1;
    }

    /**
     * 返回 restful 风格索引
     *
     * @return restful 索引
     */
    default Pair<String, Integer>[] getRestfulIndex() {
        return null;
    }

    /**
     * 该路由生产的内容类型
     *
     * @return content-type
     */
    default String getProduces() {
        return null;
    }

    /**
     * 设置该路由生产的内容类型
     *
     * @param produces content-type
     */
    default void setProduces(String produces) {

    }

    /**
     * 该路由结果是否是流式结果
     *
     * @return true/false
     */
    default boolean isStream() {
        return false;
    }

    /**
     * 该路由结果是否是 sse 结果
     *
     * @return true/false
     */
    default boolean isEventStream() {
        return false;
    }

    /**
     * 获取路由控制器
     *
     * @return 控制器
     */
    default Object getController() {
        return null;
    }

    /**
     * 应用路由，并得到应用后的结果
     *
     * @param request    请求
     * @param response   响应
     * @param dispatcher 路由分发器
     * @return 结果，路由参数 + 应用结果
     */
    Pair<MethodParameter, Object> applyRoute(ServerRequest request, ServerResponse response, AbstractDispatcher<?> dispatcher);

    /**
     * 响应式应用路由，并得到应用后的结果
     *
     * @param request    请求
     * @param response   响应
     * @param dispatcher 路由分发器
     * @return 结果，路由参数 + 应用结果
     */
    Publisher<Pair<MethodParameter, Object>> applyRouteAsync(ServerRequest request, ServerResponse response, AbstractDispatcher<?> dispatcher);

    /**
     * 克隆路由
     *
     * @return 克隆的路由
     */
    Route clone();

    /**
     * 判断是否是流式 content-type
     *
     * @param contentType content-type
     * @return true/false
     */
    static boolean isStream(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.contains("application/octet-stream") ||
                contentType.contains("text/event-stream") ||
                contentType.contains("application/stream+json") ||
                contentType.contains("application/x-ndjson");
    }
}
