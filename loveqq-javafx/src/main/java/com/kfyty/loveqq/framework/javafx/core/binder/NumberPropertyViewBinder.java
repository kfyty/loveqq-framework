package com.kfyty.loveqq.framework.javafx.core.binder;

import javafx.beans.value.WritableNumberValue;
import javafx.beans.value.WritableValue;

/**
 * 描述: 数值绑定器
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class NumberPropertyViewBinder implements ViewPropertyBinder {

    @Override
    public boolean support(WritableValue<?> view, Class<?> target) {
        return WritableNumberValue.class.isAssignableFrom(target);
    }

    @Override
    public void bind(WritableValue<?> view, Object model) {
        ((WritableNumberValue) view).setValue(model == null ? null : Double.parseDouble(model.toString()));
    }
}
