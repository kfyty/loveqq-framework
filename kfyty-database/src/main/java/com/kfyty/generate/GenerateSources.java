package com.kfyty.generate;

import com.kfyty.generate.configuration.GenerateConfigurable;
import com.kfyty.generate.configuration.GenerateConfiguration;
import com.kfyty.generate.database.AbstractDataBaseMapper;
import com.kfyty.generate.info.AbstractTableStructInfo;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.jdbc.SqlSession;
import com.kfyty.jdbc.annotation.Query;
import com.kfyty.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
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

    protected SqlSession sqlSession;

    protected GenerateConfigurable configurable;

    protected List<? extends AbstractTableStructInfo> tableInfos;

    public GenerateSources() {
        this.sqlSession = new SqlSession();
        this.configurable = new GenerateConfigurable();
    }

    public GenerateSources(GenerateConfiguration generateConfiguration) {
        this();
        this.refreshGenerateConfiguration(generateConfiguration);
    }

    protected String initFilePath() {
        String basePackage = CommonUtil.empty(configurable.getBasePackage()) ? "" : configurable.getBasePackage() + ".";
        String classSuffix = configurable.getCurrentGenerateTemplate().classSuffix().toLowerCase();
        String packageName = basePackage + (!classSuffix.endsWith("impl") ? classSuffix : classSuffix.replace("impl", ".impl"));
        String parentPath = new File(configurable.getFilePath()).getAbsolutePath();
        return parentPath + File.separator + packageName.replace(".", File.separator);
    }

    protected String initDirectory(AbstractTableStructInfo info) {
        String savePath = this.initFilePath();
        Optional.of(new File(savePath)).filter(e -> !e.exists()).map(File::mkdirs);
        String classSuffix = Optional.ofNullable(configurable.getCurrentGenerateTemplate().classSuffix()).orElse("");
        String fileTypeSuffix = Optional.ofNullable(configurable.getCurrentGenerateTemplate().fileTypeSuffix()).orElse(".java");
        return savePath + File.separator + CommonUtil.convert2Hump(info.getTableName(), true) + classSuffix + fileTypeSuffix;
    }

    protected File initFile(AbstractTableStructInfo info) throws IOException {
        File file = new File(this.initDirectory(info));
        if(file.exists() && !file.delete()) {
            log.error(": delete file failed : {}", file.getAbsolutePath());
            return null;
        }
        if(!file.createNewFile()) {
            log.error(": create file failed : {}", file.getAbsolutePath());
            return null;
        }
        return file;
    }

    protected void initTableInfos() throws Exception {
        this.sqlSession.setDataSource(configurable.getDataSource());
        AbstractDataBaseMapper dataBaseMapper = sqlSession.getProxyObject(configurable.getDataBaseMapper());

        Set<String> tables = Optional.ofNullable(configurable.getTables()).orElse(new HashSet<>());

        if(!CommonUtil.empty(configurable.getQueryTableSql())) {
            Query annotation = configurable.getDataBaseMapper().getMethod("findTableList").getAnnotation(Query.class);
            CommonUtil.setAnnotationValue(annotation, "value", configurable.getQueryTableSql());
            tables.addAll(dataBaseMapper.findTableList());
        }

        List<? extends AbstractTableStructInfo> tableInfos = dataBaseMapper.findTableInfos(configurable.getDataBaseName());
        List<? extends AbstractTableStructInfo> filteredTableInfo = Optional.of(tables).filter(e -> !e.isEmpty()).map(e -> tableInfos.stream().filter(info -> e.contains(info.getTableName())).collect(Collectors.toList())).orElse(null);
        this.tableInfos = (CommonUtil.empty(filteredTableInfo) ? tableInfos : filteredTableInfo).stream().filter(e -> configurable.getTablePattern().matcher(e.getTableName()).matches()).collect(Collectors.toList());
        for (AbstractTableStructInfo info : this.tableInfos) {
            info.setFieldInfos(dataBaseMapper.findFieldInfos(info.getDataBaseName(), info.getTableName()));
        }
        if(log.isDebugEnabled()) {
            log.debug(": initialize data base info success !");
        }
    }

    public GenerateSources refreshGenerateConfiguration(GenerateConfiguration configuration) {
        this.configurable.refreshGenerateConfiguration(configuration);
        this.tableInfos = null;
        return this;
    }

    public GenerateSources refreshGenerateConfigurable(GenerateConfigurable configurable) {
        this.configurable = configurable;
        this.tableInfos = null;
        return this;
    }

    public GenerateSources refreshGenerateTemplate(AbstractGenerateTemplate generateTemplate) {
        this.configurable.refreshGenerateTemplate(generateTemplate);
        return this;
    }

    public GenerateSources refreshGenerateTemplate(Collection<? extends AbstractGenerateTemplate> generateTemplates) {
        this.configurable.refreshGenerateTemplate(generateTemplates);
        return this;
    }

    public void generate() throws Exception {
        if(this.tableInfos == null) {
            this.initTableInfos();
        }
        File file = null;
        GenerateSourcesBufferedWriter out = null;
        while(configurable.hasGenerateTemplate()) {
            AbstractGenerateTemplate nextGenerateTemplate = configurable.getNextGenerateTemplate();
            for (AbstractTableStructInfo tableInfo : this.tableInfos) {
                if(file == null || out == null || !nextGenerateTemplate.sameFile()) {
                    file = this.initFile(tableInfo);
                    out = new GenerateSourcesBufferedWriter(new FileWriter(file, nextGenerateTemplate.sameFile()));
                }
                nextGenerateTemplate.generate(tableInfo, configurable.getBasePackage(), out);
                out.flush();
                log.debug(": generate resource:[{}] success --> [{}]", file.getName(), file.getAbsolutePath());
            }
            if(out != null && !nextGenerateTemplate.sameFile()) {
                out.close();
                out = null;
            }
        }
    }
}
