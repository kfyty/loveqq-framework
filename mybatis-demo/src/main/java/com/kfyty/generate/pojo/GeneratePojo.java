package com.kfyty.generate.pojo;

import com.kfyty.generate.pojo.configuration.GeneratePojoConfigurable;
import com.kfyty.generate.pojo.configuration.GeneratePojoConfiguration;
import com.kfyty.generate.pojo.database.DataBaseMapper;
import com.kfyty.generate.pojo.info.AbstractDataBaseInfo;
import com.kfyty.jdbc.SqlSession;
import com.kfyty.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
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

    private GeneratePojoConfigurable configurable;

    public GeneratePojo(GeneratePojoConfiguration pojoConfiguration) throws Exception {
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

    public void generate() throws IOException {
        SqlSession sqlSession = new SqlSession(configurable.getDataSource());
        DataBaseMapper dataBaseMapper = sqlSession.getProxyObject(configurable.getDataBaseMapper());
        List<? extends AbstractDataBaseInfo> dataBaseInfo = dataBaseMapper.findDataBaseInfo(configurable.getDataBaseName());
        List<? extends AbstractDataBaseInfo> dataBaseInfos = Optional.ofNullable(configurable.getTables()).filter(e -> !e.isEmpty()).map(e -> dataBaseInfo.stream().filter(info -> e.contains(info.getTableName())).collect(Collectors.toList())).orElse(null);
        dataBaseInfos = CommonUtil.empty(dataBaseInfos) ? dataBaseInfo : dataBaseInfos;
        for (AbstractDataBaseInfo info : dataBaseInfos) {
            info.setTableInfos(dataBaseMapper.findTableInfo(info.getDataBaseName(), info.getTableName()));
            this.write(info);
        }
    }
}
