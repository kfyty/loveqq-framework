package com.kfyty.loveqq.framework.javafx.core.event;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.support.Pair;
import javafx.scene.Node;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

/**
 * 描述: 窗口关闭事件监听器
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class ViewCloseEventListener implements ApplicationListener<ViewCloseEvent> {
    @Autowired(FEventListenerAdapter.BEAN_NAME)
    private FEventListenerAdapter eventListenerAdapter;

    @Override
    public synchronized void onApplicationEvent(ViewCloseEvent event) {
        Map<String, Queue<Pair<Node, Object>>> viewController = this.eventListenerAdapter.getViewController();
        for (Iterator<Map.Entry<String, Queue<Pair<Node, Object>>>> i = viewController.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, Queue<Pair<Node, Object>>> entry = i.next();
            entry.getValue().removeIf(e -> e.getKey() == event.getSource());
            if (entry.getValue().isEmpty()) {
                i.remove();
            }
        }
    }
}
