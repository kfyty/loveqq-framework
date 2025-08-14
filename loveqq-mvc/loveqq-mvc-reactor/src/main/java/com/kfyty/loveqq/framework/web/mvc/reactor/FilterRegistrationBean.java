package com.kfyty.loveqq.framework.web.mvc.reactor;

import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.core.RegistrationBean;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 描述: netty 过滤器注册
 *
 * @author kfyty725
 * @date 2021/5/28 14:49
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FilterRegistrationBean extends RegistrationBean<FilterRegistrationBean> {
    private Filter filter;

    private String filterName = CommonUtil.EMPTY_STRING;

    {
        addUrlPattern("/**");
    }
}
