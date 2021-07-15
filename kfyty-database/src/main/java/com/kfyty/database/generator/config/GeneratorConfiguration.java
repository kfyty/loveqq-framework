package com.kfyty.database.generator.config;

import com.kfyty.database.generator.config.annotation.BasePackage;
import com.kfyty.database.generator.config.annotation.Database;
import com.kfyty.database.generator.config.annotation.DatabaseMapper;
import com.kfyty.database.generator.config.annotation.FilePath;
import com.kfyty.database.generator.config.annotation.Template;
import com.kfyty.database.generator.config.annotation.Table;
import com.kfyty.database.generator.mapper.AbstractDatabaseMapper;
import com.kfyty.database.generator.template.GeneratorTemplate;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
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
public class GeneratorConfiguration {

    protected DataSource dataSource;

    protected Integer currentTemplateCursor;

    protected List<GeneratorTemplate> templateList;

    protected Class<? extends AbstractDatabaseMapper> databaseMapper;

    protected String databaseName;

    protected Set<String> tables;

    protected Pattern tablePattern;

    protected String queryTableSql;

    protected String basePackage;

    protected String filePath;

    public GeneratorConfiguration() {
        this.currentTemplateCursor = -1;
        this.templateList = new ArrayList<>();
    }

    public GeneratorConfiguration(GeneratorConfigurationSupport configuration) {
        this();
        this.initGeneratorConfiguration(configuration);
    }

    public boolean hasTemplate() {
        return currentTemplateCursor < templateList.size() - 1;
    }

    public GeneratorTemplate currentTemplate() {
        return templateList.get(currentTemplateCursor);
    }

    public GeneratorTemplate nextTemplate() {
        currentTemplateCursor++;
        return currentTemplate();
    }

    public void refreshConfiguration(GeneratorConfigurationSupport configurationSupport) {
        this.initGeneratorConfiguration(configurationSupport);
    }

    public void refreshTemplate(GeneratorTemplate template) {
        this.templateList.add(currentTemplateCursor + 1, Objects.requireNonNull(template, "template is null !"));
    }

    public void refreshTemplate(Collection<? extends GeneratorTemplate> templates) {
        Objects.requireNonNull(templates, "templates is null !").forEach(this::refreshTemplate);
    }

    private void initGeneratorConfiguration(GeneratorConfigurationSupport configurationSupport) {
        this.initGeneratorConfigurationFromAnnotation(configurationSupport);
        this.initGeneratorConfigurationFromMethod(configurationSupport);
    }

    private void initGeneratorConfigurationFromAnnotation(GeneratorConfigurationSupport configuration) {
        Class<? extends GeneratorConfigurationSupport> configurationClass = configuration.getClass();
        this.databaseMapper = databaseMapper != null ? databaseMapper : Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, DatabaseMapper.class)).map(DatabaseMapper::value).orElse(null);
        this.databaseName = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, Database.class)).map(Database::value).orElse(null);
        this.tables = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, Table.class)).filter(e -> !CommonUtil.empty(e.value()[0])).map(e -> new HashSet<>(Arrays.asList(e.value()))).orElse(null);
        this.tablePattern = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, Table.class)).filter(e -> !CommonUtil.empty(e.pattern())).map(e -> Pattern.compile(e.pattern())).orElse(Pattern.compile("([\\s\\S]*)"));
        this.queryTableSql = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, Table.class)).filter(e -> !CommonUtil.empty(e.queryTableSql())).map(Table::queryTableSql).orElse(null);
        this.basePackage = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, BasePackage.class)).map(BasePackage::value).orElse(null);
        this.filePath = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, FilePath.class)).map(FilePath::value).orElse(null);
        List<GeneratorTemplate> templateList = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, Template.class)).map(e -> Arrays.stream(e.value()).distinct().map(clazz -> (GeneratorTemplate) ReflectUtil.newInstance(clazz)).collect(Collectors.toList())).orElse(Collections.emptyList());
        if(!CommonUtil.empty(templateList)) {
            this.templateList.addAll(templateList);
        }
    }

    private void initGeneratorConfigurationFromMethod(GeneratorConfigurationSupport configuration) {
        this.dataSource = Optional.ofNullable(configuration.getDataSource()).orElseThrow(() -> new NullPointerException("data source is null !"));
        if(this.databaseMapper == null) {
            if(configuration.dataBaseMapping() != null) {
                this.databaseMapper = configuration.dataBaseMapping();
            } else {
                throw new NullPointerException("database mapper is null !");
            }
        }
        if(CommonUtil.empty(this.templateList)) {
            if(configuration.getTemplates() != null) {
                this.templateList.addAll(Arrays.stream(configuration.getTemplates()).distinct().collect(Collectors.toList()));
            } else {
                throw new NullPointerException("templates is empty !");
            }
        }
        if(CommonUtil.empty(this.databaseName)) {
            this.databaseName = Optional.ofNullable(configuration.databaseName()).orElseThrow(() -> new NullPointerException("database name is null !"));
        }
        if(CommonUtil.empty(this.tables)) {
            this.tables = Optional.ofNullable(configuration.table()).map(e -> new HashSet<>(Arrays.asList(e))).orElse(null);
        }
        if(CommonUtil.empty(this.basePackage)) {
            this.basePackage = Optional.ofNullable(configuration.basePackage()).orElse("");
        }
        if(CommonUtil.empty(this.filePath)) {
            this.filePath = Optional.ofNullable(configuration.filePath()).orElse("");
        }
    }
}
