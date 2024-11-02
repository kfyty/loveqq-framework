package com.kfyty.loveqq.framework.boot.dubbo.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Lazy;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredDescription;
import com.kfyty.loveqq.framework.core.autoconfig.beans.autowired.AutowiredDescriptionResolver;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import org.apache.dubbo.config.annotation.DubboReference;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.kfyty.loveqq.framework.boot.dubbo.autoconfig.DubboServiceRegistry.generateReferenceBeanName;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/10/29 20:31
 * @email kfyty725@hotmail.com
 */
public class DubboAutowiredDescriptionResolver implements AutowiredDescriptionResolver {

    @Override
    public AutowiredDescription resolve(AnnotatedElement element) {
        DubboReference dubboReference = AnnotationUtil.findAnnotation(element, DubboReference.class);
        if (dubboReference != null) {
            Class<?> clazz = element instanceof Field ? ((Field) element).getType() : ((Method) element).getReturnType();
            return new AutowiredDescription(generateReferenceBeanName(dubboReference, clazz), true).markLazied(hasAnnotation(element, Lazy.class));
        }
        return null;
    }
}
