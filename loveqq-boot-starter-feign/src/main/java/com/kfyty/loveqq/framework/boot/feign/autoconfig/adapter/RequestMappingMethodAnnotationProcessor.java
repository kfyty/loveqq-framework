package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter;

import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.RequestMapping;
import feign.MethodMetadata;
import feign.Request;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/11/7 20:14
 * @email kfyty725@hotmail.com
 */
public class RequestMappingMethodAnnotationProcessor extends AbstractAnnotationProcessor<Annotation> {

    public RequestMappingMethodAnnotationProcessor(LoveqqMvcContract contract, PlaceholdersResolver placeholdersResolver) {
        super(contract, placeholdersResolver);
    }

    @Override
    public void process(Annotation annotation, MethodMetadata metadata) {
        if (!(annotation instanceof RequestMapping) && !annotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
            return;
        }
        RequestMapping methodMapping = AnnotationUtil.findAnnotation(metadata.method(), RequestMapping.class);

        // 请求方法
        metadata.template().method(Request.HttpMethod.valueOf(methodMapping.method().name()));

        // 请求路径
        String pathValue = CommonUtil.notEmpty(methodMapping.value()) ? methodMapping.value() : (methodMapping.strategy() == RequestMapping.Strategy.EMPTY ? CommonUtil.EMPTY_STRING : metadata.method().getName());
        metadata.template().uri(CommonUtil.formatURI(this.resolve(pathValue)), true);

        // 默认使用 application/x-www-form-urlencoded
        if (Objects.equals(methodMapping.produces(), RequestMapping.DEFAULT_PRODUCES)) {
            metadata.template().header("Content-Type", "application/x-www-form-urlencoded");
        } else {
            metadata.template().header("Content-Type", methodMapping.produces());
        }

        // default
        metadata.indexToExpander(new LinkedHashMap<>());
    }
}
