package com.kfyty.database.generator.template.freemarker;

import com.kfyty.database.generator.config.GeneratorConfiguration;
import com.kfyty.database.generator.info.AbstractTableStructInfo;
import com.kfyty.database.generator.template.AbstractTemplateEngine;
import com.kfyty.database.generator.template.GeneratorTemplate;
import com.kfyty.database.util.TemplateEngineUtil;
import com.kfyty.support.io.SimpleBufferedWriter;
import com.kfyty.support.utils.FreemarkerUtil;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * 功能描述: freemarker 模板
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/7 14:30
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public class FreemarkerTemplate extends AbstractTemplateEngine {
    private Template freemarkerTemplate;

    public FreemarkerTemplate(String prefix, String template) {
        super(prefix, template);
    }

    public FreemarkerTemplate create(String prefix, String template) {
        return new FreemarkerTemplate(prefix, template);
    }

    @Override
    public List<? extends GeneratorTemplate> loadTemplates(String prefix) {
        return TemplateEngineUtil.loadFreemarkerTemplates(this, prefix);
    }

    @Override
    public void doGenerate(AbstractTableStructInfo tableInfo, GeneratorConfiguration configuration, SimpleBufferedWriter out) throws IOException {
        try {
            this.initTemplate();
            loadVariables(tableInfo, configuration);
            this.freemarkerTemplate.process(this.variable, out);
        } catch (TemplateException e) {
            log.error("generate source error !", e);
        }
    }

    protected void initTemplate() {
        if (this.freemarkerTemplate == null) {
            this.freemarkerTemplate = FreemarkerUtil.getTemplate(TemplateEngineUtil.getTemplatePath(prefix), this.template);
        }
    }
}
