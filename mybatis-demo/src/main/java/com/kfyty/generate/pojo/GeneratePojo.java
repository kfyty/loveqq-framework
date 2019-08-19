package com.kfyty.generate.pojo;

import com.kfyty.generate.pojo.configuration.GeneratePojoConfigurable;
import com.kfyty.generate.pojo.configuration.GeneratePojoConfiguration;
import com.kfyty.generate.pojo.database.DataBaseMapper;
import com.kfyty.generate.pojo.info.AbstractDataBaseInfo;
import com.kfyty.jdbc.SqlSession;
import com.kfyty.jdbc.annotation.SelectList;
import com.kfyty.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述: 生成 pojo
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/12 10:28
 * @since JDK 1.8
 */
@Slf4j
public class GeneratePojo {
    private File file;

    private SqlSession sqlSession;

    private GeneratePojoConfigurable configurable;

    public GeneratePojo(GeneratePojoConfiguration pojoConfiguration) throws Exception {
        this.sqlSession = new SqlSession();
        this.configurable = new GeneratePojoConfigurable(pojoConfiguration);
    }

    private String initDirectory(AbstractDataBaseInfo info) {
        String parentPath = new File(configurable.getFilePath()).getAbsolutePath();
        String savePath = parentPath.endsWith(File.separator) ?
                parentPath + configurable.getPackageName().replace(".", File.separator) :
                parentPath + File.separator + configurable.getPackageName().replace(".", File.separator);
        Optional.of(new File(savePath)).filter(e -> !e.exists()).map(File::mkdirs);
        return savePath + File.separator + CommonUtil.convert2Hump(info.getTableName(), true) +
                configurable.getFileSuffix() + configurable.getFileTypeSuffix();
    }

    private void initFile(AbstractDataBaseInfo info) throws IOException {
        this.file = new File(this.initDirectory(info));
        if(this.file.exists() && !this.file.delete()) {
            log.error(": delete file failed !");
            return ;
        }
        if(!this.file.createNewFile()) {
            log.error(": create file failed !");
        }
    }

    private void write(AbstractDataBaseInfo dataBaseInfo) throws IOException {
        if(this.file == null || !configurable.getSameFile()) {
            this.initFile(dataBaseInfo);
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(this.file, configurable.getSameFile()));
        configurable.getGenerateTemplate().generate(dataBaseInfo, configurable.getPackageName(), out);
        out.flush();
        log.debug(": generate resource:[{}] success --> [{}]", this.file.getName(), this.file.getAbsolutePath());
    }

    private List<? extends AbstractDataBaseInfo> handleDataBaseInfo(DataBaseMapper dataBaseMapper) throws Exception {
        Set<String> tables = Optional.ofNullable(configurable.getTables()).orElse(new HashSet<>());
        if(!CommonUtil.empty(configurable.getQueryTableSql())) {
            SelectList annotation = configurable.getDataBaseMapper().getMethod("findTableList").getAnnotation(SelectList.class);
            CommonUtil.setAnnotationValue(annotation, "value", configurable.getQueryTableSql());
            tables.addAll(dataBaseMapper.findTableList());
        }
        List<? extends AbstractDataBaseInfo> dataBaseInfo = dataBaseMapper.findDataBaseInfo(configurable.getDataBaseName());
        List<? extends AbstractDataBaseInfo> filteredDataBaseInfo = Optional.ofNullable(configurable.getTables()).filter(e -> !e.isEmpty()).map(e -> dataBaseInfo.stream().filter(info -> e.contains(info.getTableName())).collect(Collectors.toList())).orElse(null);
        return CommonUtil.empty(filteredDataBaseInfo) ? dataBaseInfo : filteredDataBaseInfo;
    }

    public GeneratePojo refreshGenerateConfiguration(GeneratePojoConfiguration configuration) throws Exception {
        this.configurable.refreshGenerateConfiguration(configuration);
        return this;
    }

    public GeneratePojo refreshGenerateTemplate(GenerateTemplate generateTemplate) throws Exception {
        this.configurable.refreshGenerateTemplate(generateTemplate);
        return this;
    }

    public void generate() throws Exception {
        this.sqlSession.setDataSource(configurable.getDataSource());
        DataBaseMapper dataBaseMapper = sqlSession.getProxyObject(configurable.getDataBaseMapper());
        List<? extends AbstractDataBaseInfo> dataBaseInfo = handleDataBaseInfo(dataBaseMapper);
        for (AbstractDataBaseInfo info : dataBaseInfo) {
            info.setTableInfos(dataBaseMapper.findTableInfo(info.getDataBaseName(), info.getTableName()));
            this.write(info);
        }
    }
}
