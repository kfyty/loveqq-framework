package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_STRING;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.EMPTY_STRING_ARRAY;

/**
 * 功能描述: url 映射
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/10 19:25
 * @since JDK 1.8
 */
@Data
@Slf4j
@NoArgsConstructor
public class MethodMapping {
    /**
     * URL
     */
    private String url;

    /**
     * url 路径
     */
    private String[] paths;

    /**
     * 请求方法
     */
    private RequestMethod requestMethod;

    /**
     * 响应的内容类型
     */
    private String produces;

    /**
     * 是否是 restful 风格 url
     */
    private boolean restful;

    /**
     * restful 风格 url 数据索引
     */
    private Pair<String, Integer>[] restfulMappingIndex;

    /**
     * 映射方法
     */
    private Method mappingMethod;

    /**
     * 映射方法所在的控制器实例
     */
    private Lazy<Object> controller;

    public static MethodMapping create(String url, RequestMethod requestMethod, Lazy<Object> controller, Method mappingMethod) {
        MethodMapping methodMapping = new MethodMapping();
        methodMapping.setController(controller);
        methodMapping.setMappingMethod(mappingMethod);
        methodMapping.setRequestMethod(requestMethod);
        methodMapping.setUrl(url);
        methodMapping.setPaths(CommonUtil.split(url, "[/]").toArray(EMPTY_STRING_ARRAY));
        resolveRestfulVariableIfNecessary(methodMapping);
        return methodMapping;
    }

    public Integer getLength() {
        return this.paths.length;
    }

    public boolean isEventStream() {
        return this.produces != null && this.produces.contains("text/event-stream");
    }

    public Integer getRestfulMappingIndex(String path) {
        if (this.restfulMappingIndex == null) {
            throw new IllegalArgumentException("The restful path index does not exists: restful=" + this.url + ", path=" + path);
        }
        for (Pair<String, Integer> urlMappingIndex : this.restfulMappingIndex) {
            if (Objects.equals(urlMappingIndex.getKey(), path)) {
                return urlMappingIndex.getValue();
            }
        }
        throw new IllegalArgumentException("The restful path index does not exists: restful=" + this.url + ", path=" + path);
    }

    public Object getController() {
        return this.controller.get();
    }

    public MethodParameter buildMethodParameter(Object[] parameters) {
        return new MethodParameter(this.getController(), this.mappingMethod, parameters);
    }

    @SuppressWarnings("unchecked")
    public static void resolveRestfulVariableIfNecessary(MethodMapping methodMapping) {
        if (!Routes.RESTFUL_URL_PATTERN.matcher(methodMapping.getUrl()).matches()) {
            return;
        }
        String[] paths = methodMapping.getPaths();
        List<Pair<String, Integer>> mappingIndex = new ArrayList<>();
        for (int i = 0; i < paths.length; i++) {
            if (CommonUtil.SIMPLE_PARAMETERS_PATTERN.matcher(paths[i]).matches()) {
                mappingIndex.add(new Pair<>(Routes.SIMPLE_BRACE_PATTERN.matcher(paths[i]).replaceAll(EMPTY_STRING), i));
            }
        }
        methodMapping.setRestful(true);
        methodMapping.setRestfulMappingIndex(mappingIndex.toArray(new Pair[0]));
    }
}
