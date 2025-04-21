package com.kfyty.loveqq.framework.boot.validator.context;

import com.kfyty.loveqq.framework.boot.validator.agent.ValidatorValueContextInstrumentation;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;

/**
 * 描述: 检验器上下文
 *
 * @author kfyty725
 * @date 2023/4/14 15:57
 * @email kfyty725@hotmail.com
 */
public abstract class ValidatorContext {
    /**
     * 校验器，由应用注入
     *
     * @see Validator
     */
    @Getter
    @Setter
    private static Validator validator;

    /**
     * 当前校验值上下文，由 javaagent 注入
     *
     * @see ValueContext
     * @see ValidatorValueContextInstrumentation
     */
    private static final ThreadLocal<ValueContext<?, ?>> VALUE_CONTEXT = new ThreadLocal<>();

    public static ValueContext<?, ?> getValueContext() {
        return VALUE_CONTEXT.get();
    }

    public static void setValueContext(ValueContext<?, ?> valueContext) {
        VALUE_CONTEXT.set(valueContext);
    }
}
