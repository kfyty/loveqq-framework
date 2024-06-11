package com.kfyty.loveqq.framework.boot.pagehelper.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import lombok.Data;

/**
 * 描述: PageHelperProperties
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("pagehelper")
public class PageHelperProperties {
    private Boolean offsetAsPageNum;

    private Boolean rowBoundsWithCount;

    private Boolean pageSizeZero;

    private Boolean reasonable;

    private Boolean supportMethodsArguments;

    private String dialect;

    private String helperDialect;

    private Boolean autoRuntimeDialect;

    private Boolean autoDialect;

    private Boolean closeConn;

    private String params;

    private Boolean defaultCount;

    private String dialectAlias;

    private String autoDialectClass;

    private Boolean useSqlserver2012;

    private String countColumn;

    private String replaceSql;

    private String sqlCacheClass;

    private String boundSqlInterceptors;

    private Boolean keepOrderBy;

    private Boolean keepSubSelectOrderBy;

    private String sqlParser;
}
