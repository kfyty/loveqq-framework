package com.kfyty.excel.model;

import com.kfyty.excel.annotation.TemplateExcel;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.IOUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.core.utils.XmlUtil;
import freemarker.template.Template;
import lombok.Data;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.kfyty.excel.processor.TemplateExcelParallelExport.EXCEL_SHEET_TEMPLATE_BASE_PATH;
import static com.kfyty.excel.processor.TemplateExcelParallelExport.TEMPLATE_CURRENT_ROW_PARAM_NAME;
import static com.kfyty.excel.processor.TemplateExcelParallelExport.TEMPLATE_ROWS_PARAM_NAME;
import static com.kfyty.excel.processor.TemplateExcelParallelExport.TEMPLATE_TITLES_PARAM_NAME;
import static com.kfyty.excel.utils.FreemarkerUtil.buildTemplate;
import static com.kfyty.excel.utils.FreemarkerUtil.renderTemplate;
import static com.kfyty.core.utils.StreamUtil.throwMergeFunction;
import static com.kfyty.core.utils.XmlUtil.resolveAttribute;
import static com.kfyty.core.utils.XmlUtil.resolveChildAttribute;
import static com.kfyty.core.utils.XmlUtil.resolveElement;
import static com.kfyty.core.utils.ZipUtil.createZip;
import static com.kfyty.core.utils.ZipUtil.getInputStream;

/**
 * 描述: 模板工作簿
 *
 * @author kfyty725
 * @date 2021/7/15 16:51
 * @email kfyty725@hotmail.com
 */
@Data
public class TemplateSheet {
    private static final Predicate<ZipEntry> SHARE_STRING_PREDICATE = zip -> zip.getName().equals("xl/sharedStrings.xml");

    private static final Predicate<ZipEntry> WORKBOOK_PREDICATE = zip -> zip.getName().equals("xl/workbook.xml");

    private static final Predicate<ZipEntry> WORKBOOK_REL_PREDICATE = zip -> zip.getName().equals("xl/_rels/workbook.xml.rels");

    private static final String DEFAULT_TITLE_TEMPLATE =
            "<sheetData>\n" +
                    "<#assign alphas = [\"A\", \"B\", \"C\", \"D\", \"E\", \"F\", \"G\", \"H\", \"I\", \"J\", \"K\", \"L\", \"M\", \"N\", \"O\", \"P\", \"Q\", \"R\", \"S\", \"T\", \"U\", \"V\", \"W\", \"X\", \"Y\", \"Z\"] />\n" +
                    "    <row r=\"1\" spans=\"1:14\" customFormat=\"1\" customHeight=\"1\" x14ac:dyDescent=\"0.3\">\n" +
                    "        <#list titles as title>\n" +
                    "            <c r=\"${alphas[title_index]}1\" t=\"inlineStr\">\n" +
                    "                <is><t>${title.title}</t></is>\n" +
                    "            </c>\n" +
                    "        </#list>\n" +
                    "    </row>";

    private static final String DEFAULT_ROW_TEMPLATE =
            "<#assign alphas = [\"A\", \"B\", \"C\", \"D\", \"E\", \"F\", \"G\", \"H\", \"I\", \"J\", \"K\", \"L\", \"M\", \"N\", \"O\", \"P\", \"Q\", \"R\", \"S\", \"T\", \"U\", \"V\", \"W\", \"X\", \"Y\", \"Z\"] />\n" +
                    "<#list rows as row>\n" +
                    "    <row r=\"${row_index + currentRow}\" spans=\"1:14\" customHeight=\"1\" x14ac:dyDescent=\"0.3\">\n" +
                    "        <#list row.cells as cell>\n" +
                    "            <c r=\"${alphas[cell_index]}${row_index + currentRow}\" t=\"inlineStr\">\n" +
                    "                <is><t>${cell.data}</t></is>\n" +
                    "            </c>\n" +
                    "        </#list>\n" +
                    "    </row>\n" +
                    "</#list>";

    /**
     * 模板工作簿名称
     */
    private String sheetName;

    /**
     * 解析出的 sheet 路径
     */
    private String sheetPath;

    /**
     * 解析出的表头
     */
    private Map<String, TemplateTitle> titles;

    /**
     * 开始写入数据模板
     */
    private Template startTemplate;

    /**
     * 写入数据模板
     */
    private Template writeTemplate;

    /**
     * 结束写入数据模板
     */
    private Template endTemplate;

    /**
     * 当前写入的行
     */
    private int currentRow;

    /**
     * 写入标题到输出流
     *
     * @param out 输出流
     */
    public void start(OutputStream out) {
        this.currentRow = 2;
        Map<String, Object> params = new HashMap<>(4);
        params.put(TEMPLATE_TITLES_PARAM_NAME, this.titles.values());
        String start = renderTemplate(this.startTemplate, params);
        IOUtil.write(out, start.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 写入多条数据
     *
     * @param records 数据
     */
    public void write(OutputStream out, List<Object> records) {
        if (CommonUtil.empty(records)) {
            return;
        }
        List<TemplateRow> templateRows = new ArrayList<>(records.size());
        for (Object record : records) {
            Map<String, Object> beanMap = BeanUtil.copyProperties(record);
            List<TemplateCell> cells = titles.values().stream().map(e -> new TemplateCell(beanMap.get(e.getKey()))).collect(Collectors.toList());
            templateRows.add(new TemplateRow(cells));
        }
        Map<String, Object> params = new HashMap<>(4);
        params.put(TEMPLATE_ROWS_PARAM_NAME, templateRows);
        params.put(TEMPLATE_CURRENT_ROW_PARAM_NAME, this.currentRow);
        String write = renderTemplate(this.writeTemplate, params);
        IOUtil.write(out, write.getBytes(StandardCharsets.UTF_8));
        this.currentRow += templateRows.size();
    }

    /**
     * 写入工作簿结束部分
     *
     * @param out 输出流
     */
    public void end(OutputStream out) {
        String end = renderTemplate(this.endTemplate, null);
        IOUtil.write(out, end.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解析 sheet
     *
     * @param template xlsx 模板
     * @param clazz    导出承载类
     * @return sheet
     */
    public static TemplateSheet resolveSheet(String template, Class<?> clazz) {
        return resolveSheet(clazz, createZip(EXCEL_SHEET_TEMPLATE_BASE_PATH + template));
    }

    /**
     * 解析 sheet
     *
     * @param clazz   导出承载类
     * @param zipFile xlsx 模板文件
     * @return sheet
     */
    public static TemplateSheet resolveSheet(Class<?> clazz, ZipFile zipFile) {
        TemplateSheet sheet = new TemplateSheet();
        TemplateExcel annotation = AnnotationUtil.findAnnotation(clazz, TemplateExcel.class);
        Objects.requireNonNull(annotation, "TemplateExcel annotation must be declared");
        sheet.setSheetName(annotation.value());
        sheet.setSheetPath(resolveSheetPath(annotation.value(), zipFile));
        sheet.setTitles(resolveTemplateTitle(sheet.getSheetPath(), clazz, zipFile));
        return resolveSheetTemplate(sheet, zipFile);
    }

    private static String resolveRId(String sheetName, ZipFile zipFile) {
        Element element = resolveElement(XmlUtil.create(getInputStream(zipFile, WORKBOOK_PREDICATE)), "./sheets");
        return resolveChildAttribute(element, child -> resolveAttribute(child, "name").equals(sheetName), child -> resolveAttribute(child, "r:id"));
    }

    private static String resolveSheetPath(String sheetName, ZipFile zipFile) {
        String rId = resolveRId(sheetName, zipFile);
        Element element = XmlUtil.create(getInputStream(zipFile, WORKBOOK_REL_PREDICATE)).getDocumentElement();
        return "xl/" + resolveChildAttribute(element, child -> resolveAttribute(child, "Id").equals(rId), child -> resolveAttribute(child, "Target"));
    }

    private static Map<String, TemplateTitle> resolveTemplateTitle(String sheetPath, Class<?> clazz, ZipFile zipFile) {
        List<String> xlsxTitle = resolveXlsxTitle(sheetPath, zipFile);
        Map<String, TemplateTitle> titles = resolveModelTitle(clazz);
        Set<String> modelTitle = titles.values().stream().map(TemplateTitle::getTitle).collect(Collectors.toSet());
        xlsxTitle.forEach(modelTitle::remove);
        if (!xlsxTitle.isEmpty() && !modelTitle.isEmpty()) {
            throw new IllegalArgumentException("model and xlsx template titles do not match");
        }
        return titles;
    }

    private static List<String> resolveXlsxTitle(String sheetPath, ZipFile zipFile) {
        Predicate<ZipEntry> sheetPredicate = zip -> zip.getName().equals(sheetPath);
        Element sheetData = resolveElement(XmlUtil.create(getInputStream(zipFile, sheetPredicate)), "./sheetData");
        InputStream sharedStringInputStream = getInputStream(zipFile, SHARE_STRING_PREDICATE);
        if (sheetData.getChildNodes().getLength() == 0 || sharedStringInputStream == null) {
            return Collections.emptyList();
        }
        Element title = resolveElement(sheetData, "./row[0]");
        Element sharedString = XmlUtil.create(sharedStringInputStream).getDocumentElement();
        List<String> titles = XmlUtil.resolveChildrenAttribute(title, child -> resolveElement(child, "./v").getTextContent());
        return XmlUtil.resolveChildrenAttribute(sharedString, titles.stream().map(Integer::parseInt).collect(Collectors.toList()), child -> resolveElement(child, "./t").getTextContent());
    }

    private static Map<String, TemplateTitle> resolveModelTitle(Class<?> clazz) {
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

    private static TemplateSheet resolveSheetTemplate(TemplateSheet sheet, ZipFile zipFile) {
        String empty = "<sheetData/>";
        String start = "<sheetData>";
        String end = "</sheetData>";
        String rowStart = "<row";
        String rowEnd = "</row>";

        String sheetXml = IOUtil.toString(getInputStream(zipFile, zip -> zip.getName().equals(sheet.getSheetPath())));
        int emptyIndex = sheetXml.indexOf(empty);
        if (emptyIndex > 0) {
            sheet.setStartTemplate(buildTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, sheet.getSheetName() + "_start", sheetXml.substring(0, emptyIndex) + DEFAULT_TITLE_TEMPLATE));
            sheet.setWriteTemplate(buildTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, sheet.getSheetName() + "_write", DEFAULT_ROW_TEMPLATE));
            sheet.setEndTemplate(buildTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, sheet.getSheetName() + "_end", end + sheetXml.substring(emptyIndex + empty.length())));
            return sheet;
        }

        int startIndex = sheetXml.indexOf(start);
        int endIndex = sheetXml.indexOf(end, startIndex);
        String sheetData = sheetXml.substring(startIndex + start.length(), endIndex);

        String sheetEndXml = sheetXml.substring(endIndex);
        sheet.setEndTemplate(buildTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, sheet.getSheetName() + "_end", sheetEndXml));

        int rowStartIndex = sheetData.indexOf(rowStart);
        int rowEndIndex = sheetData.indexOf(rowEnd, rowStartIndex);

        if (rowEndIndex < 0) {
            sheet.setStartTemplate(buildTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, sheet.getSheetName() + "_start", sheetXml.substring(0, startIndex) + DEFAULT_TITLE_TEMPLATE));
            sheet.setWriteTemplate(buildTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, sheet.getSheetName() + "_write", DEFAULT_ROW_TEMPLATE));
            return sheet;
        }

        String row = sheetData.substring(rowStartIndex, rowEndIndex + rowEnd.length());
        String startTemplate = sheetXml.substring(0, startIndex) + buildStartTemplate(row);
        sheet.setStartTemplate(buildTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, sheet.getSheetName() + "_start", startTemplate));

        int secondRowStartIndex = sheetData.indexOf(rowStart, rowEndIndex);

        if (secondRowStartIndex < 0) {
            sheet.setWriteTemplate(buildTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, sheet.getSheetName() + "_write", DEFAULT_ROW_TEMPLATE));
            return sheet;
        }

        int secondRowEndIndex = sheetData.indexOf(rowEnd, secondRowStartIndex);
        String secondRow = sheetData.substring(secondRowStartIndex, secondRowEndIndex + rowEnd.length());
        String writeTemplate = buildWriteTemplate(secondRow);
        sheet.setWriteTemplate(buildTemplate(EXCEL_SHEET_TEMPLATE_BASE_PATH, sheet.getSheetName() + "_write", writeTemplate));

        return sheet;
    }

    private static String buildStartTemplate(String row) {
        String start = "<c";
        String end = ">";
        StringBuilder builder = new StringBuilder()
                .append("<#assign alphas = [\"A\", \"B\", \"C\", \"D\", \"E\", \"F\", \"G\", \"H\", \"I\", \"J\", \"K\", \"L\", \"M\", \"N\", \"O\", \"P\", \"Q\", \"R\", \"S\", \"T\", \"U\", \"V\", \"W\", \"X\", \"Y\", \"Z\"] />\n")
                .append("<#assign columnIndex = 0 />\n")
                .append("<sheetData>\n")
                .append(row, 0, row.indexOf(end) + 1);
        CommonUtil.iteratorSplit(row, start, end, column -> {
            if (column.contains("t=")) {
                builder.append(column
                                .replaceAll("r=\".*?\"", "r=\"\\${alphas[columnIndex]}1\"")
                                .replaceAll("t=\".*?\"", "t=\"inlineStr\""))
                        .append(">");
            } else {
                builder.append(column.replaceAll("r=\".*?\"", "r=\"\\${alphas[columnIndex]}1\""))
                        .append("t=\"inlineStr\">");
            }
            builder.append("<is><t>${titles[columnIndex].title}</t></is>\n")
                    .append("</c>\n")
                    .append("<#assign columnIndex = columnIndex + 1 />\n");
        });

        return builder.append("</row>\n").toString();
    }

    private static String buildWriteTemplate(String row) {
        String start = "<c";
        String end = ">";
        StringBuilder builder = new StringBuilder()
                .append("<#assign alphas = [\"A\", \"B\", \"C\", \"D\", \"E\", \"F\", \"G\", \"H\", \"I\", \"J\", \"K\", \"L\", \"M\", \"N\", \"O\", \"P\", \"Q\", \"R\", \"S\", \"T\", \"U\", \"V\", \"W\", \"X\", \"Y\", \"Z\"] />\n")
                .append("<#list rows as row>\n")
                .append("<#assign columnIndex = 0 />\n")
                .append(row.substring(0, row.indexOf(end) + 1).replaceAll("r=\".*?\"", "r=\"\\${row_index + currentRow}\""));

        CommonUtil.iteratorSplit(row, start, end, column -> {
            if (column.contains("t=")) {
                builder.append(column
                                .replaceAll("r=\".*?\"", "r=\"\\${alphas[columnIndex]}\\${row_index + currentRow}\"")
                                .replaceAll("t=\".*?\"", "t=\"inlineStr\""))
                        .append(">");
            } else {
                builder.append(column.replaceAll("r=\".*?\"", "r=\"\\${alphas[columnIndex]}\\${row_index + currentRow}\""))
                        .append(" t=\"inlineStr\">\n");
            }
            builder.append("<is><t>${row.cells[columnIndex].data}</t></is>\n")
                    .append("</c>\n")
                    .append("<#assign columnIndex = columnIndex + 1 />\n");
        });

        return builder.append("</row>\n").append("</#list>\n").toString();
    }
}
