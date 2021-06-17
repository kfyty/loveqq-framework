package com.kfyty.database.generate.configuration;

import com.kfyty.database.generate.configuration.annotation.BasePackage;
import com.kfyty.database.generate.configuration.annotation.DataBase;
import com.kfyty.database.generate.configuration.annotation.DataBaseMapper;
import com.kfyty.database.generate.configuration.annotation.FilePath;
import com.kfyty.database.generate.configuration.annotation.GenerateTemplate;
import com.kfyty.database.generate.configuration.annotation.Table;
import com.kfyty.database.generate.database.AbstractDataBaseMapper;
import com.kfyty.database.generate.template.AbstractGenerateTemplate;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
public class GenerateConfigurable {

    protected DataSource dataSource;

    protected Integer currentGenerateTemplateCursor;

    protected List<AbstractGenerateTemplate> generateTemplateList;

    protected Class<? extends AbstractDataBaseMapper> dataBaseMapper;

    protected String dataBaseName;

    protected Set<String> tables;

    protected Pattern tablePattern;

    protected String queryTableSql;

    protected String basePackage;

    protected String filePath;

    public GenerateConfigurable() {
        this.currentGenerateTemplateCursor = -1;
        this.generateTemplateList = new ArrayList<>();
    }

    public GenerateConfigurable(GenerateConfiguration configuration) {
        this();
        this.initGenerateConfigurable(configuration);
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

    public void refreshGenerateConfiguration(GenerateConfiguration configuration) {
        this.initGenerateConfigurable(configuration);
    }

    public void refreshGenerateTemplate(AbstractGenerateTemplate generateTemplate) {
        Optional.ofNullable(generateTemplate).orElseThrow(() -> new NullPointerException("generate template is null !"));
        this.generateTemplateList.add(currentGenerateTemplateCursor + 1, generateTemplate);
    }

    public void refreshGenerateTemplate(Collection<? extends AbstractGenerateTemplate> generateTemplates) {
        Optional.ofNullable(generateTemplates).orElseThrow(() -> new NullPointerException("generate templates is null !"));
        generateTemplates.forEach(this::refreshGenerateTemplate);
    }

    private void initGenerateConfigurable(GenerateConfiguration configuration) {
        this.initGenerateConfigurableFromAnnotation(configuration);
        this.initGenerateConfigurableFromReturnValue(configuration);
    }

    private void initGenerateConfigurableFromAnnotation(GenerateConfiguration configuration) {
        Class<? extends GenerateConfiguration> configurationClass = configuration.getClass();
        this.dataBaseMapper = dataBaseMapper != null ? dataBaseMapper : Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, DataBaseMapper.class)).map(DataBaseMapper::value).orElse(null);
        this.dataBaseName = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, DataBase.class)).map(DataBase::value).orElse(null);
        this.tables = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, Table.class)).filter(e -> !CommonUtil.empty(e.value()[0])).map(e -> new HashSet<>(Arrays.asList(e.value()))).orElse(null);
        this.tablePattern = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, Table.class)).filter(e -> !CommonUtil.empty(e.pattern())).map(e -> Pattern.compile(e.pattern())).orElse(Pattern.compile("([\\s\\S]*)"));
        this.queryTableSql = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, Table.class)).filter(e -> !CommonUtil.empty(e.queryTableSql())).map(Table::queryTableSql).orElse(null);
        this.basePackage = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, BasePackage.class)).map(BasePackage::value).orElse(null);
        this.filePath = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, FilePath.class)).map(FilePath::value).orElse(null);
        List<AbstractGenerateTemplate> generateTemplateList = Optional.ofNullable(AnnotationUtil.findAnnotation(configurationClass, GenerateTemplate.class)).map(e -> Arrays.stream(e.value()).distinct().map(clazz -> (AbstractGenerateTemplate) ReflectUtil.newInstance(clazz)).collect(Collectors.toList())).orElse(null);
        if(!CommonUtil.empty(generateTemplateList)) {
            this.generateTemplateList.addAll(generateTemplateList);
        }
    }

    private void initGenerateConfigurableFromReturnValue(GenerateConfiguration configuration) {
        this.dataSource = Optional.ofNullable(configuration.getDataSource()).orElseThrow(() -> new NullPointerException("data source is null !"));
        if(this.dataBaseMapper == null) {
            if(configuration.dataBaseMapping() != null) {
                this.dataBaseMapper = configuration.dataBaseMapping();
            } else {
                throw new NullPointerException("data base mapper is null !");
            }
        }
        if(CommonUtil.empty(this.generateTemplateList)) {
            if(configuration.getGenerateTemplate() != null) {
                this.generateTemplateList.addAll(Arrays.stream(configuration.getGenerateTemplate()).distinct().collect(Collectors.toList()));
            } else {
                throw new NullPointerException("generate template is null !");
            }
        }
        if(CommonUtil.empty(this.dataBaseName)) {
            this.dataBaseName = Optional.ofNullable(configuration.dataBaseName()).orElseThrow(() -> new NullPointerException("data base name is null !"));
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
