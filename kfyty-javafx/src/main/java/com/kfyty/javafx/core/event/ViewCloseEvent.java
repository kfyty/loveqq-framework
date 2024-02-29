package com.kfyty.javafx.core.event;

import com.kfyty.core.event.ApplicationEvent;
import javafx.scene.Node;

/**
 * 描述: 窗口关闭事件
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class ViewCloseEvent extends ApplicationEvent<Node> {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ViewCloseEvent(Node source) {
        super(source);
    }
}
