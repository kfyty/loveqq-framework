package com.kfyty.generate.pojo.configuration;

import com.kfyty.generate.pojo.annotation.GenerateTemplate;
import com.kfyty.generate.pojo.template.GeneratePojoTemplate;
import com.kfyty.generate.pojo.annotation.DataBase;
import com.kfyty.generate.pojo.annotation.DataBaseMapping;
import com.kfyty.generate.pojo.annotation.FilePath;
import com.kfyty.generate.pojo.annotation.Package;
import com.kfyty.generate.pojo.annotation.SameFile;
import com.kfyty.generate.pojo.annotation.Table;
import com.kfyty.generate.pojo.database.DataBaseMapper;
import com.kfyty.util.CommonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 功能描述: 生成配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:12
 * @since JDK 1.8
 */
@Slf4j
@Getter
public class GeneratePojoConfigurable {

    private DataSource dataSource;

    private GeneratePojoTemplate generateTemplate;

    private Class<? extends DataBaseMapper> dataBaseMapper;

    private String dataBaseName;

    private Set<String> tables;

    private String queryTableSql;

    private String packageName;

    private String fileSuffix;

    private String fileTypeSuffix;

    private String filePath;

    private Boolean sameFile;

    public GeneratePojoConfigurable(GeneratePojoConfiguration configuration) throws Exception {
        this.initGeneratePojoConfigurable(configuration);
    }

    public void refreshGenerateConfiguration(GeneratePojoConfiguration configuration) throws Exception {
        this.initGeneratePojoConfigurable(configuration);
    }

    public void refreshGenerateTemplate(GeneratePojoTemplate generateTemplate) throws Exception {
        this.generateTemplate = Optional.ofNullable(generateTemplate).orElseThrow(() -> new NullPointerException("generate template is null !"));
        this.fileSuffix = Optional.ofNullable(generateTemplate.fileSuffix()).orElse("");
        this.fileTypeSuffix = Optional.ofNullable(generateTemplate.fileTypeSuffix()).orElse("");
    }

    private void initGeneratePojoConfigurable(GeneratePojoConfiguration configuration) throws Exception {
        this.initGeneratePojoConfigurableFromAnnotation(configuration);
        this.initGeneratePojoConfigurableFromReturnValue(configuration);
    }

    private void initGeneratePojoConfigurableFromAnnotation(GeneratePojoConfiguration configuration) throws Exception {
        Class<? extends GeneratePojoConfiguration> configurationClass = configuration.getClass();
        this.dataBaseMapper = Optional.ofNullable(configurationClass.getAnnotation(DataBaseMapping.class)).map(DataBaseMapping::value).orElse(null);
        this.dataBaseName = Optional.ofNullable(configurationClass.getAnnotation(DataBase.class)).map(DataBase::value).orElse(null);
        this.tables = Optional.ofNullable(configurationClass.getAnnotation(Table.class)).filter(e -> !CommonUtil.empty(e.value())).map(e -> new HashSet<>(Arrays.asList(e.value()))).orElse(null);
        this.queryTableSql = Optional.ofNullable(configurationClass.getAnnotation(Table.class)).filter(e -> !CommonUtil.empty(e.queryTableSql())).map(Table::queryTableSql).orElse(null);
        this.packageName = Optional.ofNullable(configurationClass.getAnnotation(Package.class)).map(Package::value).orElse(null);
        this.filePath = Optional.ofNullable(configurationClass.getAnnotation(FilePath.class)).map(FilePath::value).orElse(null);
        this.sameFile = Optional.ofNullable(configurationClass.getAnnotation(SameFile.class)).map(e -> true).orElse(null);
        this.generateTemplate = Optional.ofNullable(configurationClass.getAnnotation(GenerateTemplate.class)).map(e -> {
            try {
                return e.value().newInstance();
            } catch (Exception e1) {
                log.error(": instance generate template error:{}", e1);
                return null;
            }
        }).orElse(null);
    }

    private void initGeneratePojoConfigurableFromReturnValue(GeneratePojoConfiguration configuration) {
        this.dataSource = Optional.ofNullable(configuration.getDataSource()).orElseThrow(() -> new NullPointerException("data source is null !"));
        if(this.dataBaseMapper == null) {
            this.dataBaseMapper = Optional.ofNullable(configuration.dataBaseMapping()).orElseThrow(() -> new NullPointerException("data base mapper is null !"));
        }
        if(this.generateTemplate == null) {
            this.generateTemplate = Optional.ofNullable(configuration.getGenerateTemplate()).orElseThrow(() -> new NullPointerException("generate template is null !"));
        }
        this.fileSuffix = Optional.ofNullable(configuration.getGenerateTemplate().fileSuffix()).orElse("");
        this.fileTypeSuffix = Optional.ofNullable(configuration.getGenerateTemplate().fileTypeSuffix()).orElse("");
        if(CommonUtil.empty(this.dataBaseName)) {
            this.dataBaseName = Optional.ofNullable(configuration.dataBaseName()).orElseThrow(() -> new NullPointerException("data base name is null !"));
        }
        if(CommonUtil.empty(this.tables)) {
            this.tables = Optional.ofNullable(configuration.table()).map(e -> new HashSet<>(Arrays.asList(e))).orElse(null);
        }
        if(CommonUtil.empty(this.packageName)) {
            this.packageName = Optional.ofNullable(configuration.packageName()).orElse("");
        }
        if(CommonUtil.empty(this.filePath)) {
            this.filePath = Optional.ofNullable(configuration.filePath()).orElse("");
        }
        if(this.sameFile == null) {
            this.sameFile = Optional.ofNullable(configuration.sameFile()).orElse(false);
        }
    }
}
