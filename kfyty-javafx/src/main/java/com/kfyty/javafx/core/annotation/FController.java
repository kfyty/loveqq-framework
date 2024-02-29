package com.kfyty.javafx.core.annotation;

import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标识 FXML 控制器
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FController {
    /**
     * 控制器对应 fxml 的 fx:id 属性，会注册为 Bean 名称
     *
     * @return fx:id
     */
    String value() default "";

    /**
     * 控制器对应 fxml 的资源路径
     * 如果填写会自动加载入 ioc 容器
     *
     * @return fxml
     * @see this#componentType()
     */
    String path() default "";

    /**
     * 视图 css 文件
     *
     * @return css
     */
    String[] css() default {};

    /**
     * fxml 最终要包装的组件类型，默认是 {@link Stage}
     * 对于主场景，即 {@link javafx.application.Application#start(Stage)} 设置的场景，要设置为 {@link javafx.scene.Scene}，并手动调用 {@link com.kfyty.javafx.core.LifeCycleBinder#bindLifeCycle(Stage, Parent)}
     *
     * <b>如果既不是 Scene 也不是 Stage，会直接返回 fxml 文件的根节点组件类型</b>
     *
     * @return Bean type
     */
    Class<?> componentType() default Stage.class;

    /**
     * StageStyle
     *
     * @return StageStyle
     */
    StageStyle stageStyle() default StageStyle.DECORATED;

    /**
     * 窗口标题
     * {@link this#componentType()} == {@link Stage} 有效
     *
     * @return title
     */
    String title() default "";

    /**
     * 窗口大小是否可变
     * {@link this#componentType()} == {@link Stage} 有效
     *
     * @return resizable
     */
    boolean resizable() default true;

    /**
     * 是否全屏
     * {@link this#componentType()} == {@link Stage} 有效
     *
     * @return fullScreen
     */
    boolean fullScreen() default false;

    /**
     * 是否在顶部
     * {@link this#componentType()} == {@link Stage} 有效
     *
     * @return alwaysOnTop
     */
    boolean alwaysOnTop() default false;

    /**
     * icon 资源路径
     *
     * @return icon
     */
    String icon() default "";

    /**
     * 是否显示
     * {@link this#componentType()} == {@link Stage} 有效
     *
     * @return show
     */
    boolean show() default false;
}
