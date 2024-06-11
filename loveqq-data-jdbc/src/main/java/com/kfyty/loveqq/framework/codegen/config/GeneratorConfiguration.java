package com.kfyty.loveqq.framework.codegen.config;

import com.kfyty.loveqq.framework.codegen.config.annotation.BasePackage;
import com.kfyty.loveqq.framework.codegen.config.annotation.Database;
import com.kfyty.loveqq.framework.codegen.config.annotation.FilePath;
import com.kfyty.loveqq.framework.codegen.config.annotation.Table;
import com.kfyty.loveqq.framework.codegen.config.annotation.Template;
import com.kfyty.loveqq.framework.codegen.mapper.DatabaseMapper;
import com.kfyty.loveqq.framework.codegen.template.GeneratorTemplate;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
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
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotation;
import static com.kfyty.loveqq.framework.core.utils.CommonUtil.MATCH_ALL_PATTERN;
import static java.util.Optional.ofNullable;

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

    protected Class<? extends DatabaseMapper> databaseMapper;

    protected String databaseName;

    protected Set<String> tables;

    protected Pattern tablePattern;

    protected String queryTableSql;

    protected String tablePrefix;

    protected boolean removeTablePrefix;

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
        this.databaseMapper = this.databaseMapper != null ? this.databaseMapper : ofNullable(findAnnotation(configurationClass, com.kfyty.loveqq.framework.codegen.config.annotation.DatabaseMapper.class)).map(com.kfyty.loveqq.framework.codegen.config.annotation.DatabaseMapper::value).orElse(null);
        this.databaseName = ofNullable(findAnnotation(configurationClass, Database.class)).map(Database::value).orElse(null);
        this.tables = ofNullable(findAnnotation(configurationClass, Table.class)).filter(e -> CommonUtil.notEmpty(e.value()[0])).map(e -> new HashSet<>(Arrays.asList(e.value()))).orElse(null);
        this.tablePattern = ofNullable(findAnnotation(configurationClass, Table.class)).filter(e -> CommonUtil.notEmpty(e.pattern())).map(e -> Pattern.compile(e.pattern())).orElse(MATCH_ALL_PATTERN);
        this.queryTableSql = ofNullable(findAnnotation(configurationClass, Table.class)).map(Table::queryTableSql).filter(CommonUtil::notEmpty).orElse(null);
        this.tablePrefix = ofNullable(findAnnotation(configurationClass, Table.class)).map(Table::prefix).filter(CommonUtil::notEmpty).orElse("");
        this.removeTablePrefix = ofNullable(findAnnotation(configurationClass, Table.class)).map(Table::removePrefix).orElse(false);
        this.basePackage = ofNullable(findAnnotation(configurationClass, BasePackage.class)).map(BasePackage::value).orElse(null);
        this.filePath = ofNullable(findAnnotation(configurationClass, FilePath.class)).map(FilePath::value).orElse(null);
        List<GeneratorTemplate> templateList = ofNullable(findAnnotation(configurationClass, Template.class)).map(e -> Arrays.stream(e.value()).distinct().map(clazz -> (GeneratorTemplate) ReflectUtil.newInstance(clazz)).collect(Collectors.toList())).orElse(Collections.emptyList());
        if (!CommonUtil.empty(templateList)) {
            this.templateList.addAll(templateList);
        }
    }

    private void initGeneratorConfigurationFromMethod(GeneratorConfigurationSupport configuration) {
        if ((this.dataSource = configuration.getDataSource()) == null) {
            throw new NullPointerException("data source can't null !");
        }
        if (this.databaseMapper == null && (this.databaseMapper = configuration.databaseMapping()) == null) {
            throw new NullPointerException("database mapper can't null !");
        }
        if (CommonUtil.notEmpty(configuration.getTemplates())) {
            this.templateList.addAll(Arrays.stream(configuration.getTemplates()).distinct().collect(Collectors.toList()));
        }
        if (CommonUtil.empty(this.databaseName)) {
            this.databaseName = ofNullable(configuration.databaseName()).orElseThrow(() -> new NullPointerException("database name can't null !"));
        }
        if (CommonUtil.empty(this.tables)) {
            this.tables = ofNullable(configuration.table()).map(e -> new HashSet<>(Arrays.asList(e))).orElse(null);
        }
        if (CommonUtil.empty(this.tablePrefix)) {
            this.removeTablePrefix = configuration.isRemoveTablePrefix();
        }
        if (CommonUtil.empty(this.tablePrefix)) {
            this.tablePrefix = ofNullable(configuration.tablePrefix()).orElse("");
        }
        if (CommonUtil.empty(this.basePackage)) {
            this.basePackage = ofNullable(configuration.basePackage()).orElse("");
        }
        if (CommonUtil.empty(this.filePath)) {
            this.filePath = ofNullable(configuration.filePath()).orElse("");
        }
        if (CommonUtil.empty(this.templateList)) {
            log.warn("generate template is empty !");
        }
    }
}
