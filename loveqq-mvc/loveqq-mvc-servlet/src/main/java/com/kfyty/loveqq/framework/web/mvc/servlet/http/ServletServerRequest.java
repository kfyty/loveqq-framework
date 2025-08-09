package com.kfyty.loveqq.framework.web.mvc.servlet.http;

import com.kfyty.loveqq.framework.core.lang.util.EnumerationIterator;
import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import com.kfyty.loveqq.framework.web.mvc.servlet.util.ServletUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 描述: servlet 实现
 *
 * @author kfyty725
 * @date 2024/7/6 18:38
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class ServletServerRequest implements ServerRequest {
    /**
     * servlet request
     */
    private final HttpServletRequest request;

    /**
     * multipart
     */
    private Map<String, MultipartFile> multipart;

    public void init() {
        String contentType = this.request.getContentType();
        if (contentType != null && contentType.contains("multipart/form-data")) {
            this.multipart = Collections.unmodifiableMap(ServletUtil.from(this.request).stream().collect(Collectors.toMap(MultipartFile::getName, v -> v)));
        }
    }

    @Override
    public String getScheme() {
        return this.request.getScheme();
    }

    @Override
    public String getHost() {
        return this.request.getServerName();
    }

    @Override
    public Integer getServerPort() {
        return this.request.getServerPort();
    }

    @Override
    public String getMethod() {
        return this.request.getMethod();
    }

    @Override
    public String getRequestURL() {
        return this.request.getRequestURL().toString();
    }

    @Override
    public String getRequestURI() {
        return this.request.getRequestURI();
    }

    @Override
    public String getContentType() {
        return this.request.getContentType();
    }

    @Override
    public InputStream getInputStream() {
        try {
            return this.request.getInputStream();
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    @Override
    public MultipartFile getMultipart(String name) {
        return this.multipart.get(name);
    }

    @Override
    public Collection<MultipartFile> getMultipart() {
        return this.multipart.values();
    }

    @Override
    public String getParameter(String name) {
        String[] values = this.request.getParameterValues(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values.length == 1 ? values[0] : String.join(",", values);
    }

    @Override
    public Collection<String> getParameterNames() {
        return new EnumerationIterator<>(this.request.getParameterNames()).toList();
    }

    @Override
    public Map<String, String> getParameterMap() {
        return this.request.getParameterMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> String.join(",", v.getValue())));
    }

    @Override
    public String getHeader(String name) {
        return this.request.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return new EnumerationIterator<>(this.request.getHeaders(name)).toList();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return new EnumerationIterator<>(this.request.getHeaderNames()).toList();
    }

    @Override
    public HttpCookie getCookie(String name) {
        Cookie[] cookies = this.request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (Objects.equals(cookie.getName(), name)) {
                return new HttpCookie(cookie.getName(), cookie.getValue());
            }
        }
        return null;
    }

    @Override
    public HttpCookie[] getCookies() {
        Cookie[] cookies = this.request.getCookies();
        if (cookies == null) {
            return null;
        }
        HttpCookie[] httpCookies = new HttpCookie[cookies.length];
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            httpCookies[i] = new HttpCookie(cookie.getName(), cookie.getValue());
        }
        return httpCookies;
    }

    @Override
    public void setAttribute(String name, Object o) {
        this.request.setAttribute(name, o);
    }

    @Override
    public Object getAttribute(String name) {
        return this.request.getAttribute(name);
    }

    @Override
    public void removeAttribute(String name) {
        this.request.removeAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributeMap() {
        Map<String, Object> attributes = new HashMap<>();
        for (String value : new EnumerationIterator<>(this.request.getAttributeNames())) {
            attributes.put(value, this.request.getAttribute(value));
        }
        return attributes;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return new InetSocketAddress(this.request.getRemoteHost(), this.request.getRemotePort());
    }

    @Override
    public Locale getLocale() {
        return this.request.getLocale();
    }

    @Override
    public HttpServletRequest getRawRequest() {
        return this.request;
    }
}
