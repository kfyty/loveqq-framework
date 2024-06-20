package org.springframework.beans.factory;

/**
 * 描述: 避免运行错误，无实际功能实现
 *
 * @author kfyty725
 * @date 2021/7/29 13:07
 * @email kfyty725@hotmail.com
 */
public interface InitializingBean {
    void afterPropertiesSet();
}
