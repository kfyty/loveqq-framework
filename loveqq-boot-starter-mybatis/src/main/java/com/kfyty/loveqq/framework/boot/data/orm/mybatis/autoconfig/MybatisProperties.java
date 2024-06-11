package com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import lombok.Data;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.scripting.LanguageDriver;

import java.util.Properties;

/**
 * 描述: mybatis 配置属性
 *
 * @author kfyty725
 * @date 2024/6/03 18:55
 * @email kfyty725@hotmail.com
 */
@Data
@Component
@ConfigurationProperties("mybatis")
public class MybatisProperties {
    /**
     * mybatis 配置路径配置
     */
    private String configLocation;

    /**
     * 类型别名注册基础包名
     */
    private String typeAliasesPackage;

    /**
     * {@link org.apache.ibatis.type.TypeHandler} 注册基础包名
     */
    private String typeHandlersPackage;

    /**
     * mapper 路径配置
     */
    private String[] mapperLocations;

    /**
     * vfs
     */
    private Class<? extends VFS> vfs;

    /**
     * 语言驱动
     */
    private Class<? extends LanguageDriver> defaultScriptingLanguageDriver;

    /**
     * 配置属性
     */
    private Properties configurationProperties;
}
