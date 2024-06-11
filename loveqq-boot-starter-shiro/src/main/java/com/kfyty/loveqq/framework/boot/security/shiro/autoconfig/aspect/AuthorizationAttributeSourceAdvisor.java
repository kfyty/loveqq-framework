package com.kfyty.loveqq.framework.boot.security.shiro.autoconfig.aspect;

import com.kfyty.loveqq.framework.aop.Pointcut;
import com.kfyty.loveqq.framework.aop.PointcutAdvisor;
import com.kfyty.loveqq.framework.aop.support.annotated.AnnotationMethodMatcher;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.authz.aop.AnnotationsAuthorizingMethodInterceptor;
import org.apache.shiro.authz.aop.AuthenticatedAnnotationMethodInterceptor;
import org.apache.shiro.authz.aop.GuestAnnotationMethodInterceptor;
import org.apache.shiro.authz.aop.PermissionAnnotationMethodInterceptor;
import org.apache.shiro.authz.aop.RoleAnnotationMethodInterceptor;
import org.apache.shiro.authz.aop.UserAnnotationMethodInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 描述: shiro 权限注解通知
 *
 * @author kfyty725
 * @date 2024/6/06 20:55
 * @email kfyty725@hotmail.com
 */
@Component
@ConditionalOnMissingBean
public class AuthorizationAttributeSourceAdvisor extends AnnotationsAuthorizingMethodInterceptor implements PointcutAdvisor {
    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] AUTH_ANNOTATION_CLASSES = new Class[]{
            RequiresPermissions.class,
            RequiresRoles.class,
            RequiresUser.class,
            RequiresAuthentication.class,
            RequiresGuest.class
    };

    public AuthorizationAttributeSourceAdvisor() {
        AnnotationResolver resolver = new DefaultAnnotationResolver();
        this.setMethodInterceptors(Arrays.asList(
                new RoleAnnotationMethodInterceptor(resolver),
                new PermissionAnnotationMethodInterceptor(resolver),
                new AuthenticatedAnnotationMethodInterceptor(resolver),
                new UserAnnotationMethodInterceptor(resolver),
                new GuestAnnotationMethodInterceptor(resolver)
        ));
    }

    @Override
    public Advice getAdvice() {
        return (MethodInterceptor) invocation -> {
            org.apache.shiro.aop.MethodInvocation methodInvocation = this.createMethodInvocation(invocation);
            return super.invoke(methodInvocation);
        };
    }

    @Override
    public Pointcut getPointcut() {
        return () -> new AnnotationMethodMatcher(null) {

            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                for (Class<? extends Annotation> authAnnotationClass : AUTH_ANNOTATION_CLASSES) {
                    if (AnnotationUtil.hasAnnotation(method, authAnnotationClass) || AnnotationUtil.hasAnnotation(targetClass, authAnnotationClass)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    protected org.apache.shiro.aop.MethodInvocation createMethodInvocation(MethodInvocation mi) {
        return new org.apache.shiro.aop.MethodInvocation() {

            public Method getMethod() {
                return mi.getMethod();
            }

            public Object[] getArguments() {
                return mi.getArguments();
            }

            public String toString() {
                return "Method invocation [" + mi.getMethod() + "]";
            }

            public Object proceed() throws Throwable {
                return mi.proceed();
            }

            public Object getThis() {
                return mi.getThis();
            }
        };
    }
}
