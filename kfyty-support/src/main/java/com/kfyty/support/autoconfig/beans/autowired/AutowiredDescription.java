package com.kfyty.support.autoconfig.beans.autowired;

import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.utils.AnnotationUtil;
import lombok.RequiredArgsConstructor;

import javax.annotation.Resource;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

import static com.kfyty.support.utils.CommonUtil.notEmpty;

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
    private final boolean required;

    public String value() {
        return this.value;
    }

    public boolean required() {
        return this.required;
    }

    public static boolean isRequired(AutowiredDescription description) {
        return description == null || description.required();
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
