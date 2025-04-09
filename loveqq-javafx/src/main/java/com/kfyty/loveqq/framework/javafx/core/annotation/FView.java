package com.kfyty.loveqq.framework.javafx.core.annotation;

import com.kfyty.loveqq.framework.javafx.core.AbstractViewModelBindCapableController;
import com.kfyty.loveqq.framework.javafx.core.binder.ViewPropertyBinder;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标识 FXML 视图，实现双向绑定
 * 由于 fxml 的 fx:id 必须是属性名称，因此无需定义 id 元数据
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.FIELD)
@Repeatable(FView.FViews.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface FView {
    /**
     * view 绑定目标模型路径，eg: text:user.name
     * 当 {@link this#method()} 返回 false 时，表示获取 text 属性，绑定到 user.name
     * 当 {@link this#method()} 返回 true 时，表示调用 text() 方法，绑定到 user.name
     * <p>
     * {@link this#method()} 返回 true 时，必须都是无参方法
     * <p>
     * <b>最终返回值必须是 {@link javafx.beans.value.ObservableValue} 类型</b>
     *
     * @see AbstractViewModelBindCapableController
     * @see ViewPropertyBinder
     */
    String value();

    /**
     * 表示获取要绑定的视图是使用属性解析还是方法解析
     *
     * @return true is field
     */
    boolean method() default false;

    /**
     * 重复注解支持
     */
    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface FViews {
        FView[] value();
    }
}
