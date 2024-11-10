package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter;

import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.RequestParam;
import com.kfyty.loveqq.framework.web.core.multipart.MultipartFile;
import feign.MethodMetadata;

import java.io.File;
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
public class RequestParamParameterAnnotationProcessor extends AbstractAnnotationProcessor<RequestParam> {

    public RequestParamParameterAnnotationProcessor(LoveqqMvcContract contract, PlaceholdersResolver placeholdersResolver) {
        super(contract, placeholdersResolver);
    }

    @Override
    public void process(RequestParam annotation, MethodMetadata metadata, int paramIndex) {
        Parameter parameter = metadata.method().getParameters()[paramIndex];
        String name = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : parameter.getName();
        if (Map.class.isAssignableFrom(parameter.getType())) {
            metadata.queryMapIndex(paramIndex);
            return;
        }
        if (this.isMultipart(parameter)) {
            metadata.formParams().add(name);
            this.contract.nameParam(metadata, name, paramIndex);
            return;
        }
        Collection<String> queries = Optional.ofNullable(metadata.template().queries().get(name)).orElseGet(LinkedList::new);
        queries.add(String.format("{%s}", name));
        metadata.template().query(name, queries);
        this.contract.nameParam(metadata, name, paramIndex);
    }

    protected boolean isMultipart(Parameter parameter) {
        return parameter.getType() == byte[].class ||
                parameter.getType() == File[].class ||
                parameter.getType() == MultipartFile[].class ||
                File.class.isAssignableFrom(parameter.getType()) ||
                MultipartFile.class.isAssignableFrom(parameter.getType());
    }
}
