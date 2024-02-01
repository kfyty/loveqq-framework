package com.kfyty.mvc.request.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 描述: 模型视图容器
 *
 * @author kfyty725
 * @date 2021/6/10 11:48
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
public class ModelViewContainer {
    private String prefix;
    private String suffix;
    private Model model;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public ModelViewContainer() {
        this(RequestContextHolder.getCurrentRequest(), ResponseContextHolder.getCurrentResponse());
    }

    public ModelViewContainer(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }
}
