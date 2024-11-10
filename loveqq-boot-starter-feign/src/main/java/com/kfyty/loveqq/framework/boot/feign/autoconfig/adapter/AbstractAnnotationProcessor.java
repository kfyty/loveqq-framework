package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter;

import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import feign.DeclarativeContract;
import feign.MethodMetadata;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/11/7 20:14
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public abstract class AbstractAnnotationProcessor<E extends Annotation> implements DeclarativeContract.AnnotationProcessor<E>, DeclarativeContract.ParameterAnnotationProcessor<E> {
    protected final LoveqqMvcContract contract;
    protected final PlaceholdersResolver placeholdersResolver;

    @Override
    public void process(E annotation, MethodMetadata metadata) {
        // override by subclass
    }

    @Override
    public void process(E annotation, MethodMetadata metadata, int paramIndex) {
        // override by subclass
    }

    protected String resolve(String value) {
        if (this.placeholdersResolver == null) {
            return value;
        }
        return this.placeholdersResolver.resolvePlaceholders(value);
    }
}
