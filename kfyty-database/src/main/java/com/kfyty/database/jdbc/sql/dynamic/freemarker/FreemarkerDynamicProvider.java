package com.kfyty.database.jdbc.sql.dynamic.freemarker;

import com.kfyty.database.jdbc.mapping.freemarker.FreemarkerTemplateStatement;
import com.kfyty.database.jdbc.sql.dynamic.AbstractDynamicProvider;
import com.kfyty.support.utils.CommonUtil;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.kfyty.database.jdbc.session.Configuration.EXECUTE_LABEL;
import static com.kfyty.database.jdbc.session.Configuration.MAPPER_NAMESPACE;
import static com.kfyty.database.jdbc.session.Configuration.MAPPER_STATEMENT_ID;
import static com.kfyty.database.jdbc.session.Configuration.SELECT_LABEL;
import static com.kfyty.support.utils.CommonUtil.BLANK_LINE_PATTERN;
import static com.kfyty.support.utils.CommonUtil.resolveAttribute;

/**
 * 描述: 基于 freemarker 的动态 SQL 提供者
 *
 * @author kfyty725
 * @date 2021/9/29 22:43
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FreemarkerDynamicProvider extends AbstractDynamicProvider<FreemarkerTemplateStatement> {
    private static final String FREEMARKER_SUFFIX = ".ftl";

    private freemarker.template.Configuration freemarkerConfiguration;

    @Override
    public List<FreemarkerTemplateStatement> resolve(List<String> paths) {
        try {
            List<FreemarkerTemplateStatement> templateStatements = new ArrayList<>();
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            for (String path : paths) {
                for (File file : CommonUtil.scanFiles(path, f -> f.getName().endsWith(FREEMARKER_SUFFIX))) {
                    Element rootElement = documentBuilder.parse(file).getDocumentElement();
                    String namespace = resolveAttribute(rootElement, MAPPER_NAMESPACE, () -> new IllegalArgumentException("namespace can't empty"));
                    NodeList select = rootElement.getElementsByTagName(SELECT_LABEL);
                    NodeList execute = rootElement.getElementsByTagName(EXECUTE_LABEL);
                    templateStatements.addAll(this.resolve(namespace, SELECT_LABEL, select));
                    templateStatements.addAll(this.resolve(namespace, EXECUTE_LABEL, execute));
                }
            }
            return templateStatements;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String processTemplate(FreemarkerTemplateStatement template, Map<String, Object> params) {
        try {
            StringWriter sql = new StringWriter();
            template.getTemplate().process(params, sql);
            return sql.toString().replaceAll(BLANK_LINE_PATTERN.pattern(), "");
        } catch (TemplateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<FreemarkerTemplateStatement> resolve(String namespace, String labelType, NodeList nodeList) throws IOException {
        List<FreemarkerTemplateStatement> templateStatements = new ArrayList<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            String ftl = element.getTextContent();
            String id = namespace + "." + resolveAttribute(element, MAPPER_STATEMENT_ID, () -> new IllegalArgumentException("id can't empty"));
            FreemarkerTemplateStatement statement = new FreemarkerTemplateStatement(id, labelType);
            statement.setTemplate(new Template(id, new StringReader(ftl), this.freemarkerConfiguration));
            templateStatements.add(statement);
        }
        return templateStatements;
    }
}
