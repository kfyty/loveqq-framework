package com.kfyty.aop.support;

import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.MatchingContext;
import org.aspectj.weaver.tools.ShadowMatch;

/**
 * 描述: 简单的匹配器实现
 *
 * @author kfyty725
 * @date 2022/4/11 21:22
 * @email kfyty725@hotmail.com
 */
public class SimpleShadowMatch implements ShadowMatch {
    public static final ShadowMatch INSTANCE = new SimpleShadowMatch();

    @Override
    public boolean alwaysMatches() {
        return true;
    }

    @Override
    public boolean maybeMatches() {
        return true;
    }

    @Override
    public boolean neverMatches() {
        return false;
    }

    @Override
    public JoinPointMatch matchesJoinPoint(Object thisObject, Object targetObject, Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMatchingContext(MatchingContext aMatchContext) {
        throw new UnsupportedOperationException();
    }
}
