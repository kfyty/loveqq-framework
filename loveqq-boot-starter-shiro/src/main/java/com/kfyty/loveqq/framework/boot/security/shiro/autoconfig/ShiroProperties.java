package com.kfyty.loveqq.framework.boot.security.shiro.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.InitializingBean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import lombok.Data;
import org.apache.shiro.web.filter.mgt.DefaultFilter;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 描述: shiro 配置属性
 *
 * @author kfyty725
 * @date 2024/6/06 20:55
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("shiro")
public class ShiroProperties implements InitializingBean {
    private String loginUrl;

    private String successUrl;

    private String unauthorizedUrl;

    private List<String> globalFilters;

    private Map<String, String> filterChainDefinitionMap;

    protected String rememberMeCookieName = CookieRememberMeManager.DEFAULT_REMEMBER_ME_COOKIE_NAME;

    protected int rememberMeCookieMaxAge = Cookie.ONE_YEAR;

    protected String rememberMeCookieDomain;

    protected String rememberMeCookiePath;

    protected boolean rememberMeCookieSecure = false;

    protected Cookie.SameSiteOptions rememberMeSameSite = Cookie.SameSiteOptions.LAX;

    protected boolean sessionManagerDeleteInvalidSessions = true;

    @Override
    public void afterPropertiesSet() {
        if (CommonUtil.empty(this.globalFilters)) {
            if (this.globalFilters == null) {
                this.globalFilters = new LinkedList<>();
            }
            this.globalFilters.add(DefaultFilter.invalidRequest.name());
        }
    }

    public Cookie rememberMeCookieTemplate() {
        return buildCookie(
                rememberMeCookieName,
                rememberMeCookieMaxAge,
                rememberMeCookiePath,
                rememberMeCookieDomain,
                rememberMeCookieSecure,
                rememberMeSameSite
        );
    }

    public Cookie buildCookie(String name, int maxAge, String path, String domain, boolean secure, Cookie.SameSiteOptions sameSiteOption) {
        Cookie cookie = new SimpleCookie(name);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        cookie.setDomain(domain);
        cookie.setSecure(secure);
        cookie.setSameSite(sameSiteOption);
        return cookie;
    }
}
