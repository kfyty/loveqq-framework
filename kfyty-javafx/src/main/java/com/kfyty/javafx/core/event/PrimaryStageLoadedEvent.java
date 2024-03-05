package com.kfyty.javafx.core.event;

import com.kfyty.core.event.ApplicationEvent;
import javafx.stage.Stage;

/**
 * 描述: 主场景加载完成事件
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class PrimaryStageLoadedEvent extends ApplicationEvent<Stage> {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public PrimaryStageLoadedEvent(Stage source) {
        super(source);
    }
}
