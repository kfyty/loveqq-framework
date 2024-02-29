package com.kfyty.javafx.core.binder;

import javafx.beans.value.WritableStringValue;
import javafx.beans.value.WritableValue;

/**
 * 描述: 字符串绑定器
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class StringPropertyViewBinder implements ViewPropertyBinder {

    @Override
    public boolean support(WritableValue<?> view, Class<?> target) {
        return WritableStringValue.class.isAssignableFrom(target);
    }

    @Override
    public void bind(WritableValue<?> view, Object model) {
        ((WritableStringValue) view).setValue(model == null ? null : model.toString());
    }
}
