package com.kfyty.core.autoconfig.beans.autowired;

import com.kfyty.core.utils.ReflectUtil;
import lombok.RequiredArgsConstructor;

/**
 * 描述: 自动注入描述
 *
 * @author kfyty725
 * @date 2022/7/24 14:05
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class AutowiredDescription {
    /**
     * jakarta-annotation api 是否可用
     */
    public static boolean JAKARTA_AVAILABLE = ReflectUtil.isPresent("jakarta.annotation.Resource");

    /**
     * jakarta-inject api 是否可用
     */
    public static boolean INJECT_AVAILABLE = ReflectUtil.isPresent("jakarta.inject.Inject");

    /**
     * bean name
     */
    private final String value;

    /**
     * 是否必须
     */
    private boolean required;

    /**
     * 是否延迟加载
     */
    private boolean lazied;

    public AutowiredDescription(String value, boolean required) {
        this.value = value;
        this.required = required;
    }

    public String value() {
        return this.value;
    }

    public boolean required() {
        return this.required;
    }

    public boolean lazied() {
        return this.lazied;
    }

    public AutowiredDescription markRequired(boolean isRequired) {
        this.required = isRequired;
        return this;
    }

    public AutowiredDescription markLazied(boolean isLazied) {
        this.lazied = isLazied;
        return this;
    }

    public static boolean isRequired(AutowiredDescription description) {
        return description == null || description.required();
    }

    public static boolean isLazied(AutowiredDescription description) {
        return description != null && description.lazied();
    }
}
