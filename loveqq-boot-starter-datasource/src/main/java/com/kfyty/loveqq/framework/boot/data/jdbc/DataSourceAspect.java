package com.kfyty.loveqq.framework.boot.data.jdbc;

import com.kfyty.loveqq.framework.boot.data.jdbc.annotation.DataSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 描述: 数据源注解切面
 *
 * @author kfyty725
 * @date 2022/5/30 14:55
 * @email kfyty725@hotmail.com
 */
@Aspect
public class DataSourceAspect {

    @Around("@annotation(dataSource)")
    public Object around(ProceedingJoinPoint pjp, DataSource dataSource) throws Throwable {
        Object prev = ThreadLocalRoutingDataSource.setCurrentDataSource(dataSource.value());
        try {
            return pjp.proceed();
        } finally {
            ThreadLocalRoutingDataSource.setCurrentDataSource(prev);
        }
    }
}
