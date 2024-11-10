package com.kfyty.loveqq.framework.boot.feign.autoconfig.adapter;

import com.kfyty.loveqq.framework.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.annotation.RequestMapping;
import feign.MethodMetadata;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/11/7 20:14
 * @email kfyty725@hotmail.com
 */
public class RequestMappingClassAnnotationProcessor extends AbstractAnnotationProcessor<RequestMapping> {

    public RequestMappingClassAnnotationProcessor(LoveqqMvcContract contract, PlaceholdersResolver placeholdersResolver) {
        super(contract, placeholdersResolver);
    }

    @Override
    public void process(RequestMapping annotation, MethodMetadata metadata) {
        if (CommonUtil.notEmpty(annotation.value())) {
            String pathValue = this.resolve(annotation.value());
            metadata.template().uri(CommonUtil.formatURI(pathValue));
        }
    }
}
