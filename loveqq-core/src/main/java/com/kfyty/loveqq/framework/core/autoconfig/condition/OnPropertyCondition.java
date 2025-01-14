package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;

import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/24 19:35
 * @email kfyty725@hotmail.com
 */
public class OnPropertyCondition implements Condition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        ConditionalOnProperty conditional = (ConditionalOnProperty) metadata.get();
        PropertyContext propertyContext = context.getBeanFactory().getBean(PropertyContext.class);
        String propertyKey = this.obtainPropertyKey(conditional);
        if (!propertyContext.contains(propertyKey)) {
            return conditional.matchIfMissing();
        }
        if (conditional.matchIfNonNull()) {
            return CommonUtil.notEmpty(propertyContext.getProperty(propertyKey));
        }
        return Objects.equals(propertyContext.getProperty(propertyKey), conditional.havingValue());
    }

    protected String obtainPropertyKey(ConditionalOnProperty conditional) {
        String prefix = ofNullable(conditional.prefix()).orElse("");
        if (CommonUtil.empty(prefix)) {
            return conditional.value();
        }
        return prefix.charAt(prefix.length() - 1) == '.' ? prefix + conditional.value() : prefix + '.' + conditional.value();
    }
}
