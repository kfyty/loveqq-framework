package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.processor;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.util.StandardProcessorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * 描述: input field 处理器
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@Component
public class LoveqqInputFieldTagProcessor extends AbstractLoveqqAttributeTagProcessor {

    public LoveqqInputFieldTagProcessor() {
        this("th");
    }

    public LoveqqInputFieldTagProcessor(String dialectPrefix) {
        super(dialectPrefix, "input", "field", 0);
    }

    @Override
    protected void doProcessInternal(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
        Map<String, String> attributeMap = tag.getAttributeMap();
        String field = attributeValue.replaceAll("[*{}]", "");
        String value = OgnlUtil.compute(field, buildEvaluatorContext(context), String.class);

        if (!attributeMap.containsKey(NAME_ATTR_NAME)) {
            StandardProcessorUtils.setAttribute(structureHandler, this.nameAttributeDefinition, NAME_ATTR_NAME, field);
        }

        if (value != null && !attributeMap.containsKey(VALUE_ATTR_NAME)) {
            StandardProcessorUtils.setAttribute(structureHandler, this.valueAttributeDefinition, VALUE_ATTR_NAME, value);
        }

        if (value != null && Objects.equals(tag.getAttribute(TYPE_ATTR_NAME).getValue(), "radio")) {
            LoveqqInputCheckboxTagProcessor.processRadio(value, this.checkedAttributeDefinition, context, tag, structureHandler);
        }
    }
}
