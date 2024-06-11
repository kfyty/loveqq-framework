package com.kfyty.loveqq.framework.core.support.json;

import java.util.List;
import java.util.Map;

/**
 * 描述: JSON Aware
 *
 * @author kfyty725
 * @date 2022/7/17 18:46
 * @email kfyty725@hotmail.com
 */
public interface JSONAware {

    @SuppressWarnings("unchecked")
    default JSON getJSON(Object o, String msg) {
        if (o == null || o instanceof JSON) {
            return (JSON) o;
        }
        if (o instanceof Map<?, ?>) {
            return new JSON((Map<String, Object>) o);
        }
        throw new IllegalStateException(msg);
    }

    @SuppressWarnings("unchecked")
    default Array getArray(Object o, String msg) {
        if (o == null || o instanceof Array) {
            return (Array) o;
        }
        if (o instanceof List<?>) {
            return new Array((List<Object>) o);
        }
        throw new IllegalStateException(msg);
    }
}
