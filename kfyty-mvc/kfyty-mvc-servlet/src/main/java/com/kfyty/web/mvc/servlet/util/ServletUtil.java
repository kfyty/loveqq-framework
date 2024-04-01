package com.kfyty.web.mvc.servlet.util;

import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.web.mvc.core.multipart.MultipartFile;
import com.kfyty.web.mvc.servlet.multipart.DefaultMultipartFile;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    /**
     * 当前请求参数
     */
    private static final String CURRENT_REQUEST_PARAM = "__CURRENT__REQUEST__PARAMETERS__" + UUID.randomUUID();

    /**
     * 当前请求文件
     */
    private static final String CURRENT_REQUEST_FILES = "__CURRENT__REQUEST__FILES__" + UUID.randomUUID();

    /**
     * 是否是文件上传请求
     *
     * @param request {@link HttpServletRequest}
     * @return true if multipart/form-data
     */
    public static boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getHeader("content-type");
        return contentType != null && contentType.startsWith("multipart/form-data");
    }

    /**
     * 获取请求的数据
     * 如果是上传文件，则只获取 ParameterMap 的数据
     */
    public static String getRequestBody(HttpServletRequest request) throws IOException {
        if (isMultipartRequest(request)) {
            return getMultipartRequestBody(request);
        }
        String currentRequestParam = (String) request.getAttribute(CURRENT_REQUEST_PARAM);
        if (CommonUtil.notEmpty(currentRequestParam)) {
            return currentRequestParam;
        }
        int contentLength = request.getContentLength();
        if (contentLength < 0) {
            return CommonUtil.EMPTY_STRING;
        }
        byte[] buffer = new byte[contentLength];
        ServletInputStream inputStream = request.getInputStream();
        for (int n = 0, i = 0; n != -1 && i < contentLength; i += n) {
            n = inputStream.read(buffer, i, contentLength - i);
        }
        String body = new String(buffer, request.getCharacterEncoding());
        request.setAttribute(CURRENT_REQUEST_PARAM, body);
        return body;
    }

    /**
     * 从文件上传请求解析 ParameterMap 数据
     *
     * @param request {@link HttpServletRequest}
     * @return ParameterMap
     */
    public static String getMultipartRequestBody(HttpServletRequest request) throws IOException {
        String currentRequestParam = (String) request.getAttribute(CURRENT_REQUEST_PARAM);
        if (CommonUtil.notEmpty(currentRequestParam)) {
            return currentRequestParam;
        }
        Set<Pair<String, String>> params = new HashSet<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            for (String value : entry.getValue()) {
                params.add(new Pair<>(entry.getKey(), value));
            }
        }
        List<MultipartFile> files = DefaultMultipartFile.from(request);
        for (Iterator<MultipartFile> i = files.iterator(); i.hasNext(); ) {
            MultipartFile file = i.next();
            if (file.isFile()) {
                continue;
            }
            params.add(new Pair<>(file.getName(), new String(file.getBytes())));
            i.remove();
        }
        String body = params.stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
        request.setAttribute(CURRENT_REQUEST_PARAM, body);
        request.setAttribute(CURRENT_REQUEST_FILES, files);
        return body;
    }

    /**
     * 获取上传的文件
     *
     * @param request {@link HttpServletRequest}
     * @return 文件
     */
    @SuppressWarnings("unchecked")
    public static List<MultipartFile> getMultipart(HttpServletRequest request) throws IOException {
        List<MultipartFile> currentRequestFile = (List<MultipartFile>) request.getAttribute(CURRENT_REQUEST_FILES);
        if (CommonUtil.notEmpty(currentRequestFile)) {
            return currentRequestFile;
        }
        getMultipartRequestBody(request);
        return (List<MultipartFile>) request.getAttribute(CURRENT_REQUEST_FILES);
    }

    /**
     * 根据参数名获取单一的参数值
     * 如果获取不到则尝试从备份数据中获取
     */
    public static String getParameter(HttpServletRequest request, String paramName) throws IOException {
        String parameter = request.getParameter(paramName);
        return CommonUtil.notEmpty(parameter) ? URLDecoder.decode(parameter, request.getCharacterEncoding()) : tryGetParameter(request, paramName);
    }

    /**
     * 根据参数名获取重复的参数值
     * 如果获取不到则尝试从备份数据中获取
     */
    public static List<String> getParameters(HttpServletRequest request, String paramName) throws IOException {
        String[] params = request.getParameterMap().get(paramName);
        if (CommonUtil.notEmpty(params)) {
            return Arrays.asList(params);
        }
        return tryGetParameters(request, paramName);
    }

    /**
     * 获取 ParametersMap，如果获取不到则尝试从备份数据中获取
     */
    public static Map<String, String> getRequestParametersMap(HttpServletRequest request) throws IOException {
        return getRequestParametersMap(request, CommonUtil.EMPTY_STRING);
    }

    /**
     * 根据参数名前缀获取 ParametersMap，如果获取不到则尝试从备份数据中获取
     */
    public static Map<String, String> getRequestParametersMap(HttpServletRequest request, String prefix) throws IOException {
        Map<String, String> map = new HashMap<>();
        Enumeration<?> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = (String) parameterNames.nextElement();
            if (paramName.startsWith(prefix)) {
                map.put(paramName, URLDecoder.decode(request.getParameter(paramName), request.getCharacterEncoding()));
            }
        }
        return CommonUtil.notEmpty(map) ? map : tryGetRequestParametersMap(request, prefix);
    }

    public static String tryGetParameter(HttpServletRequest request, String paramName) throws IOException {
        return tryGetRequestParametersMap(request, paramName).get(paramName);
    }

    @SuppressWarnings("unchecked")
    public static List<String> tryGetParameters(HttpServletRequest request, String paramName) throws IOException {
        return (List<String>) CommonUtil.toList(tryGetRequestParametersMap(request, paramName).values());
    }

    public static Map<String, String> tryGetRequestParametersMap(HttpServletRequest request, String prefix) throws IOException {
        Map<String, String> map = new HashMap<>();
        String[] split = getRequestBody(request).split("&");
        if (CommonUtil.empty(split)) {
            return map;
        }
        for (String s : split) {
            String[] paramMap = s.split("=");
            if (CommonUtil.empty(paramMap) || paramMap.length < 2) {
                continue;
            }
            if (paramMap[0].startsWith(prefix)) {
                map.put(paramMap[0], URLDecoder.decode(paramMap[1], request.getCharacterEncoding()));
            }
        }
        return map;
    }
}
