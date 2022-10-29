package com.kfyty.excel.processor;

import com.kfyty.excel.model.TemplateSheet;
import com.kfyty.core.utils.ExceptionUtil;
import com.kfyty.core.utils.IOUtil;
import com.kfyty.core.utils.ZipUtil;
import lombok.Getter;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.kfyty.excel.model.TemplateSheet.resolveSheet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

/**
 * 描述: 支持 excel 模板并行导出，一边处理数据，一边写出到输出流
 * 如果写出到客户端，需自行设置请求头
 *
 * @author kfyty725
 * @date 2022/6/29 17:22
 * @email kfyty725@hotmail.com
 */
@Getter
public class TemplateExcelParallelExport implements AutoCloseable {
    /**
     * 默认的模板
     */
    public static final String DEFAULT_TEMPLATE = "public.xlsx";

    /**
     * xlsx 模板路径
     */
    public static final String EXCEL_SHEET_TEMPLATE_BASE_PATH = "excel/templates/";

    /**
     * 表头变量名称
     */
    public static final String TEMPLATE_TITLES_PARAM_NAME = "titles";

    /**
     * 行数据名称
     */
    public static final String TEMPLATE_ROWS_PARAM_NAME = "rows";

    /**
     * 当前写入行名称
     */
    public static final String TEMPLATE_CURRENT_ROW_PARAM_NAME = "currentRow";

    /**
     * xlsx 模板文件
     */
    private final ZipFile zipFile;

    /**
     * 输出流
     */
    private final ArchiveOutputStream out;

    /**
     * 表头
     */
    private final Map<String, TemplateSheet> sheets;

    /**
     * 当前写入的工作簿
     */
    private TemplateSheet currentSheet;

    /**
     * 是否已写入公共条目
     */
    private boolean writePublicItems;

    /**
     * 构造器
     *
     * @param out   输出流
     * @param clazz 导出实体类
     */
    public TemplateExcelParallelExport(OutputStream out, Class<?> clazz) {
        this(DEFAULT_TEMPLATE, out, clazz);
    }

    /**
     * 构造器
     *
     * @param template 模板名称，默认为 public，会到 /excel/templates/*.xlsx 下查找
     * @param out      输出流
     * @param classes  导出实体类
     */
    public TemplateExcelParallelExport(String template, OutputStream out, Class<?>... classes) {
        try {
            this.zipFile = Objects.requireNonNull(ZipUtil.createZip(EXCEL_SHEET_TEMPLATE_BASE_PATH + template), "xlsx template file not found");
            this.out = new ArchiveStreamFactory().createArchiveOutputStream("zip", out);
            this.sheets = Arrays.stream(classes).map(clazz -> resolveSheet(clazz, this.zipFile)).collect(toMap(TemplateSheet::getSheetName, v -> v));
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 开始导出 excel，必须首先调用，然后才能开始写数据
     */
    public void start() {
        if (this.sheets.size() != 1) {
            throw new IllegalArgumentException("there are multiple sheets, the starting sheet must be specified !");
        }
        this.start(this.sheets.values().iterator().next().getSheetName());
    }

    /**
     * 开始导出 excel，必须首先调用，然后才能开始写数据
     */
    public void start(String sheet) {
        try {
            this.writePublicItems();
            this.currentSheet = requireNonNull(this.sheets.get(sheet), "the sheet does not exist: " + sheet);
            this.out.putArchiveEntry(new ZipArchiveEntry(this.currentSheet.getSheetPath()));
            this.currentSheet.start(this.out);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 写入数据
     *
     * @param record 数据
     */
    @SuppressWarnings("unchecked")
    public void write(Object record) {
        if (record == null) {
            return;
        }
        if (record instanceof List) {
            this.currentSheet.write(this.out, (List<Object>) record);
            return;
        }
        this.currentSheet.write(this.out, Collections.singletonList(record));
    }

    /**
     * 数据写入完毕后，必须调用此方法以完成 excel 导出
     */
    public void end() {
        try {
            this.currentSheet.end(this.out);
            this.out.closeArchiveEntry();
            this.currentSheet = null;
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 写入完毕后可以调用此方法，如果 excel 打开失败
     */
    @Override
    public void close() {
        try {
            IOUtil.close(this.out);
            IOUtil.close(this.zipFile);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 将 excel 的公共条目先写出
     */
    private void writePublicItems() {
        try {
            if (this.writePublicItems) {
                return;
            }
            this.writePublicItems = true;
            Enumeration<? extends ZipEntry> entries = this.zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (this.sheets.values().stream().anyMatch(sheet -> sheet.getSheetPath().equals(zipEntry.getName()))) {
                    continue;
                }
                this.out.putArchiveEntry(new ZipArchiveEntry(zipEntry));
                IOUtil.copy(this.zipFile.getInputStream(zipEntry), this.out);
                this.out.closeArchiveEntry();
            }
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
