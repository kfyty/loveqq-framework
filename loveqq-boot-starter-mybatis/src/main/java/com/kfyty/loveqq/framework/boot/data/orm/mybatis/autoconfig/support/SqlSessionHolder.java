package com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * 描述: SqlSessionHolder
 *
 * @author kfyty725
 * @date 2024/6/03 18:55
 * @email kfyty725@hotmail.com
 */
@Getter
@RequiredArgsConstructor
public class SqlSessionHolder extends ResourceHolderSupport {
    private final SqlSession sqlSession;
    private final ExecutorType executorType;
}
