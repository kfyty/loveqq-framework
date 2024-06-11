package com.kfyty.loveqq.framework.data.jdbc.sql.dialect;

import com.kfyty.loveqq.framework.data.jdbc.sql.ProviderAdapter;

/**
 * 描述: 基于方言的 SQL 提供者
 * <p>
 * {@link ProviderAdapter} 在适配动态 SQL 转发时,
 * 如果提供者是 {@link DialectProvider} 的子类，那么将忽略注解的 {@link com.kfyty.loveqq.framework.data.jdbc.annotation.Query#provider()} 属性,
 * 而是由 {@link ProviderAdapter#getDialect()} 返回方言提供者
 *
 * @author kfyty725
 * @date 2021/9/29 22:19
 * @email kfyty725@hotmail.com
 */
public abstract class DialectProvider extends AbstractProvider {
}
