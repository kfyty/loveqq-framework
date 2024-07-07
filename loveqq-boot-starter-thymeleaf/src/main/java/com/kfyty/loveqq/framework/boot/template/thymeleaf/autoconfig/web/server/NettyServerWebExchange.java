package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.web.server;

import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.IWebRequest;
import org.thymeleaf.web.IWebSession;
import reactor.netty.http.server.HttpServerRequest;

import java.net.HttpCookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/7/7 10:32
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class NettyServerWebExchange implements IWebExchange {
    private final ServerRequest request;

    private final ServerResponse response;

    private final IWebApplication webApplication;

    @Override
    public IWebSession getSession() {
        throw new UnsupportedOperationException("NettyServerWebExchange.getSession");
    }

    @Override
    public IWebApplication getApplication() {
        return this.webApplication;
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public String getContentType() {
        return this.response.getContentType();
    }

    @Override
    public String getCharacterEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    @Override
    public boolean containsAttribute(String name) {
        return this.request.getAttribute(name) != null;
    }

    @Override
    public int getAttributeCount() {
        return this.request.getAttributeMap().size();
    }

    @Override
    public Set<String> getAllAttributeNames() {
        return this.request.getAttributeMap().keySet();
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        return this.request.getAttributeMap();
    }

    @Override
    public Object getAttributeValue(String name) {
        return this.request.getAttribute(name);
    }

    @Override
    public void setAttributeValue(String name, Object value) {
        this.request.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        this.request.removeAttribute(name);
    }

    @Override
    public String transformURL(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

    @Override
    public IWebRequest getRequest() {
        return new IWebRequest() {
            @Override
            public String getMethod() {
                return request.getMethod();
            }

            @Override
            public String getScheme() {
                return request.getScheme();
            }

            @Override
            public String getServerName() {
                return "NettyServer";
            }

            @Override
            public Integer getServerPort() {
                return request.getServerPort();
            }

            @Override
            public String getApplicationPath() {
                return "/";
            }

            @Override
            public String getPathWithinApplication() {
                return "/";
            }

            @Override
            public String getQueryString() {
                HttpServerRequest serverRequest = (HttpServerRequest) request.getRawRequest();
                String uri = serverRequest.uri();
                int index = uri.indexOf('?');
                return index < 0 ? null : uri.substring(index + 1);
            }

            @Override
            public boolean containsHeader(String name) {
                return request.getHeader(name) != null;
            }

            @Override
            public int getHeaderCount() {
                return request.getHeaderNames().size();
            }

            @Override
            public Set<String> getAllHeaderNames() {
                return new HashSet<>(request.getHeaderNames());
            }

            @Override
            public Map<String, String[]> getHeaderMap() {
                return request.getHeaderNames().stream().collect(Collectors.toMap(k -> k, v -> new String[]{request.getHeader(v)}));
            }

            @Override
            public String[] getHeaderValues(String name) {
                return request.getHeaderNames().stream().map(request::getHeader).toArray(String[]::new);
            }

            @Override
            public boolean containsParameter(String name) {
                return request.getParameter(name) != null;
            }

            @Override
            public int getParameterCount() {
                return request.getParameterMap().size();
            }

            @Override
            public Set<String> getAllParameterNames() {
                return new HashSet<>(request.getParameterNames());
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return request.getParameterNames().stream().collect(Collectors.toMap(k -> k, v -> new String[]{request.getParameter(v)}));
            }

            @Override
            public String[] getParameterValues(String name) {
                return request.getParameterNames().stream().map(request::getParameter).toArray(String[]::new);
            }

            @Override
            public boolean containsCookie(String name) {
                return request.getCookie(name) != null;
            }

            @Override
            public int getCookieCount() {
                return request.getCookies().length;
            }

            @Override
            public Set<String> getAllCookieNames() {
                return Arrays.stream(request.getCookies()).map(HttpCookie::getName).collect(Collectors.toSet());
            }

            @Override
            public Map<String, String[]> getCookieMap() {
                return Arrays.stream(request.getCookies()).collect(Collectors.toMap(HttpCookie::getName, v -> new String[]{v.getValue()}));
            }

            @Override
            public String[] getCookieValues(String name) {
                return Arrays.stream(request.getCookies()).map(HttpCookie::getValue).toArray(String[]::new);
            }
        };
    }
}
