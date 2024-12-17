package com.kfyty.loveqq.framework.core.autoconfig.condition;

import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnExpression;
import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/4/24 19:35
 * @email kfyty725@hotmail.com
 */
public class OnExpressionCondition implements Condition {

    @Override
    public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
        ConditionalOnExpression conditional = (ConditionalOnExpression) metadata.get();
        if (conditional.value().isEmpty()) {
            return true;
        }
        Map<String, Object> root = new HashMap<>(context.getBeanFactory().getBean(PropertyContext.class).getProperties());
        if (root.putIfAbsent("beanFactory", context.getBeanFactory()) != null) {
            throw new ResolvableException("Conflict conditional property key: beanFactory");
        }
        return OgnlUtil.getBoolean(conditional.value(), Collections.unmodifiableMap(root));                             // 使用不可变包装，避免表达式错误
    }
}
