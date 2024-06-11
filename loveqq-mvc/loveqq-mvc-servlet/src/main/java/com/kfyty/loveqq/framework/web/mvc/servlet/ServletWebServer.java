package com.kfyty.loveqq.framework.web.mvc.servlet;

import com.kfyty.loveqq.framework.web.core.WebServer;
import jakarta.servlet.ServletContext;

/**
 * 描述: web 服务器
 *
 * @author kfyty725
 * @date 2021/5/28 14:49
 * @email kfyty725@hotmail.com
 */
public interface ServletWebServer extends WebServer {
    /**
     * 获取 {@link ServletContext}
     *
     * @return {@link ServletContext}
     */
    ServletContext getServletContext();
}
