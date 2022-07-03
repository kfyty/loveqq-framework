package com.kfyty.excel.processor;

import com.kfyty.excel.annotation.TemplateExcel;
import com.kfyty.excel.model.TemplateCell;
import com.kfyty.excel.model.TemplateRow;
import com.kfyty.excel.model.TemplateTitle;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.BeanUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ExceptionUtil;
import com.kfyty.support.utils.IOUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Getter;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static com.kfyty.support.utils.FreemarkerUtil.renderTemplate;
import static com.kfyty.support.utils.StreamUtil.throwMergeFunction;

/**
 * 描述: 支持 excel 模板并行导出，一边处理数据，一边写出到输出流
 * 如果写出到客户端，需自行设置请求头
 *
 * @author kfyty725
 * @date 2022/6/29 17:22
 * @email kfyty725@hotmail.com
 */
@Getter
public class TemplateExcelParallelExport<T> implements AutoCloseable {
    /**
     * 默认的模板
     */
    public static final String DEFAULT_TEMPLATE = "public";

    /**
     * 默认工作簿
     */
    public static final String DEFAULT_SHEET = "sheet1.xml";

    /**
     * 模板公共压缩条目路径
     */
    public static final String EXCEL_PUBLIC_ITEMS_PREFIX = "excel/items/";

    /**
     * 模板 freemarker 模板路径
     */
    public static final String EXCEL_SHEET_TEMPLATE_BASE_PATH = "/excel/templates";

    /**
     * sheet 条目路径
     */
    public static final String EXCEL_SHEET_ZIP_ITEM_PATH = "xl/worksheets/";

    /**
     * 开始模板后缀
     */
    public static final String TEMPLATE_START_SUFFIX = "_start.ftl";

    /**
     * 写入数据模板后缀
     */
    public static final String TEMPLATE_WRITE_SUFFIX = "_write.ftl";

    /**
     * 结束模板后缀
     */
    public static final String TEMPLATE_END_SUFFIX = "_end.ftl";

    /**
     * 表头变量名称
     */
    public static final String TEMPLATE_TITLES_PARAM_NAME = "titles";

    /**
     * 行数据名称
     */
    public static final String TEMPLATE_ROWS_PARAM_NAME = "rows";

    /**
     * 模板名称，默认为 public
     * 对于公共项目，会到 /excel/items/{template} 下查找
     * 对于表格模板，会到 /excel/templates/{template}_*.ftl 下查找
     */
    private final String template;

    /**
     * 输出流
     */
    private final ArchiveOutputStream out;

    /**
     * 表头
     */
    private final Map<String, TemplateTitle> titles;

    public TemplateExcelParallelExport(OutputStream out, Class<T> clazz) {
        this(DEFAULT_TEMPLATE, out, clazz);
    }

    public TemplateExcelParallelExport(String template, OutputStream out, Class<T> clazz) {
        try {
            this.template = Objects.requireNonNull(template);
            this.out = new ArchiveStreamFactory().createArchiveOutputStream("zip", out);
            this.titles = this.resolveTemplateTitle(Objects.requireNonNull(clazz));
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 开始导出 excel，必须首先调用，然后才能开始写数据
     */
    public void start() {
        this.start(DEFAULT_SHEET);
    }

    /**
     * 开始导出 excel，必须首先调用，然后才能开始写数据
     */
    public void start(String sheet) {
        try {
            this.downloadPublicItems();
            this.out.putArchiveEntry(new ZipArchiveEntry(EXCEL_SHEET_ZIP_ITEM_PATH + sheet));
            Map<String, Collection<TemplateTitle>> params = new HashMap<>(4);
            params.put(TEMPLATE_TITLES_PARAM_NAME, this.titles.values());
            String start = renderTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, this.template + TEMPLATE_START_SUFFIX, params);
            IOUtil.write(this.out, start.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 写入单条数据
     *
     * @param record 数据
     */
    public void write(T record) {
        if (record != null) {
            this.write(Collections.singletonList(record));
        }
    }

    /**
     * 写入多条数据
     *
     * @param records 数据
     */
    public void write(List<T> records) {
        if (CommonUtil.empty(records)) {
            return;
        }
        List<TemplateRow> templateRows = new ArrayList<>(records.size());
        for (T record : records) {
            Map<String, Object> beanMap = BeanUtil.copyProperties(record);
            List<TemplateCell> cells = titles.values().stream().map(e -> new TemplateCell(beanMap.get(e.getKey()))).collect(Collectors.toList());
            templateRows.add(new TemplateRow(cells));
        }
        Map<String, Collection<TemplateRow>> params = new HashMap<>(4);
        params.put(TEMPLATE_ROWS_PARAM_NAME, templateRows);
        String write = renderTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, this.template + TEMPLATE_WRITE_SUFFIX, params);
        IOUtil.write(this.out, write.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 数据写入完毕后，必须调用此方法以完成 excel 导出
     */
    public void end() {
        try {
            String end = renderTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, this.template + TEMPLATE_END_SUFFIX, null);
            IOUtil.write(this.out, end.getBytes(StandardCharsets.UTF_8));
            this.out.closeArchiveEntry();
            this.out.flush();
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 写入完毕后可以调用此方法，如果 excel 打开失败
     */
    @Override
    public void close() {
        CommonUtil.close(this.out);
    }

    /**
     * 处理表头
     *
     * @param clazz 实体类
     * @return 排过序的表头
     */
    public Map<String, TemplateTitle> resolveTemplateTitle(Class<T> clazz) {
        List<TemplateTitle> templateTitles = new ArrayList<>();
        for (Map.Entry<String, Field> entry : ReflectUtil.getFieldMap(clazz).entrySet()) {
            Field field = entry.getValue();
            TemplateExcel annotation = AnnotationUtil.findAnnotation(field, TemplateExcel.class);
            if (annotation == null) {
                continue;
            }
            templateTitles.add(new TemplateTitle(field.getName(), annotation.value(), annotation.order()));
        }
        return templateTitles.stream().sorted(Comparator.comparing(TemplateTitle::getOrder)).collect(Collectors.toMap(TemplateTitle::getKey, v -> v, throwMergeFunction(), LinkedHashMap::new));
    }

    /**
     * 将 excel 的公共条目先写出
     */
    private void downloadPublicItems() {
        try {
            String path = EXCEL_PUBLIC_ITEMS_PREFIX + this.template;
            URLConnection connection = TemplateExcelParallelExport.class.getClassLoader().getResource(path).openConnection();
            if (!(connection instanceof JarURLConnection)) {
                File parent = Paths.get(TemplateExcelParallelExport.class.getResource("/" + path).toURI()).toFile();
                this.downloadPublicItems(parent.getPath() + File.separator, parent);
                return;
            }
            path = path + "/";
            JarFile jarFile = ((JarURLConnection) connection).getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String entryName = jarEntry.getName();
                if (!entryName.startsWith(path) || entryName.equals(path)) {
                    continue;
                }
                this.out.putArchiveEntry(new ZipArchiveEntry(entryName.replace(path, "")));
                IOUtil.copy(jarFile.getInputStream(jarEntry), this.out);
                this.out.closeArchiveEntry();
            }
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    private void downloadPublicItems(String parentPath, File parent) throws Exception {
        if (parent.isFile()) {
            this.out.putArchiveEntry(new ZipArchiveEntry(parent.getPath().replace(parentPath, "")));
            IOUtil.copy(Files.newInputStream(parent.toPath()), this.out);
            this.out.closeArchiveEntry();
            return;
        }
        File[] children = parent.listFiles();
        if (children != null) {
            for (File child : children) {
                this.downloadPublicItems(parentPath, child);
            }
        }
    }
}
