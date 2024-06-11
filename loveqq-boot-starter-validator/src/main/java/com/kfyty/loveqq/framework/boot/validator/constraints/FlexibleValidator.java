package com.kfyty.loveqq.framework.boot.validator.constraints;

/**
 * 描述: 校验器
 *
 * @author kfyty725
 * @date 2022/10/17 10:02
 * @email kfyty725@hotmail.com
 */
public interface FlexibleValidator<T> {
    /**
     * 校验逻辑
     * 实现必须线程安全
     *
     * @param target 目标对象
     * @return true if valid passed
     */
    boolean onValid(T target);

    /**
     * 校验逻辑
     * 实现必须线程安全
     *
     * @param target 目标对象
     * @return true if valid passed
     */
    @SuppressWarnings("unchecked")
    default boolean valid(Object target) {
        if (target == null) {
            return true;
        }
        return this.onValid((T) target);
    }
}
