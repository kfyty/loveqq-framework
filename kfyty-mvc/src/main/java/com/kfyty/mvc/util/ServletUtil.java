package com.kfyty.mvc.util;

import com.kfyty.util.CommonUtil;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述: servlet 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/11 16:07
 * @since JDK 1.8
 */
public class ServletUtil {
    public static String getRequestJson(HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if(contentLength < 0) {
            return null;
        }
        byte[] buffer = new byte[contentLength];
        ServletInputStream inputStream = request.getInputStream();
        for (int n = 0, i = 0; n != -1 && i < contentLength; i += n) {
            n = inputStream.read(buffer, i, contentLength - i);
        }
        return new String(buffer, "UTF-8");
    }

    public static Map<String, Object> getRequestParametersMap(HttpServletRequest request) {
        if(request == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        Enumeration parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            String paramName = (String) parameterNames.nextElement();
            map.put(paramName, request.getParameter(paramName));
        }
        return map;
    }

    public static Map<String, Object> getRequestParametersMap(HttpServletRequest request, String prefix) {
        if(request == null) {
            return null;
        }
        if(CommonUtil.empty(prefix)) {
            return getRequestParametersMap(request);
        }
        prefix += ".";
        Map<String, Object> map = new HashMap<>();
        Enumeration parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            String paramName = (String) parameterNames.nextElement();
            if(paramName.startsWith(prefix)) {
                map.put(paramName.replace(prefix, ""), request.getParameter(paramName));
            }
        }
        return !CommonUtil.empty(map) ? map : getRequestParametersMap(request);
    }
}
