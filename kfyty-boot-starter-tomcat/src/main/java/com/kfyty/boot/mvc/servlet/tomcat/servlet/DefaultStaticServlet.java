package com.kfyty.boot.mvc.servlet.tomcat.servlet;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.servlets.DefaultServlet;

/**
 * 描述: 静态资源处理
 *
 * @author kfyty725
 * @date 2021/5/28 14:51
 * @email kfyty725@hotmail.com
 */
public class DefaultStaticServlet extends DefaultServlet {

    @Override
    protected String getRelativePath(HttpServletRequest request, boolean allowEmptyPath) {
        String relativePath = super.getRelativePath(request, allowEmptyPath);
        if (relativePath.startsWith("/static")) {
            return relativePath;
        }
        return "/static" + relativePath;
    }
}
