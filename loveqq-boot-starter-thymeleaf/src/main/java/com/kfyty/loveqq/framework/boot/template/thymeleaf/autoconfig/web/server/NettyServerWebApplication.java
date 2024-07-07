package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.web.server;

import com.kfyty.loveqq.framework.core.utils.IOUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import com.kfyty.loveqq.framework.web.mvc.netty.ServerWebServer;
import org.thymeleaf.web.IWebApplication;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/7/7 10:30
 * @email kfyty725@hotmail.com
 */
public class NettyServerWebApplication implements IWebApplication {

    @Override
    public boolean containsAttribute(String name) {
        return false;
    }

    @Override
    public int getAttributeCount() {
        return 0;
    }

    @Override
    public Set<String> getAllAttributeNames() {
        return Collections.emptySet();
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        return Collections.emptyMap();
    }

    @Override
    public Object getAttributeValue(String name) {
        return null;
    }

    @Override
    public void setAttributeValue(String name, Object value) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public boolean resourceExists(String path) {
        return this.getResource(path) != null;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return IOUtil.newInputStream(this.getResource(path));
    }

    public URL getResource(String path) {
        if (path.startsWith("/")) {
            return ServerWebServer.class.getClassLoader().getResource(path.substring(1));
        }
        return ServerWebServer.class.getClassLoader().getResource(path);
    }

    public NettyServerWebExchange buildExchange(ServerRequest request, ServerResponse response) {
        return new NettyServerWebExchange(request, response, this);
    }
}
