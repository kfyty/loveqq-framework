package com.kfyty.loveqq.framework.web.core;

import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedList;
import java.util.List;

/**
 * 描述: 组件注册
 *
 * @author kfyty725
 * @date 2021/5/28 14:49
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
public abstract class RegistrationBean<T extends RegistrationBean<T>> {
    private boolean asyncSupported = false;

    private String displayName = CommonUtil.EMPTY_STRING;

    private String description = CommonUtil.EMPTY_STRING;

    private String smallIcon = CommonUtil.EMPTY_STRING;

    private String largeIcon = CommonUtil.EMPTY_STRING;

    private List<Pair<String, String>> initParam = new LinkedList<>();

    private List<String> urlPatterns = new LinkedList<>();

    @SuppressWarnings("unchecked")
    public T addUrlPattern(String pattern) {
        this.urlPatterns.add(pattern);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addInitParam(String key, String value) {
        this.initParam.add(new Pair<>(key, value));
        return (T) this;
    }
}
