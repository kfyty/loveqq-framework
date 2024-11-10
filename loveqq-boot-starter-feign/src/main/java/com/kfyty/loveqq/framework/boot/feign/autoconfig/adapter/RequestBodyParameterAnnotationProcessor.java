package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter;

import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestBody;
import feign.MethodMetadata;
import feign.Types;

import java.lang.reflect.Parameter;

/**
 * 描述: {@link RequestBody} 需添加默认 content-type，否则会作为 form-data 编码
 *
 * @author kfyty725
 * @date 2024/11/7 20:14
 * @email kfyty725@hotmail.com
 */
public class RequestBodyParameterAnnotationProcessor extends AbstractAnnotationProcessor<RequestBody> {

    public RequestBodyParameterAnnotationProcessor(LoveqqMvcContract contract, PlaceholdersResolver placeholdersResolver) {
        super(contract, placeholdersResolver);
    }

    @Override
    public void process(RequestBody annotation, MethodMetadata metadata, int paramIndex) {
        Parameter parameter = metadata.method().getParameters()[paramIndex];
        metadata.bodyIndex(paramIndex);
        metadata.bodyType(Types.resolve(metadata.targetType(), metadata.targetType(), parameter.getParameterizedType()));
        metadata.template().header("Content-Type", "application/json; charset=utf-8");
        this.contract.nameParam(metadata, parameter.getName(), paramIndex);
    }
}
