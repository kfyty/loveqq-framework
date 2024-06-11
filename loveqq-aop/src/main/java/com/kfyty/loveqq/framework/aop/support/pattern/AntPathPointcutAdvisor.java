package com.kfyty.loveqq.framework.aop.support.pattern;

import com.kfyty.loveqq.framework.aop.support.DefaultPointcutAdvisor;
import org.aopalliance.aop.Advice;

/**
 * 描述: ant 路径匹配实现
 *
 * @author kfyty725
 * @date 2022/12/1 20:32
 * @email kfyty725@hotmail.com
 */
public class AntPathPointcutAdvisor extends DefaultPointcutAdvisor {

    public AntPathPointcutAdvisor(String pattern, Advice advice) {
        super(new AntPathPointcut(pattern), advice);
    }
}
