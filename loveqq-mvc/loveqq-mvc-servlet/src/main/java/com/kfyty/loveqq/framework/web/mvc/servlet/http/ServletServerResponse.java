package com.kfyty.loveqq.framework.web.mvc.servlet.http;

import com.kfyty.loveqq.framework.core.utils.ExceptionUtil;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.Collection;

/**
 * 描述: servlet 实现
 *
 * @author kfyty725
 * @date 2024/7/6 18:51
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class ServletServerResponse implements ServerResponse {
    private final HttpServletResponse response;

    @Override
    public String getContentType() {
        return this.response.getContentType();
    }

    @Override
    public void setContentType(String type) {
        this.response.setContentType(type);
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            return this.response.getOutputStream();
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    @Override
    public void addCookie(HttpCookie cookie) {
        this.response.addCookie(new Cookie(cookie.getName(), cookie.getValue()));
    }

    @Override
    public Object sendRedirect(String location) {
        try {
            this.response.sendRedirect(location);
            return null;
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    @Override
    public void setHeader(String name, String value) {
        this.response.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.response.addHeader(name, value);
    }

    @Override
    public String getHeader(String name) {
        return this.response.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return this.response.getHeaders(name);
    }

    @Override
    public int getStatus() {
        return this.response.getStatus();
    }

    @Override
    public void setStatus(int sc) {
        this.response.setStatus(sc);
    }

    @Override
    public void flush() throws IOException {
        this.response.flushBuffer();
    }

    @Override
    public HttpServletResponse getRawResponse() {
        return this.response;
    }
}
