package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.processor;

import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeDefinition;
import org.thymeleaf.engine.AttributeDefinitions;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.engine.IAttributeDefinitionsAware;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.Validate;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述: 标签处理器
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
public abstract class AbstractLoveqqAttributeTagProcessor extends AbstractAttributeTagProcessor implements IAttributeDefinitionsAware {
    protected static final String ID_ATTR_NAME = "id";
    protected static final String TYPE_ATTR_NAME = "type";
    protected static final String NAME_ATTR_NAME = "name";
    protected static final String VALUE_ATTR_NAME = "value";
    protected static final String CHECKED_ATTR_NAME = "checked";
    protected static final String SELECTED_ATTR_NAME = "selected";
    protected static final String DISABLED_ATTR_NAME = "disabled";
    protected static final String MULTIPLE_ATTR_NAME = "multiple";

    protected AttributeDefinition idAttributeDefinition;
    protected AttributeDefinition typeAttributeDefinition;
    protected AttributeDefinition nameAttributeDefinition;
    protected AttributeDefinition valueAttributeDefinition;
    protected AttributeDefinition checkedAttributeDefinition;
    protected AttributeDefinition selectedAttributeDefinition;
    protected AttributeDefinition disabledAttributeDefinition;
    protected AttributeDefinition multipleAttributeDefinition;

    public AbstractLoveqqAttributeTagProcessor(final String dialectPrefix, final String elementName, final String attributeName, final int precedence) {
        super(TemplateMode.HTML, dialectPrefix, elementName, false, attributeName, true, precedence, false);
    }

    @Override
    public void setAttributeDefinitions(AttributeDefinitions attributeDefinitions) {
        Validate.notNull(attributeDefinitions, "Attribute Definitions cannot be null");
        this.idAttributeDefinition = attributeDefinitions.forName(TemplateMode.HTML, ID_ATTR_NAME);
        this.typeAttributeDefinition = attributeDefinitions.forName(TemplateMode.HTML, TYPE_ATTR_NAME);
        this.nameAttributeDefinition = attributeDefinitions.forName(TemplateMode.HTML, NAME_ATTR_NAME);
        this.valueAttributeDefinition = attributeDefinitions.forName(TemplateMode.HTML, VALUE_ATTR_NAME);
        this.checkedAttributeDefinition = attributeDefinitions.forName(TemplateMode.HTML, CHECKED_ATTR_NAME);
        this.selectedAttributeDefinition = attributeDefinitions.forName(TemplateMode.HTML, SELECTED_ATTR_NAME);
        this.disabledAttributeDefinition = attributeDefinitions.forName(TemplateMode.HTML, DISABLED_ATTR_NAME);
        this.multipleAttributeDefinition = attributeDefinitions.forName(TemplateMode.HTML, MULTIPLE_ATTR_NAME);
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
        if (this.continueProcess(context, tag, attributeName, attributeValue, structureHandler)) {
            this.doProcessInternal(context, tag, attributeName, attributeValue, structureHandler);
        }
    }

    protected boolean continueProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
        return this.getMatchingAttributeName().getMatchingAttributeName().getAttributeName().equals(attributeName.getAttributeName());
    }

    protected abstract void doProcessInternal(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler);

    public static Object buildEvaluatorContext(ITemplateContext context) {
        Map<String, Object> params = new HashMap<>();

        Object target = context.getSelectionTarget();

        // 先将目标属性放进去
        if (target != null) {
            params = BeanUtil.copyProperties(target);
        }

        // 搜索变量放进去
        for (String variableName : context.getVariableNames()) {
            Object variable = context.getVariable(variableName);
            params.put(variableName, variable);
        }

        return params;
    }
}
