package com.kfyty.database.jdbc.sql.dynamic.freemarker;

import com.kfyty.database.jdbc.mapping.freemarker.FreemarkerTemplateStatement;
import com.kfyty.database.jdbc.sql.dynamic.AbstractDynamicProvider;
import com.kfyty.core.utils.IOUtil;
import com.kfyty.core.utils.XmlUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.kfyty.database.jdbc.session.Configuration.EXECUTE_LABEL;
import static com.kfyty.database.jdbc.session.Configuration.MAPPER_NAMESPACE;
import static com.kfyty.database.jdbc.session.Configuration.MAPPER_STATEMENT_ID;
import static com.kfyty.database.jdbc.session.Configuration.SELECT_LABEL;
import static com.kfyty.core.utils.CommonUtil.BLANK_LINE_PATTERN;
import static com.kfyty.core.utils.FreemarkerUtil.FREEMARKER_SUFFIX;
import static com.kfyty.core.utils.FreemarkerUtil.buildTemplate;
import static com.kfyty.core.utils.FreemarkerUtil.renderTemplate;
import static com.kfyty.core.utils.XmlUtil.resolveAttribute;

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
    protected freemarker.template.Configuration freemarkerConfiguration;

    @Override
    public List<FreemarkerTemplateStatement> resolve(List<String> paths) {
        List<FreemarkerTemplateStatement> templateStatements = new ArrayList<>();
        for (String path : paths) {
            for (File file : IOUtil.scanFiles(path, f -> f.getName().endsWith(FREEMARKER_SUFFIX))) {
                Element rootElement = XmlUtil.create(file).getDocumentElement();
                String namespace = resolveAttribute(rootElement, MAPPER_NAMESPACE, () -> new IllegalArgumentException("namespace can't empty"));
                NodeList select = rootElement.getElementsByTagName(SELECT_LABEL);
                NodeList execute = rootElement.getElementsByTagName(EXECUTE_LABEL);
                templateStatements.addAll(this.resolve(namespace, SELECT_LABEL, select));
                templateStatements.addAll(this.resolve(namespace, EXECUTE_LABEL, execute));
            }
        }
        return templateStatements;
    }

    @Override
    public String processTemplate(FreemarkerTemplateStatement template, Map<String, Object> params) {
        String sql = renderTemplate(template.getTemplate(), params);
        return sql.replaceAll(BLANK_LINE_PATTERN.pattern(), "").trim();
    }

    protected List<FreemarkerTemplateStatement> resolve(String namespace, String labelType, NodeList nodeList) {
        List<FreemarkerTemplateStatement> templateStatements = new ArrayList<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            String ftl = element.getTextContent();
            String id = namespace + "." + resolveAttribute(element, MAPPER_STATEMENT_ID, () -> new IllegalArgumentException("id can't empty"));
            templateStatements.add(new FreemarkerTemplateStatement(id, labelType, buildTemplate(id, new StringReader(ftl), this.freemarkerConfiguration)));
        }
        return templateStatements;
    }
}
