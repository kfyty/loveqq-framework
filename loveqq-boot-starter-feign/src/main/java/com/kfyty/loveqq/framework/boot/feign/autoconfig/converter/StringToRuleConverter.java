package com.kfyty.loveqq.framework.boot.feign.autoconfig.converter;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.converter.Converter;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.netflix.loadbalancer.IRule;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/8/13 20:15
 * @email kfyty725@hotmail.com
 */
@Component
public class StringToRuleConverter implements Converter<String, IRule> {

    @Override
    public IRule apply(String source) {
        return CommonUtil.empty(source) ? null : (IRule) ReflectUtil.newInstance(ReflectUtil.load(source));
    }
}
