package com.kfyty.generate.configuration;

import com.kfyty.configuration.Configuration;
import com.kfyty.generate.annotation.DataBase;
import com.kfyty.generate.annotation.DataBaseMapper;
import com.kfyty.generate.annotation.FilePath;
import com.kfyty.generate.annotation.GenerateTemplate;
import com.kfyty.generate.annotation.Package;
import com.kfyty.generate.annotation.SameFile;
import com.kfyty.generate.annotation.Table;
import com.kfyty.generate.database.AbstractDataBaseMapper;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.util.CommonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述: 生成配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/19 14:12
 * @since JDK 1.8
 */
@Data
@Slf4j
public class GenerateConfigurable extends Configuration {

    private DataSource dataSource;

    private Integer currentGenerateTemplateCursor;

    private List<AbstractGenerateTemplate> generateTemplateList;

    private Class<? extends AbstractDataBaseMapper> dataBaseMapper;

    private String dataBaseName;

    private Set<String> tables;

    private String queryTableSql;

    private String packageName;

    private String filePath;

    private Boolean sameFile;

    public GenerateConfigurable() throws Exception {
        this.currentGenerateTemplateCursor = -1;
        this.generateTemplateList = new ArrayList<>();
    }

    public GenerateConfigurable(GenerateConfiguration configuration) throws Exception {
        this();
        this.initGeneratePojoConfigurable(configuration);
    }

    public boolean hasGenerateTemplate() {
        return currentGenerateTemplateCursor < generateTemplateList.size() - 1;
    }

    public AbstractGenerateTemplate getCurrentGenerateTemplate() {
        return generateTemplateList.get(currentGenerateTemplateCursor);
    }

    public AbstractGenerateTemplate getNextGenerateTemplate() {
        currentGenerateTemplateCursor++;
        return getCurrentGenerateTemplate();
    }

    public void refreshGenerateConfiguration(GenerateConfiguration configuration) throws Exception {
        this.initGeneratePojoConfigurable(configuration);
    }

    public void refreshGenerateTemplate(AbstractGenerateTemplate generateTemplate) throws Exception {
        Optional.ofNullable(generateTemplate).orElseThrow(() -> new NullPointerException("generate template is null !"));
        this.generateTemplateList.add(currentGenerateTemplateCursor + 1, generateTemplate);
    }

    @Override
    public void autoConfigurationAfterCheck() {
        Optional.ofNullable(this.dataSource).orElseThrow(() -> new NullPointerException("data source is null !"));
        Optional.ofNullable(this.dataBaseMapper).orElseThrow(() -> new NullPointerException("data base mapper is null !"));
        this.generateTemplateList = Optional.ofNullable(generateTemplateList).filter(e -> !e.isEmpty()).map(e -> e.stream().distinct().collect(Collectors.toList())).orElseThrow(() -> new NullPointerException("generate template is null !"));
    }

    private void initGeneratePojoConfigurable(GenerateConfiguration configuration) throws Exception {
        this.initGeneratePojoConfigurableFromAnnotation(configuration);
        this.initGeneratePojoConfigurableFromReturnValue(configuration);
    }

    private void initGeneratePojoConfigurableFromAnnotation(GenerateConfiguration configuration) throws Exception {
        Class<? extends GenerateConfiguration> configurationClass = configuration.getClass();
        this.dataBaseMapper = dataBaseMapper != null ? dataBaseMapper : Optional.ofNullable(configurationClass.getAnnotation(DataBaseMapper.class)).map(DataBaseMapper::value).orElse(null);
        this.dataBaseName = Optional.ofNullable(configurationClass.getAnnotation(DataBase.class)).map(DataBase::value).orElse(null);
        this.tables = Optional.ofNullable(configurationClass.getAnnotation(Table.class)).filter(e -> !CommonUtil.empty(e.value())).map(e -> new HashSet<>(Arrays.asList(e.value()))).orElse(null);
        this.queryTableSql = Optional.ofNullable(configurationClass.getAnnotation(Table.class)).filter(e -> !CommonUtil.empty(e.queryTableSql())).map(Table::queryTableSql).orElse(null);
        this.packageName = Optional.ofNullable(configurationClass.getAnnotation(Package.class)).map(Package::value).orElse(null);
        this.filePath = Optional.ofNullable(configurationClass.getAnnotation(FilePath.class)).map(FilePath::value).orElse(null);
        this.sameFile = Optional.ofNullable(configurationClass.getAnnotation(SameFile.class)).map(e -> true).orElse(null);
        List<AbstractGenerateTemplate> generateTemplateList = Optional.ofNullable(configurationClass.getAnnotation(GenerateTemplate.class)).map(e -> Arrays.stream(e.value()).distinct().map(clazz -> {
            try {
                return (AbstractGenerateTemplate) clazz.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList())).orElse(null);
        if(!CommonUtil.empty(generateTemplateList)) {
            this.generateTemplateList.addAll(generateTemplateList);
        }
    }

    private void initGeneratePojoConfigurableFromReturnValue(GenerateConfiguration configuration) {
        this.dataSource = Optional.ofNullable(configuration.getDataSource()).orElseThrow(() -> new NullPointerException("data source is null !"));
        if(this.dataBaseMapper == null) {
            if(configuration.dataBaseMapping() != null) {
                this.dataBaseMapper = configuration.dataBaseMapping();
            } else if(!isAutoConfiguration()) {
                throw new NullPointerException("data base mapper is null !");
            }
        }
        if(CommonUtil.empty(this.generateTemplateList)) {
            if(!CommonUtil.empty(configuration.getGenerateTemplate())) {
                this.generateTemplateList.addAll(Arrays.stream(configuration.getGenerateTemplate()).distinct().collect(Collectors.toList()));
            } else if(!isAutoConfiguration()) {
                throw new NullPointerException("generate template is null !");
            }
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
