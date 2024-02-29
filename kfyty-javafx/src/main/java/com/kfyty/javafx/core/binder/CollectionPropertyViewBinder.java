package com.kfyty.javafx.core.binder;

import javafx.beans.value.WritableObjectValue;
import javafx.beans.value.WritableValue;

import java.util.Collection;

/**
 * 描述: 集合绑定器
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class CollectionPropertyViewBinder implements ViewPropertyBinder {

    @Override
    public boolean support(WritableValue<?> view, Class<?> target) {
        if (!WritableObjectValue.class.isAssignableFrom(target)) {
            return false;
        }
        return view.getValue() instanceof Collection;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bind(WritableValue<?> view, Object model) {
        Collection<Object> target = (Collection<Object>) view.getValue();
        Collection<Object> source = (Collection<Object>) model;
        if (target == model) {
            return;
        }
        target.clear();
        target.addAll(source);
    }
}
