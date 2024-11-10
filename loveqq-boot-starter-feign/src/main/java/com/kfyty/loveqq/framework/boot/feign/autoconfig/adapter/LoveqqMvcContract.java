package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter;

import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.web.core.annotation.RequestMapping;
import com.kfyty.loveqq.framework.web.core.annotation.bind.PathVariable;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestBody;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestHeader;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestParam;
import feign.DeclarativeContract;
import feign.MethodMetadata;
import feign.RequestLine;

import java.lang.reflect.Method;

/**
 * 描述: 桥接 loveqq-mvc 注解支持
 *
 * @author kfyty725
 * @date 2024/10/24 22:05
 * @email kfyty725@hotmail.com
 */
public class LoveqqMvcContract extends DeclarativeContract {
    private final BaseContract defaultContract;

    public LoveqqMvcContract(BaseContract defaultContract, PlaceholdersResolver placeholdersResolver) {
        this.defaultContract = defaultContract;
        this.registerClassAnnotation(RequestMapping.class, new RequestMappingClassAnnotationProcessor(this, placeholdersResolver));
        this.registerMethodAnnotation(e -> true, new RequestMappingMethodAnnotationProcessor(this, placeholdersResolver));
        this.registerParameterAnnotation(RequestBody.class, new RequestBodyParameterAnnotationProcessor(this, placeholdersResolver));
        this.registerParameterAnnotation(PathVariable.class, new PathVariableParameterAnnotationProcessor(this, placeholdersResolver));
        this.registerParameterAnnotation(RequestParam.class, new RequestParamParameterAnnotationProcessor(this, placeholdersResolver));
        this.registerParameterAnnotation(RequestHeader.class, new RequestHeaderParameterAnnotationProcessor(this, placeholdersResolver));
    }

    @Override
    protected MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
        if (AnnotationUtil.hasAnnotation(method, RequestLine.class)) {
            return ReflectUtil.invokeMethod(this.defaultContract, "parseAndValidateMetadata", targetType, method);
        }
        return super.parseAndValidateMetadata(targetType, method);
    }

    @Override
    public void nameParam(MethodMetadata data, String name, int i) {
        super.nameParam(data, name, i);
    }
}
