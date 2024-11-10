package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter;

import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestHeader;
import feign.MethodMetadata;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/11/7 20:14
 * @email kfyty725@hotmail.com
 */
public class RequestHeaderParameterAnnotationProcessor extends AbstractAnnotationProcessor<RequestHeader> {

    public RequestHeaderParameterAnnotationProcessor(LoveqqMvcContract contract, PlaceholdersResolver placeholdersResolver) {
        super(contract, placeholdersResolver);
    }

    @Override
    public void process(RequestHeader annotation, MethodMetadata metadata, int paramIndex) {
        Parameter parameter = metadata.method().getParameters()[paramIndex];
        String name = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : parameter.getName();
        if (Map.class.isAssignableFrom(parameter.getType())) {
            metadata.headerMapIndex(paramIndex);
            return;
        }
        Collection<String> headers = Optional.ofNullable(metadata.template().headers().get(name)).orElseGet(LinkedList::new);
        headers.add(String.format("{%s}", name));
        metadata.template().header(name, headers);
        this.contract.nameParam(metadata, name, paramIndex);
    }
}
