package com.kfyty.mvc.util;

import com.kfyty.support.utils.CommonUtil;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 功能描述: servlet 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/9/11 16:07
 * @since JDK 1.8
 */
public class ServletUtil {
    private static final String CURRENT_REQUEST_PARAM = "__CURRENT__REQUEST__PARAM__" + UUID.randomUUID();

    /**
     * 将请求参数备份一下，以支持 PUT/DELETE 方法
     */
    public static void preparedRequestParam(HttpServletRequest request) throws IOException {
        request.setAttribute(CURRENT_REQUEST_PARAM, getRequestBody(request));
    }

    /**
     * 获取请求的数据
     * 如果是上传文件，则只获取 ParameterMap 的数据
     */
    public static String getRequestBody(HttpServletRequest request) throws IOException {
        String currentRequestParam = (String) request.getAttribute(CURRENT_REQUEST_PARAM);
        if(CommonUtil.notEmpty(currentRequestParam)) {
            return currentRequestParam;
        }
        String contentType = request.getHeader("content-type");
        if(contentType != null && contentType.startsWith("multipart/form-data")) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                for (String s : entry.getValue()) {
                    builder.append(entry.getKey()).append("=").append(s).append("&");
                }
            }
            return builder.length() < 1 ? "" : builder.deleteCharAt(builder.length() - 1).toString();
        }
        int contentLength = request.getContentLength();
        if(contentLength < 0) {
            return "";
        }
        byte[] buffer = new byte[contentLength];
        ServletInputStream inputStream = request.getInputStream();
        for (int n = 0, i = 0; n != -1 && i < contentLength; i += n) {
            n = inputStream.read(buffer, i, contentLength - i);
        }
        return new String(buffer, StandardCharsets.UTF_8);
    }

    /**
     * 根据参数名获取单一的参数值
     * 如果获取不到则尝试从备份数据中获取
     */
    public static String getParameter(HttpServletRequest request, String paramName) throws IOException {
        String param = request.getParameter(paramName);
        return CommonUtil.notEmpty(param) ? param : tryGetParameter(request, paramName);
    }

    /**
     * 根据参数名获取重复的参数值
     * 如果获取不到则尝试从备份数据中获取
     */
    public static List<String> getParameters(HttpServletRequest request, String paramName) throws IOException {
        String[] params = request.getParameterMap().get(paramName);
        if(params != null && params.length > 0) {
            return Arrays.stream(params).collect(Collectors.toList());
        }
        return tryGetParameters(request, paramName);
    }

    /**
     * 获取 ParametersMap，如果获取不到则尝试从备份数据中获取
     */
    public static Map<String, Object> getRequestParametersMap(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        Enumeration<?> parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            String paramName = (String) parameterNames.nextElement();
            map.put(paramName, request.getParameter(paramName));
        }
        return CommonUtil.notEmpty(map) ? map : tryGetRequestParametersMap(request);
    }

    /**
     * 根据参数名前缀获取 ParametersMap，如果获取不到则尝试从备份数据中获取
     */
    public static Map<String, Object> getRequestParametersMap(HttpServletRequest request, String prefix) {
        if(CommonUtil.empty(prefix)) {
            return getRequestParametersMap(request);
        }
        prefix += ".";
        Map<String, Object> map = new HashMap<>();
        Enumeration<?> parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            String paramName = (String) parameterNames.nextElement();
            if(paramName.startsWith(prefix)) {
                map.put(paramName.replace(prefix, ""), request.getParameter(paramName));
            }
        }
        return CommonUtil.notEmpty(map) ? map : tryGetRequestParametersMap(request, prefix);
    }

    private static String tryGetParameter(HttpServletRequest request, String paramName) throws IOException {
        String[] split = ((String) request.getAttribute(CURRENT_REQUEST_PARAM)).split("&");
        if(CommonUtil.empty(split)) {
            return null;
        }
        for (String s : split) {
            String[] paramMap = s.split("=");
            if(CommonUtil.empty(paramMap) || paramMap.length < 2) {
                continue;
            }
            if(paramMap[0].equals(paramName)) {
                return URLDecoder.decode(paramMap[1], "UTF-8");
            }
        }
        return null;
    }

    public static List<String> tryGetParameters(HttpServletRequest request, String paramName) throws IOException {
        List<String> values = new ArrayList<>();
        String[] split = ((String) request.getAttribute(CURRENT_REQUEST_PARAM)).split("&");
        if(CommonUtil.empty(split)) {
            return values;
        }
        for (String s : split) {
            String[] paramMap = s.split("=");
            if(CommonUtil.empty(paramMap) || paramMap.length < 2) {
                continue;
            }
            if(paramMap[0].equals(paramName)) {
                values.add(URLDecoder.decode(paramMap[1], "UTF-8"));
            }
        }
        return values;
    }

    private static Map<String, Object> tryGetRequestParametersMap(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        String[] split = ((String) request.getAttribute(CURRENT_REQUEST_PARAM)).split("&");
        if(CommonUtil.empty(split)) {
            return map;
        }
        for (String s : split) {
            String[] paramMap = s.split("=");
            if(CommonUtil.empty(paramMap) || paramMap.length < 2) {
                continue;
            }
            map.put(paramMap[0], paramMap[1]);
        }
        return map;
    }

    private static Map<String, Object> tryGetRequestParametersMap(HttpServletRequest request, String prefix) {
        Map<String, Object> map = new HashMap<>();
        String[] split = ((String) request.getAttribute(CURRENT_REQUEST_PARAM)).split("&");
        if(CommonUtil.empty(split)) {
            return map;
        }
        for (String s : split) {
            String[] paramMap = s.split("=");
            if(CommonUtil.empty(paramMap) || paramMap.length < 2) {
                continue;
            }
            if(paramMap[0].startsWith(prefix)) {
                map.put(paramMap[0].replace(prefix, ""), paramMap[1]);
            }
        }
        return map;
    }
}
