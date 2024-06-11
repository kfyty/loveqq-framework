package com.kfyty.loveqq.framework.aop.support.pattern;

import com.kfyty.aop.support.SimpleShadowMatch;
import com.kfyty.loveqq.framework.aop.MethodMatcher;
import com.kfyty.loveqq.framework.core.support.AntPathMatcher;
import com.kfyty.loveqq.framework.core.support.PatternMatcher;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;

/**
 * 描述: ant 路径匹配器
 *
 * @author kfyty725
 * @date 2022/12/1 20:27
 * @email kfyty725@hotmail.com
 */
public class AntPathMethodMatcher implements MethodMatcher {
    private final String pattern;
    private final PatternMatcher patternMatcher;

    public AntPathMethodMatcher(String pattern) {
        this.pattern = pattern;
        this.patternMatcher = new AntPathMatcher();
    }

    @Override
    public ShadowMatch getShadowMatch(Method method) {
        return SimpleShadowMatch.INSTANCE;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        String matchName = targetClass.getName() + "#" + method.getName();
        return this.patternMatcher.matches(this.pattern, matchName);
    }
}
