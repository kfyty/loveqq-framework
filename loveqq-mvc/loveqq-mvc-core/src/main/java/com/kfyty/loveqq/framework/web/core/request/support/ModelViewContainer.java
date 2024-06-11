package com.kfyty.loveqq.framework.web.core.request.support;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
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
public class ModelViewContainer<Request, Response> {
    private String prefix;
    private String suffix;
    private Model model;
    private Request request;
    private Response response;

    public ModelViewContainer(Request request, Response response) {
        this(CommonUtil.EMPTY_STRING, CommonUtil.EMPTY_STRING, request, response);
    }

    public ModelViewContainer(String prefix, String suffix, Request request, Response response) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.request = request;
        this.response = response;
    }
}
