package com.kfyty.generate.pojo.configuration;

import com.kfyty.generate.pojo.GenerateTemplate;
import com.kfyty.generate.pojo.annotation.DataBase;
import com.kfyty.generate.pojo.annotation.DataBaseMapping;
import com.kfyty.generate.pojo.annotation.FilePath;
import com.kfyty.generate.pojo.annotation.Package;
import com.kfyty.generate.pojo.annotation.SameFile;
import com.kfyty.generate.pojo.annotation.Table;
import com.kfyty.generate.pojo.database.DataBaseMapper;
import com.kfyty.util.CommonUtil;
import lombok.Getter;

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
@Getter
public class GeneratePojoConfigurable {

    private DataSource dataSource;

    private GenerateTemplate generateTemplate;

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

    private void initGeneratePojoConfigurable(GeneratePojoConfiguration configuration) throws Exception {
        this.initGeneratePojoConfigurableFromAnnotation(configuration);
        this.initGeneratePojoConfigurableFromReturnValue(configuration);
    }

    private void initGeneratePojoConfigurableFromAnnotation(GeneratePojoConfiguration configuration) throws Exception {
        Class<? extends GeneratePojoConfiguration> configurationClass = configuration.getClass();
        this.dataBaseMapper = Optional.ofNullable(configurationClass.getMethod("dataBaseMapping").getAnnotation(DataBaseMapping.class)).map(DataBaseMapping::value).orElse(null);
        this.dataBaseName = Optional.ofNullable(configurationClass.getMethod("dataBaseName").getAnnotation(DataBase.class)).map(DataBase::value).orElse(null);
        this.tables = Optional.ofNullable(configurationClass.getMethod("table").getAnnotation(Table.class)).filter(e -> !CommonUtil.empty(e.value())).map(e -> new HashSet<>(Arrays.asList(e.value()))).orElse(null);
        this.queryTableSql = Optional.ofNullable(configurationClass.getMethod("table").getAnnotation(Table.class)).filter(e -> !CommonUtil.empty(e.queryTableSql())).map(Table::queryTableSql).orElse(null);
        this.packageName = Optional.ofNullable(configurationClass.getMethod("packageName").getAnnotation(Package.class)).map(Package::value).orElse(null);
        this.filePath = Optional.ofNullable(configurationClass.getMethod("filePath").getAnnotation(FilePath.class)).map(FilePath::value).orElse(null);
        this.sameFile = Optional.ofNullable(configurationClass.getMethod("sameFile").getAnnotation(SameFile.class)).map(SameFile::value).orElse(null);
    }

    private void initGeneratePojoConfigurableFromReturnValue(GeneratePojoConfiguration configuration) {
        this.dataSource = Optional.ofNullable(configuration.getDataSource()).orElseThrow(() -> new NullPointerException("data source is null !"));
        this.generateTemplate = Optional.ofNullable(configuration.getGenerateTemplate()).orElseThrow(() -> new NullPointerException("generate template is null !"));
        this.fileSuffix = configuration.getGenerateTemplate().fileSuffix();
        this.fileTypeSuffix = configuration.getGenerateTemplate().fileTypeSuffix();
        if(this.dataBaseMapper == null) {
            this.dataBaseMapper = configuration.dataBaseMapping();
        }
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
