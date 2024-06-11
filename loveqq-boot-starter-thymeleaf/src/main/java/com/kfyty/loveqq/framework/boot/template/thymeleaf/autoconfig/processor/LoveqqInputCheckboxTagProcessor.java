package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.processor;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeDefinition;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.util.StandardProcessorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * 描述: input checkbox 处理器
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@Component
public class LoveqqInputCheckboxTagProcessor extends AbstractLoveqqAttributeTagProcessor {

    public LoveqqInputCheckboxTagProcessor() {
        this("th");
    }

    public LoveqqInputCheckboxTagProcessor(String dialectPrefix) {
        super(dialectPrefix, "input", "checked", 0);
    }

    @Override
    protected void doProcessInternal(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
        Map<String, String> attributeMap = tag.getAttributeMap();
        String express = attributeValue.replaceAll("[*${}]", "");
        String value = OgnlUtil.compute(express, buildEvaluatorContext(context), String.class);

        // radio checked 处理
        if (value != null && Objects.equals(tag.getAttribute(TYPE_ATTR_NAME).getValue(), "radio")) {
            processRadio(value, this.checkedAttributeDefinition, context, tag, structureHandler);
        }
        // 其他 checked 处理
        else {
            if (value != null && !attributeMap.containsKey(CHECKED_ATTR_NAME)) {
                StandardProcessorUtils.setAttribute(structureHandler, this.checkedAttributeDefinition, CHECKED_ATTR_NAME, value);
            }
        }
    }

    public static void processRadio(String value, AttributeDefinition checkedAttributeDefinition, ITemplateContext context, IProcessableElementTag tag, IElementTagStructureHandler structureHandler) {
        String checkedValue;
        if (tag.getAttribute("value") != null) {
            checkedValue = tag.getAttributeValue("value");
        } else if (tag.getAttribute("th:value") != null) {
            String express = tag.getAttributeValue("th:value").replaceAll("[*${}]", "");
            checkedValue = OgnlUtil.compute(express, buildEvaluatorContext(context), String.class);
        } else {
            throw new TemplateProcessingException("The input radio tag not found value.");
        }

        if (Objects.equals(checkedValue, value)) {
            StandardProcessorUtils.setAttribute(structureHandler, checkedAttributeDefinition, CHECKED_ATTR_NAME, value);
        }
    }
}
