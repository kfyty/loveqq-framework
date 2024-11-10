package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter;

import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.bind.PathVariable;
import feign.MethodMetadata;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/11/7 20:14
 * @email kfyty725@hotmail.com
 */
public class PathVariableParameterAnnotationProcessor extends AbstractAnnotationProcessor<PathVariable> {

    public PathVariableParameterAnnotationProcessor(LoveqqMvcContract contract, PlaceholdersResolver placeholdersResolver) {
        super(contract, placeholdersResolver);
    }

    @Override
    public void process(PathVariable annotation, MethodMetadata metadata, int paramIndex) {
        String name = CommonUtil.notEmpty(annotation.value()) ? annotation.value() : metadata.method().getParameters()[paramIndex].getName();
        this.contract.nameParam(metadata, name, paramIndex);
    }
}
