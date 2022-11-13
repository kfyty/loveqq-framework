package com.kfyty.core.autoconfig.beans.autowired;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.utils.AnnotationUtil;
import lombok.RequiredArgsConstructor;

import javax.annotation.Resource;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

import static com.kfyty.core.utils.CommonUtil.notEmpty;

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

    public static AutowiredDescription from(AccessibleObject accessibleObject) {
        Autowired autowired = AnnotationUtil.findAnnotation(accessibleObject, Autowired.class);
        if (autowired != null) {
            return from(autowired);
        }
        Resource resource = AnnotationUtil.findAnnotation(accessibleObject, Resource.class);
        if (resource != null) {
            return from(accessibleObject, resource);
        }
        return null;
    }

    public static AutowiredDescription from(Autowired autowired) {
        return autowired == null ? null : new AutowiredDescription(autowired.value(), autowired.required());
    }

    public static AutowiredDescription from(AccessibleObject accessibleObject, Resource resource) {
        String name = accessibleObject == null ? null : (accessibleObject instanceof Field ? ((Field) accessibleObject).getName() : ((Executable) accessibleObject).getName());
        String value = accessibleObject == null || notEmpty(resource.name()) ? resource.name() : name;
        return new AutowiredDescription(value, true);
    }
}
