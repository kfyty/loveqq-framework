package com.kfyty.loveqq.framework.web.core.mapping;

import com.kfyty.loveqq.framework.web.core.request.RequestMethod;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 描述: 请求方法映射信息
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Data
@RequiredArgsConstructor
public class RequestMethodMapping {
    /**
     * 请求方法
     */
    private final RequestMethod requestMethod;

    /**
     * url 长度映射
     * key: url 长度
     * value: url 映射
     */
    private final Map<Integer, Map<String, MethodMapping>> urlLengthMapping;

    /**
     * 合并请求方法映射
     *
     * @param urlLengthMapping 请求方法映射
     */
    public void mergeRequestMethodMapping(Map<Integer, Map<String, MethodMapping>> urlLengthMapping) {
        this.urlLengthMapping.putAll(urlLengthMapping);
    }
}
