package com.kfyty.loveqq.framework.boot.security.shiro.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnWebApplication;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.event.EventBus;
import org.apache.shiro.event.support.DefaultEventBus;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.DefaultSubjectFactory;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.mgt.SubjectDAO;
import org.apache.shiro.mgt.SubjectFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionFactory;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.SimpleSessionFactory;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.web.config.ShiroFilterConfiguration;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import java.util.List;
import java.util.Objects;

/**
 * 描述: shiro 自动配置
 *
 * @author kfyty725
 * @date 2024/6/06 20:55
 * @email kfyty725@hotmail.com
 */
@Configuration
public class ShiroAutoConfiguration {
    @Autowired
    private ShiroProperties shiroProperties;

    @Autowired(required = false)
    private List<Realm> realms;

    @Autowired(required = false)
    protected CacheManager cacheManager;

    @Autowired(required = false)
    protected RolePermissionResolver rolePermissionResolver;

    @Autowired(required = false)
    protected PermissionResolver permissionResolver;

    @Bean
    public EventBus eventBus() {
        return new DefaultEventBus();
    }

    @Bean
    public SessionStorageEvaluator sessionStorageEvaluator() {
        return new DefaultSessionStorageEvaluator();
    }

    @Bean
    protected RememberMeManager defaultRememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(this.shiroProperties.rememberMeCookieTemplate());
        return cookieRememberMeManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public ShiroFilterConfiguration defaultFilterConfiguration() {
        return new ShiroFilterConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    public SubjectDAO defaultSubjectDAO(SessionStorageEvaluator sessionStorageEvaluator) {
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator);
        return subjectDAO;
    }

    @Bean
    @ConditionalOnMissingBean
    public SubjectFactory defaultSubjectFactory() {
        return new DefaultSubjectFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionDAO defaultSessionDAO() {
        return new MemorySessionDAO();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionFactory defaultSessionFactory() {
        return new SimpleSessionFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationStrategy defaultAuthenticationStrategy() {
        return new AtLeastOneSuccessfulStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionManager defaultSessionManager(SessionDAO sessionDAO, SessionFactory sessionFactory) {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.setSessionDAO(sessionDAO);
        sessionManager.setSessionFactory(sessionFactory);
        sessionManager.setDeleteInvalidSessions(this.shiroProperties.isSessionManagerDeleteInvalidSessions());
        return sessionManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public Authenticator defaultAuthenticator(AuthenticationStrategy authenticationStrategy) {
        ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
        authenticator.setAuthenticationStrategy(authenticationStrategy);
        return authenticator;
    }

    @Bean
    @ConditionalOnMissingBean
    public Authorizer defaultAuthorizer() {
        ModularRealmAuthorizer authorizer = new ModularRealmAuthorizer();
        if (this.permissionResolver != null) {
            authorizer.setPermissionResolver(this.permissionResolver);
        }
        if (this.rolePermissionResolver != null) {
            authorizer.setRolePermissionResolver(this.rolePermissionResolver);
        }
        return authorizer;
    }

    @Bean("isWebApplication")
    @ConditionalOnWebApplication
    public Boolean isWebApplication() {
        return true;
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityManager defaultSecurityManager(SubjectDAO subjectDAO,
                                           SubjectFactory subjectFactory,
                                           Authenticator authenticator,
                                           Authorizer authorizer,
                                           SessionManager sessionManager,
                                           RememberMeManager rememberMeManager,
                                           EventBus eventBus,
                                           @Autowired(value = "isWebApplication", required = false) Boolean isWebApplication) {
        SessionsSecurityManager securityManager = this.createSecurityManager(subjectDAO, subjectFactory, rememberMeManager, isWebApplication);
        securityManager.setRealms(this.realms);
        securityManager.setAuthenticator(authenticator);
        securityManager.setAuthorizer(authorizer);
        securityManager.setSessionManager(sessionManager);
        securityManager.setEventBus(eventBus);

        if (this.cacheManager != null) {
            securityManager.setCacheManager(this.cacheManager);
        }

        return securityManager;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    public ShiroFilterFactoryBean defaultShiroFilterFactoryBean() {
        return new ShiroFilterFactoryBean();
    }

    protected SessionsSecurityManager createSecurityManager(SubjectDAO subjectDAO, SubjectFactory subjectFactory, RememberMeManager rememberMeManager, Boolean isWebApplication) {
        DefaultSecurityManager securityManager = Objects.equals(isWebApplication, true) ? new DefaultWebSecurityManager() : new DefaultSecurityManager();
        securityManager.setSubjectDAO(subjectDAO);
        securityManager.setSubjectFactory(subjectFactory);
        securityManager.setRememberMeManager(rememberMeManager);
        return securityManager;
    }
}
