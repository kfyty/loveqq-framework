package com.kfyty.mvc.util;

import com.kfyty.util.CommonUtil;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 功能描述: servlet 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/11 16:07
 * @since JDK 1.8
 */
public class ServletUtil {
    private static final String CURRENT_REQUEST_PARAM = "__CURRENT__REQUEST__PARAM__" + UUID.randomUUID();

    public static void preparedRequestParam(HttpServletRequest request) throws IOException {
        request.setAttribute(CURRENT_REQUEST_PARAM, getRequestJson(request));
    }

    public static String getRequestJson(HttpServletRequest request) throws IOException {
        String currentRequestParam = (String) request.getAttribute(CURRENT_REQUEST_PARAM);
        if(!CommonUtil.empty(currentRequestParam)) {
            return currentRequestParam;
        }
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

    public static String getParameter(HttpServletRequest request, String paramName) {
        String param = request.getParameter(paramName);
        return !CommonUtil.empty(param) ? param : tryGetParameter(request, paramName);
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
        return !CommonUtil.empty(map) ? map : tryGetRequestParametersMap(request);
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
        return !CommonUtil.empty(map) ? map : tryGetRequestParametersMap(request, prefix);
    }

    private static String tryGetParameter(HttpServletRequest request, String paramName) {
        String currentRequestParam = (String) request.getAttribute(CURRENT_REQUEST_PARAM);
        String[] split = currentRequestParam.split("&");
        if(CommonUtil.empty(split)) {
            return null;
        }
        for (String s : split) {
            String[] paramMap = s.split("=");
            if(CommonUtil.empty(paramMap) || paramMap.length < 2) {
                continue;
            }
            if(paramMap[0].equals(paramName)) {
                return paramMap[1];
            }
        }
        return null;
    }

    private static Map<String, Object> tryGetRequestParametersMap(HttpServletRequest request) {
        String currentRequestParam = (String) request.getAttribute(CURRENT_REQUEST_PARAM);
        String[] split = currentRequestParam.split("&");
        if(CommonUtil.empty(split)) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        for (String s : split) {
            String[] paramMap = s.split("=");
            if(CommonUtil.empty(paramMap) || paramMap.length < 2) {
                continue;
            }
            map.put(paramMap[0], paramMap[1]);
        }
        return CommonUtil.empty(map) ? null : map;
    }

    private static Map<String, Object> tryGetRequestParametersMap(HttpServletRequest request, String prefix) {
        String currentRequestParam = (String) request.getAttribute(CURRENT_REQUEST_PARAM);
        String[] split = currentRequestParam.split("&");
        if(CommonUtil.empty(split)) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        for (String s : split) {
            String[] paramMap = s.split("=");
            if(CommonUtil.empty(paramMap) || paramMap.length < 2) {
                continue;
            }
            if(paramMap[0].startsWith(prefix)) {
                map.put(paramMap[0].replace(prefix, ""), paramMap[1]);
            }
        }
        return CommonUtil.empty(map) ? null : map;
    }
}
