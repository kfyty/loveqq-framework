package com.kfyty.web.mvc.servlet.filter;

import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.CommonUtil;
import jakarta.servlet.Filter;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedList;
import java.util.List;

/**
 * 描述: web 服务器
 *
 * @author kfyty725
 * @date 2021/5/28 14:49
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
public class FilterRegistrationBean {
    private Filter filter;

    private boolean asyncSupported = false;

    private String filterName = CommonUtil.EMPTY_STRING;

    private String displayName = CommonUtil.EMPTY_STRING;

    private String description = CommonUtil.EMPTY_STRING;

    private String smallIcon = CommonUtil.EMPTY_STRING;

    private String largeIcon = CommonUtil.EMPTY_STRING;

    private List<Pair<String, String>> initParam = new LinkedList<>();

    private List<String> urlPatterns = new LinkedList<>() {{
        add("/*");
    }};


    public FilterRegistrationBean addUrlPattern(String pattern) {
        this.urlPatterns.add(pattern);
        return this;
    }

    public FilterRegistrationBean addInitParam(String key, String value) {
        this.initParam.add(new Pair<>(key, value));
        return this;
    }
}
