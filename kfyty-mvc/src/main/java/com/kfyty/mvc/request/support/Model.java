package com.kfyty.mvc.request.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/10 11:50
 * @email kfyty725@hotmail.com
 */
public class Model extends LinkedHashMap<String, Object> {

    public Model addAttribute(String attributeName, Object attributeValue) {
        Objects.requireNonNull(attributeName, "Model attribute name must not be null");
        put(attributeName, attributeValue);
        return this;
    }

    public Model addAllAttributes(Map<String, ?> attributes) {
        if (attributes != null) {
            putAll(attributes);
        }
        return this;
    }
}
