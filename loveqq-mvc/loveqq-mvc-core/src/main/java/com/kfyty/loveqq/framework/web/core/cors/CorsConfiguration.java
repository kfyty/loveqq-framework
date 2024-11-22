package com.kfyty.loveqq.framework.web.core.cors;

import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import com.kfyty.loveqq.framework.web.core.http.ServerRequest;
import com.kfyty.loveqq.framework.web.core.http.ServerResponse;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * 描述: cors 配置
 *
 * @author kfyty725
 * @date 2021/5/28 14:49
 * @email kfyty725@hotmail.com
 */
@Data
public class CorsConfiguration {
    /**
     * 路径匹配器
     */
    private PatternMatcher patternMatcher;

    /**
     * 需要跨域的 uri，基于 ant 匹配
     */
    private List<String> patternPaths = Collections.emptyList();

    /**
     * 允许的域名，* 表示全部
     */
    private List<String> allowOrigin = Collections.emptyList();

    /**
     * 允许的 http 方法
     */
    private List<String> allowedMethods = Collections.emptyList();

    /**
     * 浏览器会额外发送的请求头
     */
    private List<String> allowedHeaders = Collections.emptyList();

    /**
     * 允许响应给浏览器的其他响应头
     */
    private List<String> exposeHeaders = Collections.emptyList();

    /**
     * 是否允许浏览器发送 cookie
     */
    private Boolean allowCredentials;

    /**
     * 时间
     */
    private Integer maxAge;

    @SuppressWarnings("rawtypes")
    public CorsConfiguration addPatternPath(String... paths) {
        if (this.patternPaths == (List) Collections.emptyList()) {
            this.patternPaths = new LinkedList<>();
        }
        this.patternPaths.addAll(Arrays.asList(paths));
        return this;
    }

    @SuppressWarnings("rawtypes")
    public CorsConfiguration addAllowedOrigins(String... origins) {
        if (this.allowOrigin == (List) Collections.emptyList()) {
            this.allowOrigin = new LinkedList<>();
        }
        this.allowOrigin.addAll(Arrays.asList(origins));
        return this;
    }

    @SuppressWarnings("rawtypes")
    public CorsConfiguration addAllowedMethods(String... methods) {
        if (this.allowedMethods == (List) Collections.emptyList()) {
            this.allowedMethods = new LinkedList<>();
        }
        this.allowedMethods.addAll(Arrays.asList(methods));
        return this;
    }

    @SuppressWarnings("rawtypes")
    public CorsConfiguration addAllowedHeaders(String... headers) {
        if (this.allowedHeaders == (List) Collections.emptyList()) {
            this.allowedHeaders = new LinkedList<>();
        }
        this.allowedHeaders.addAll(Arrays.asList(headers));
        return this;
    }

    @SuppressWarnings("rawtypes")
    public CorsConfiguration addExposeHeaders(String... headers) {
        if (this.exposeHeaders == (List) Collections.emptyList()) {
            this.exposeHeaders = new LinkedList<>();
        }
        this.exposeHeaders.addAll(Arrays.asList(headers));
        return this;
    }

    public PatternMatcher getPatternMatcher() {
        if (this.patternMatcher == null) {
            synchronized (this) {
                if (this.patternMatcher == null) {
                    this.patternMatcher = new AntPathMatcher();
                }
            }
        }
        return this.patternMatcher;
    }

    /**
     * 检测是否应该应用跨域配置
     *
     * @param requestUri 请求 uri
     * @return true/false
     */
    public boolean shouldApplyCors(String requestUri) {
        if (this.patternPaths.isEmpty()) {
            return true;
        }
        PatternMatcher patternMatcher = this.getPatternMatcher();
        for (String patternPath : this.patternPaths) {
            if (patternMatcher.matches(patternPath, requestUri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 应用当前跨域配置到请求/响应
     *
     * @param request  请求
     * @param response 响应
     */
    public void apply(ServerRequest request, ServerResponse response) {
        if (!this.shouldApplyCors(request.getRequestURI())) {
            return;
        }
        Optional<String> optionalOrigin = Optional.of("*");
        if (!this.allowOrigin.contains("*")) {
            String requestOrigin = request.getHeader("Origin");
            optionalOrigin = this.allowOrigin.stream().filter(e -> e.equals(requestOrigin)).findAny();
            if (!optionalOrigin.isPresent()) {
                return;
            }
        }
        response.addHeader("Access-Control-Allow-Origin", optionalOrigin.get());
        response.addHeader("Access-Control-Allow-Methods", String.join(",", this.allowedMethods));
        response.addHeader("Access-Control-Allow-Headers", String.join(",", this.allowedHeaders));
        response.addHeader("Access-Control-Expose-Headers", String.join(",", this.exposeHeaders));
        if (this.allowCredentials != null && this.allowCredentials) {
            response.addHeader("Access-Control-Allow-Credentials", Boolean.TRUE.toString());
        }
        if (this.maxAge != null) {
            response.addHeader("Access-Control-Max-Age", Integer.toString(this.maxAge));
        }
    }
}
