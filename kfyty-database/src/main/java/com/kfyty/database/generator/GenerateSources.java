package com.kfyty.database.generator;

import com.kfyty.database.generator.config.GeneratorConfiguration;
import com.kfyty.database.generator.config.GeneratorConfigurationSupport;
import com.kfyty.database.jdbc.session.SqlSessionProxyFactory;
import com.kfyty.database.generator.mapper.AbstractDatabaseMapper;
import com.kfyty.database.generator.info.AbstractTableStructInfo;
import com.kfyty.database.generator.template.GeneratorTemplate;
import com.kfyty.database.jdbc.annotation.Query;
import com.kfyty.support.io.SimpleBufferedWriter;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述: 生成资源
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 10:28
 * @since JDK 1.8
 */
@Slf4j
public class GenerateSources {
    @Getter
    protected GeneratorConfiguration configuration;

    protected List<? extends AbstractTableStructInfo> tableInfos;

    public GenerateSources() {
        this.configuration = new GeneratorConfiguration();
    }

    public GenerateSources(GeneratorConfigurationSupport configurationSupport) {
        this();
        this.refreshConfiguration(configurationSupport);
    }

    protected String initFilePath() {
        GeneratorTemplate template = configuration.currentTemplate();
        String basePackage = CommonUtil.empty(configuration.getBasePackage()) ? "" : configuration.getBasePackage() + ".";
        String classSuffix = CommonUtil.empty(template.classSuffix()) ? "" : template.classSuffix().toLowerCase();
        String packageName = basePackage + (!classSuffix.endsWith("impl") ? classSuffix : classSuffix.replace("impl", ".impl"));
        if(CommonUtil.notEmpty(template.packageName())) {
            packageName = template.packageName();
        }
        String parentPath = new File(CommonUtil.notEmpty(template.filePath()) ? template.filePath() : configuration.getFilePath()).getAbsolutePath();
        return parentPath + File.separator + packageName.replace(".", File.separator);
    }

    protected String initDirectory(AbstractTableStructInfo info) {
        String savePath = this.initFilePath();
        Optional.of(new File(savePath)).filter(e -> !e.exists()).map(File::mkdirs);
        String classSuffix = Optional.ofNullable(configuration.currentTemplate().classSuffix()).orElse("");
        String fileTypeSuffix = Optional.ofNullable(configuration.currentTemplate().fileTypeSuffix()).orElse(".java");
        return savePath + File.separator + CommonUtil.underline2CamelCase(info.getTableName(), true) + classSuffix + fileTypeSuffix;
    }

    protected File initFile(AbstractTableStructInfo info) throws IOException {
        File file = new File(this.initDirectory(info));
        if(file.exists() && !file.delete()) {
            throw new IllegalStateException("delete file failed: " + file.getAbsolutePath());
        }
        if(!file.createNewFile()) {
            throw new IllegalStateException("create file failed: " + file.getAbsolutePath());
        }
        return file;
    }

    protected void initTableInfos() {
        AbstractDatabaseMapper databaseMapper = SqlSessionProxyFactory.createProxy(configuration.getDataSource(), configuration.getDatabaseMapper());

        Set<String> tables = Optional.ofNullable(configuration.getTables()).orElse(new HashSet<>());

        if(CommonUtil.notEmpty(configuration.getQueryTableSql())) {
            Query annotation = AnnotationUtil.findAnnotation(ReflectUtil.getMethod(configuration.getDatabaseMapper(), "findTableList"), Query.class);
            ReflectUtil.setAnnotationValue(annotation, "value", configuration.getQueryTableSql());
            tables.addAll(databaseMapper.findTableList());
        }

        List<? extends AbstractTableStructInfo> tableInfos = databaseMapper.findTableInfos(configuration.getDatabaseName());
        List<? extends AbstractTableStructInfo> filteredTableInfo = Optional.of(tables).filter(e -> !e.isEmpty()).map(e -> tableInfos.stream().filter(info -> e.contains(info.getTableName())).collect(Collectors.toList())).orElse(Collections.emptyList());
        this.tableInfos = (CommonUtil.empty(filteredTableInfo) ? tableInfos : filteredTableInfo).stream().filter(e -> configuration.getTablePattern().matcher(e.getTableName()).matches()).collect(Collectors.toList());
        for (AbstractTableStructInfo info : this.tableInfos) {
            info.setFieldInfos(databaseMapper.findFieldInfos(info.getDatabaseName(), info.getTableName()));
        }
        if(log.isDebugEnabled()) {
            log.debug("initialize data base info success !");
        }
    }

    public GenerateSources refreshConfiguration(GeneratorConfigurationSupport configurationSupport) {
        this.configuration.refreshConfiguration(configurationSupport);
        this.tableInfos = null;
        return this;
    }

    public GenerateSources refreshConfiguration(GeneratorConfiguration configuration) {
        this.configuration = configuration;
        this.tableInfos = null;
        return this;
    }

    public GenerateSources refreshTemplate(GeneratorTemplate template) {
        this.configuration.refreshTemplate(template);
        return this;
    }

    public GenerateSources refreshTemplate(Collection<? extends GeneratorTemplate> templates) {
        this.configuration.refreshTemplate(templates);
        return this;
    }

    public void doGenerate() throws Exception {
        if(this.tableInfos == null) {
            this.initTableInfos();
        }
        File file = null;
        SimpleBufferedWriter out = null;
        while(configuration.hasTemplate()) {
            GeneratorTemplate template = configuration.nextTemplate();
            for (AbstractTableStructInfo tableInfo : this.tableInfos) {
                if(file == null || out == null || !template.sameFile()) {
                    file = this.initFile(tableInfo);
                    out = new SimpleBufferedWriter(new FileWriter(file, template.sameFile()));
                }
                template.doGenerate(tableInfo, configuration.getBasePackage(), out);
                out.flush();
                log.debug("generate resource: [{}] success --> [{}]", file.getName(), file.getAbsolutePath());
            }
            if(out != null && !template.sameFile()) {
                out.close();
                out = null;
            }
        }
    }
}
