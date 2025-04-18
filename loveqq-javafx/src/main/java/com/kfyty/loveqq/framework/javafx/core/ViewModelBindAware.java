package com.kfyty.loveqq.framework.javafx.core;

/**
 * 描述: 视图模型绑定感知接口
 * 视图模型绑定时，默认当模型的前后的 hashCode 不同时进行绑定，因此可能出现 hash 冲突
 * 当出现 hash 冲突时将无法自动更新视图，此时当设置模型数据后，可调用 {@link this#markBind()}，那么此时将忽略 hash 冲突进入绑定视图
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public interface ViewModelBindAware {
    /**
     * 是否标记了需要绑定
     *
     * @return true/false
     */
    boolean isMarkBind();

    /**
     * 标记需要进行绑定
     */
    void markBind();

    /**
     * 取消需要绑定的标记
     */
    void unmarkBind();
}
