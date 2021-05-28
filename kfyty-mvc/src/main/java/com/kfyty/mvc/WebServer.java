package com.kfyty.mvc;

/**
 * 描述: web 服务器
 *
 * @author kfyty725
 * @date 2021/5/28 14:49
 * @email kfyty725@hotmail.com
 */
public interface WebServer {

    void start();

    void stop();

    int getPort();
}
