package com.kfyty.boot.template.thymeleaf.expression;

import com.kfyty.core.utils.IOC;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.standard.expression.IStandardVariableExpression;
import org.thymeleaf.standard.expression.IStandardVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.OGNLVariableExpressionEvaluator;
import org.thymeleaf.standard.expression.SelectionVariableExpression;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import org.thymeleaf.standard.expression.VariableExpression;

/**
 * 描述: thymeleaf 表达式解析器，支持 @ 获取 bean
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
public class DefaultVariableExpressionEvaluator implements IStandardVariableExpressionEvaluator {
    public static final char CUSTOMIZE_EXPRESS = '@';

    private final IStandardVariableExpressionEvaluator expressionEvaluator;

    public DefaultVariableExpressionEvaluator() {
        this(new OGNLVariableExpressionEvaluator(true));
    }

    public DefaultVariableExpressionEvaluator(IStandardVariableExpressionEvaluator expressionEvaluator) {
        this.expressionEvaluator = expressionEvaluator != null ? expressionEvaluator : new OGNLVariableExpressionEvaluator(true);
    }

    @Override
    public Object evaluate(IExpressionContext context, IStandardVariableExpression expression, StandardExpressionExecutionContext expContext) {
        String realExpress = expression.getExpression();
        int index = realExpress.indexOf(CUSTOMIZE_EXPRESS);
        if (index < 0) {
            return this.expressionEvaluator.evaluate(context, expression, expContext);
        }

        int start = 0;
        char[] charArray = expression.getExpression().toCharArray();
        StringBuilder builder = new StringBuilder();
        while (index > -1) {
            builder.append(charArray, start, index - start);
            int beanNameIndex = realExpress.indexOf('.', index);
            String beanName = realExpress.substring(index + 1, beanNameIndex);
            builder.append(CUSTOMIZE_EXPRESS).append(IOC.class.getName())
                    .append(CUSTOMIZE_EXPRESS).append("getBean('").append(beanName).append("')");
            start = beanNameIndex;
            index = realExpress.indexOf(CUSTOMIZE_EXPRESS, start);
        }

        builder.append(charArray, start, charArray.length - start);

        if (expression instanceof VariableExpression) {
            return this.expressionEvaluator.evaluate(context, new VariableExpression(builder.toString(), ((VariableExpression) expression).getConvertToString()), expContext);
        }
        if (expression instanceof SelectionVariableExpression) {
            return this.expressionEvaluator.evaluate(context, new SelectionVariableExpression(builder.toString(), ((SelectionVariableExpression) expression).getConvertToString()), expContext);
        }

        return this.expressionEvaluator.evaluate(context, expression, expContext);
    }
}
