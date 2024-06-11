package com.kfyty.loveqq.framework.boot.security.shiro.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.web.mvc.servlet.filter.FilterRegistrationBean;
import jakarta.servlet.Filter;
import lombok.Getter;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.Nameable;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.config.ShiroFilterConfiguration;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.OncePerRequestFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述: shiro 过滤器配置
 *
 * @author kfyty725
 * @date 2024/6/06 20:55
 * @email kfyty725@hotmail.com
 */
public class ShiroFilterFactoryBean implements FactoryBean<FilterRegistrationBean> {
    @Autowired
    protected ShiroProperties shiroProperties;

    @Autowired
    protected SecurityManager securityManager;

    @Autowired
    protected ShiroFilterConfiguration shiroFilterConfiguration;

    @Getter
    protected final Map<String, Filter> filters = new HashMap<>();

    @Override
    public Class<?> getBeanType() {
        return FilterRegistrationBean.class;
    }

    @Override
    public FilterRegistrationBean getObject() {
        return new FilterRegistrationBean().setFilter(this.createFilter());
    }

    protected AbstractShiroFilter createFilter() {
        FilterChainManager manager = createFilterChainManager();

        PathMatchingFilterChainResolver chainResolver = new PathMatchingFilterChainResolver();
        chainResolver.setFilterChainManager(manager);

        return new ShiroFilter((WebSecurityManager) this.securityManager, chainResolver, this.shiroFilterConfiguration);
    }

    protected FilterChainManager createFilterChainManager() {
        DefaultFilterChainManager manager = new DefaultFilterChainManager();

        for (Filter filter : manager.getFilters().values()) {
            this.applyGlobalPropertiesIfNecessary(filter);
        }

        if (!CollectionUtils.isEmpty(this.filters)) {
            for (Map.Entry<String, Filter> entry : this.filters.entrySet()) {
                String name = entry.getKey();
                Filter filter = entry.getValue();
                this.applyGlobalPropertiesIfNecessary(filter);
                if (filter instanceof Nameable) {
                    ((Nameable) filter).setName(name);
                }
                manager.addFilter(name, filter, false);
            }
        }

        manager.setGlobalFilters(this.shiroProperties.getGlobalFilters());

        Map<String, String> chains = this.shiroProperties.getFilterChainDefinitionMap();
        if (CommonUtil.notEmpty(chains)) {
            for (Map.Entry<String, String> entry : chains.entrySet()) {
                String url = entry.getKey();
                String chainDefinition = entry.getValue();
                manager.createChain(url, chainDefinition);
            }
        }

        manager.createDefaultChain("/**");

        return manager;
    }

    private void applyLoginUrlIfNecessary(Filter filter) {
        String loginUrl = this.shiroProperties.getLoginUrl();
        if (StringUtils.hasText(loginUrl) && (filter instanceof AccessControlFilter)) {
            AccessControlFilter acFilter = (AccessControlFilter) filter;
            String existingLoginUrl = acFilter.getLoginUrl();
            if (AccessControlFilter.DEFAULT_LOGIN_URL.equals(existingLoginUrl)) {
                acFilter.setLoginUrl(loginUrl);
            }
        }
    }

    private void applySuccessUrlIfNecessary(Filter filter) {
        String successUrl = this.shiroProperties.getSuccessUrl();
        if (StringUtils.hasText(successUrl) && (filter instanceof AuthenticationFilter)) {
            AuthenticationFilter authcFilter = (AuthenticationFilter) filter;
            String existingSuccessUrl = authcFilter.getSuccessUrl();
            if (AuthenticationFilter.DEFAULT_SUCCESS_URL.equals(existingSuccessUrl)) {
                authcFilter.setSuccessUrl(successUrl);
            }
        }
    }

    private void applyUnauthorizedUrlIfNecessary(Filter filter) {
        String unauthorizedUrl = this.shiroProperties.getUnauthorizedUrl();
        if (StringUtils.hasText(unauthorizedUrl) && (filter instanceof AuthorizationFilter)) {
            AuthorizationFilter authzFilter = (AuthorizationFilter) filter;
            String existingUnauthorizedUrl = authzFilter.getUnauthorizedUrl();
            if (existingUnauthorizedUrl == null) {
                authzFilter.setUnauthorizedUrl(unauthorizedUrl);
            }
        }
    }

    private void applyGlobalPropertiesIfNecessary(Filter filter) {
        this.applyLoginUrlIfNecessary(filter);
        this.applySuccessUrlIfNecessary(filter);
        this.applyUnauthorizedUrlIfNecessary(filter);
        if (filter instanceof OncePerRequestFilter) {
            ((OncePerRequestFilter) filter).setFilterOncePerRequest(this.shiroFilterConfiguration.isFilterOncePerRequest());
        }
    }

    private static class ShiroFilter extends AbstractShiroFilter {

        protected ShiroFilter(WebSecurityManager webSecurityManager, FilterChainResolver resolver, ShiroFilterConfiguration filterConfiguration) {
            super();
            setSecurityManager(webSecurityManager);
            setShiroFilterConfiguration(filterConfiguration);
            if (resolver != null) {
                setFilterChainResolver(resolver);
            }
        }
    }
}
