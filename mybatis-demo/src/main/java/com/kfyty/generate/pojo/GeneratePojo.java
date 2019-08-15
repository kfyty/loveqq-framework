package com.kfyty.generate.pojo;

import com.kfyty.generate.pojo.annotation.DataBase;
import com.kfyty.generate.pojo.annotation.FilePath;
import com.kfyty.generate.pojo.annotation.FileSuffix;
import com.kfyty.generate.pojo.annotation.MYSQL;
import com.kfyty.generate.pojo.annotation.ORACLE;
import com.kfyty.generate.pojo.annotation.Package;
import com.kfyty.generate.pojo.annotation.Table;
import com.kfyty.generate.pojo.configuration.GeneratePojoConfiguration;
import com.kfyty.generate.pojo.info.DataBaseInfo;
import com.kfyty.generate.pojo.info.TableInfo;
import com.kfyty.jdbc.SqlSession;
import com.kfyty.jdbc.annotation.Param;
import com.kfyty.jdbc.annotation.SelectList;
import com.kfyty.util.CommonUtil;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述: 生成 pojo
 *
 * @author zhangkun@wisdombud.com
 * @date 2019/8/12 10:28
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public class GeneratePojo {
    @Setter
    private List<GeneratePojoConfiguration> pojoConfigurations;

    @Setter
    private GenerateTemplate generateTemplate;

    private Class<? extends DataBaseMapper> dataBaseMapper;

    private String databaseName;

    private Set<String> tables;

    private String packageName;

    private String fileSuffix;

    private String filePath;

    public GeneratePojo(GeneratePojoConfiguration pojoConfiguration) {
        if(this.pojoConfigurations == null) {
            this.pojoConfigurations = new ArrayList<>();
        }
        this.pojoConfigurations.add(pojoConfiguration);
        this.generateTemplate = new GenerateTemplate() {};
    }

    public GeneratePojo(GeneratePojoConfiguration pojoConfiguration, GenerateTemplate generateTemplate) {
        this(pojoConfiguration);
        this.generateTemplate = generateTemplate;
    }

    public GeneratePojo(List<GeneratePojoConfiguration> pojoConfigurations) {
        this.pojoConfigurations = pojoConfigurations;
        this.generateTemplate = new GenerateTemplate() {};
    }

    public GeneratePojo(List<GeneratePojoConfiguration> pojoConfigurations, GenerateTemplate generateTemplate) {
        this(pojoConfigurations);
        this.generateTemplate = generateTemplate;
    }

    private void initConfiguration(Class<? extends GeneratePojoConfiguration> configuration) throws NoSuchMethodException {
        Method method = configuration.getMethod("dataBaseType");
        if(method.isAnnotationPresent(ORACLE.class)) {
            this.dataBaseMapper = OracleDataBaseMapper.class;
        } else if(method.isAnnotationPresent(MYSQL.class)) {
            this.dataBaseMapper = MySQLDataBaseMapper.class;
        } else {
            throw new IllegalArgumentException("database type is null !");
        }

        this.databaseName = Optional.ofNullable(configuration.getMethod("dataBaseName").getAnnotation(DataBase.class)).map(DataBase::value).orElseThrow(() ->new NullPointerException("database name is null !"));
        this.tables = Optional.ofNullable(configuration.getMethod("table").getAnnotation(Table.class)).filter(e -> !CommonUtil.empty(e.value())).map(e -> new HashSet<>(Arrays.asList(e.value()))).orElse(null);
        this.packageName = Optional.ofNullable(configuration.getMethod("packageName").getAnnotation(Package.class)).map(Package::value).orElse("");
        this.fileSuffix = Optional.ofNullable(configuration.getMethod("fileSuffix").getAnnotation(FileSuffix.class)).map(FileSuffix::value).orElse(".java");
        this.filePath = Optional.ofNullable(configuration.getMethod("filePath").getAnnotation(FilePath.class)).map(FilePath::value).orElse("");
    }

    private File initFile(DataBaseInfo info) throws IOException {
        String parentPath = new File(this.filePath).getAbsolutePath();
        String savePath = parentPath.endsWith(File.separator) ?
                parentPath + packageName.replace(".", File.separator) :
                parentPath + File.separator + packageName.replace(".", File.separator);
        Optional.of(new File(savePath)).filter(e -> !e.exists()).map(File::mkdirs);
        String filePath = savePath + File.separator + CommonUtil.convert2Hump(info.getTableName(), true) + this.fileSuffix;
        File javaFile = new File(filePath);
        if(javaFile.exists() && !javaFile.delete()) {
            log.error(": delete file failed !");
            return null;
        }
        if(!javaFile.createNewFile()) {
            log.error(": create file failed !");
            return null;
        }
        return javaFile;
    }

    private void findDataInfo(DataBaseMapper dataBaseMapper) throws IOException {
        List<DataBaseInfo> infos = dataBaseMapper.findDataBaseInfo(this.databaseName);
        List<DataBaseInfo> dataBaseInfos = Optional.ofNullable(this.tables).filter(e -> !e.isEmpty()).map(e -> infos.stream().filter(info -> e.contains(info.getTableName())).collect(Collectors.toList())).orElse(infos);
        for (DataBaseInfo info : dataBaseInfos) {
            info.setTableInfos(dataBaseMapper.findTableInfo(info.getDataBaseName(), info.getTableName()));
            this.write(info);
        }
    }

    private void write(DataBaseInfo dataBaseInfo) throws IOException {
        File javaFile = initFile(dataBaseInfo);
        BufferedWriter out = new BufferedWriter(new FileWriter(javaFile));
        generateTemplate.generate(dataBaseInfo, this.packageName, out);
        out.flush();
        log.debug(": generate resource:[{}] success --> [{}]", javaFile.getName(), javaFile.getAbsolutePath());
    }

    public void generate() throws NoSuchMethodException, IOException {
        SqlSession sqlSession = new SqlSession();
        for(GeneratePojoConfiguration configuration : pojoConfigurations) {
            this.initConfiguration(configuration.getClass());
            sqlSession.setDataSource(configuration.getDataSource());
            DataBaseMapper dataBaseMapper = sqlSession.getProxyObject(this.dataBaseMapper);
            this.findDataInfo(dataBaseMapper);
        }
    }

    private interface DataBaseMapper {

        List<DataBaseInfo> findDataBaseInfo(String dataBaseName);

        List<TableInfo> findTableInfo(String dataBaseName, String tableName);
    }

    private interface OracleDataBaseMapper extends DataBaseMapper {
        @Override
        @SelectList("select OWNER \"dataBaseName\", TABLE_NAME, COMMENTS \"tableComment\" from all_tab_comments where OWNER = #{dataBaseName}")
        List<DataBaseInfo> findDataBaseInfo(@Param("dataBaseName") String dataBaseName);

        @Override
        @SelectList("SELECT t.table_name, t.column_name \"field\", t.data_type \"fieldType\", c.comments \"fieldComment\" from user_tab_columns t join user_col_comments c on t.table_name = c.table_name and t.column_name = c.column_name where t.table_name = upper(#{tableName})")
        List<TableInfo> findTableInfo(@Param("dataBaseName") String dataBaseName, @Param("tableName") String tableName);
    }

    private interface MySQLDataBaseMapper extends DataBaseMapper {
        @Override
        @SelectList("select table_schema dataBaseName, table_name, table_comment from information_schema.tables where table_schema = #{dataBaseName}")
        List<DataBaseInfo> findDataBaseInfo(@Param("dataBaseName") String dataBaseName);

        @Override
        @SelectList("select table_name, column_name field, data_type fieldType, column_comment fieldComment from information_schema.COLUMNS where table_schema = #{dataBaseName} and table_name = #{tableName}")
        List<TableInfo> findTableInfo(@Param("dataBaseName") String dataBaseName, @Param("tableName") String tableName);
    }
}
