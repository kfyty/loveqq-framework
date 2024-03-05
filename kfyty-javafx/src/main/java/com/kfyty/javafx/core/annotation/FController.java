package com.kfyty.javafx.core.annotation;

import javafx.scene.Scene;
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
     * {@link this#window()} == true 或 {@link this#main()} == true 有效
     *
     * @return css
     */
    String[] css() default {};

    /**
     * fxml 最终要包装的组件类型，默认是 {@link Stage} 类型
     * 当 {@link this#main()} 返回 true，该设置无效，强制返回 {@link Scene} 类型
     * 当 {@link this#window()} 返回 true，该设置无效，强制返回 {@link Stage} 类型
     *
     * <p><b>当不满足上两条时，应返回 fxml 文件的根节点组件类型</b></p>
     *
     * @return Bean type
     */
    Class<?> componentType() default Stage.class;

    /**
     * StageStyle
     * {@link this#window()} == true 或 {@link this#main()} == true 有效
     *
     * @return StageStyle
     */
    StageStyle stageStyle() default StageStyle.DECORATED;

    /**
     * 窗口标题
     * {@link this#window()} == true 或 {@link this#main()} == true 有效
     *
     * @return title
     */
    String title() default "";

    /**
     * 窗口大小是否可变
     * {@link this#window()} == true 或 {@link this#main()} == true 有效
     *
     * @return resizable
     */
    boolean resizable() default true;

    /**
     * 是否全屏
     * {@link this#window()} == true 或 {@link this#main()} == true 有效
     *
     * @return fullScreen
     */
    boolean fullScreen() default false;

    /**
     * 是否在顶部
     * {@link this#window()} == true 或 {@link this#main()} == true 有效
     *
     * @return alwaysOnTop
     */
    boolean alwaysOnTop() default false;

    /**
     * icon 资源路径
     * {@link this#window()} == true 或 {@link this#main()} == true 有效
     *
     * @return icon
     */
    String icon() default "";

    /**
     * 是否显示
     * {@link this#window()} == true 有效
     *
     * @return show
     */
    boolean show() default false;

    /**
     * fxml 是否返回一个窗口对象
     * 返回 true 时，将包装为 {@link Stage}
     * 返回 false 时，将返回 fxml 文件的根节点组件类型
     *
     * @return true if window
     */
    boolean window() default true;

    /**
     * fxml 是否是主场景，返回 true 时，{@link this#window()} 无效，{@link this#show()} 无效，并包装为 {@link javafx.scene.Scene}
     *
     * @return true if main window
     */
    boolean main() default false;
}
