package com.kfyty.loveqq.framework.javafx.core.binder;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import javafx.beans.value.WritableObjectValue;
import javafx.beans.value.WritableValue;

import java.util.Map;

/**
 * 描述: 集合绑定器
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class MapPropertyViewBinder implements ViewPropertyBinder {

    @Override
    public boolean support(WritableValue<?> view, Class<?> target) {
        if (!WritableObjectValue.class.isAssignableFrom(target)) {
            return false;
        }
        return view.getValue() instanceof Map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bind(WritableValue<?> view, Object model) {
        Map<Object, Object> target = (Map<Object, Object>) view.getValue();
        Map<Object, Object> source = (Map<Object, Object>) model;
        if (target == model || CommonUtil.empty(target) && CommonUtil.empty(source)) {
            return;
        }
        target.clear();
        target.putAll(source);
    }
}
