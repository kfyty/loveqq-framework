package com.kfyty.loveqq.framework.web.mvc.servlet;

import jakarta.servlet.Servlet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 描述: web 服务器
 *
 * @author kfyty725
 * @date 2021/5/28 14:49
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ServletRegistrationBean extends RegistrationBean<ServletRegistrationBean> {
    private Servlet servlet;

    private String name;

    private int loadOnStartup = -1;
}
