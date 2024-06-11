package com.kfyty.loveqq.framework.data.jdbc.sql.dynamic;

import com.kfyty.database.jdbc.mapping.TemplateStatement;
import com.kfyty.loveqq.framework.core.method.MethodParameter;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import com.kfyty.loveqq.framework.core.utils.XmlUtil;
import com.kfyty.loveqq.framework.data.jdbc.session.Configuration;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kfyty.loveqq.framework.core.utils.XmlUtil.resolveAttribute;
import static com.kfyty.loveqq.framework.data.jdbc.session.Configuration.EXECUTE_LABEL;
import static com.kfyty.loveqq.framework.data.jdbc.session.Configuration.MAPPER_NAMESPACE;
import static com.kfyty.loveqq.framework.data.jdbc.session.Configuration.MAPPER_STATEMENT_ID;
import static com.kfyty.loveqq.framework.data.jdbc.session.Configuration.SELECT_LABEL;

/**
 * 描述: 动态 SQL 提供基础实现
 *
 * @author kfyty725
 * @date 2021/9/30 13:13
 * @email kfyty725@hotmail.com
 */
@Data
public abstract class AbstractDynamicProvider<TS extends TemplateStatement> implements DynamicProvider<TS> {
    protected Configuration configuration;

    @Override
    public List<TS> resolve(List<String> paths) {
        String templateSuffix = this.getTemplateSuffix();
        List<TS> templateStatements = new ArrayList<>();
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = this.configuration.getPathMatchingResourcePatternResolver();
        for (String path : paths) {
            for (URL file : pathMatchingResourcePatternResolver.findResources(path)) {
                if (!file.getFile().endsWith(templateSuffix)) {
                    continue;
                }
                Element rootElement = XmlUtil.create(file).getDocumentElement();
                String namespace = resolveAttribute(rootElement, MAPPER_NAMESPACE, () -> new IllegalArgumentException("namespace can't empty"));
                NodeList select = rootElement.getElementsByTagName(SELECT_LABEL);
                NodeList execute = rootElement.getElementsByTagName(EXECUTE_LABEL);
                templateStatements.addAll(this.resolveInternal(namespace, SELECT_LABEL, select));
                templateStatements.addAll(this.resolveInternal(namespace, EXECUTE_LABEL, execute));
            }
        }
        return templateStatements;
    }

    @Override
    public String resolveTemplateStatementId(Class<?> mapperClass, Method mapperMethod) {
        String id = mapperClass.getName() + "." + mapperMethod.getName();
        if (!this.configuration.getTemplateStatements().containsKey(id)) {
            throw new IllegalArgumentException("template statement not exists of id: " + id);
        }
        return id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String doProvide(Class<?> mapperClass, Method mapperMethod, Map<String, MethodParameter> params) {
        String id = this.resolveTemplateStatementId(mapperClass, mapperMethod);
        Map<String, Object> parameters = this.processTemplateParameters(params);
        TemplateStatement templateStatement = this.configuration.getTemplateStatements().get(id);
        return this.processTemplate((TS) templateStatement, parameters);
    }

    protected Map<String, Object> processTemplateParameters(Map<String, MethodParameter> methodParameterMap) {
        Map<String, Object> params = new HashMap<>(methodParameterMap.size());
        for (Map.Entry<String, MethodParameter> entry : methodParameterMap.entrySet()) {
            params.put(entry.getKey(), entry.getValue().getValue());
        }
        return params;
    }

    protected List<TS> resolveInternal(String namespace, String labelType, NodeList nodeList) {
        List<TS> templateStatements = new ArrayList<>(nodeList.getLength());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            String id = namespace + "." + resolveAttribute(element, MAPPER_STATEMENT_ID, () -> new IllegalArgumentException("id can't empty"));
            String xml = element.getTextContent();
            templateStatements.add(this.buildTemplateStatement(id, labelType, xml));
        }
        return templateStatements;
    }

    protected abstract String getTemplateSuffix();

    protected abstract TS buildTemplateStatement(String id, String labelType, String content);
}
