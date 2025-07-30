package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.web.core.AbstractDispatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import org.reactivestreams.Publisher;

/**
 * 功能描述: 网关路由
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
public class GatewayRoute implements Route {


    @Override
    public String getUri() {
        return "";
    }

    @Override
    public String[] getPaths() {
        return new String[0];
    }

    @Override
    public RequestMethod getRequestMethod() {
        return null;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public boolean isRestful() {
        return false;
    }

    @Override
    public int getRestfulIndex(String path) {
        return 0;
    }

    @Override
    public Pair<String, Integer>[] getRestfulIndex() {
        return new Pair[0];
    }

    @Override
    public String getProduces() {
        return "";
    }

    @Override
    public void setProduces(String produces) {

    }

    @Override
    public boolean isStream() {
        return false;
    }

    @Override
    public boolean isEventStream() {
        return false;
    }

    @Override
    public Object getController() {
        return null;
    }

    @Override
    public Pair<MethodParameter, Object> applyRoute(ServerRequest request, ServerResponse response, AbstractDispatcher<?> dispatcher) {
        return null;
    }

    @Override
    public Publisher<Pair<MethodParameter, Object>> applyRouteAsync(ServerRequest request, ServerResponse response, AbstractDispatcher<?> dispatcher) {
        return null;
    }

    @Override
    public Route clone() {
        return null;
    }
}
