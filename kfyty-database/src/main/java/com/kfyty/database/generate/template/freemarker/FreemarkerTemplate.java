package com.kfyty.database.generate.template.freemarker;

import com.kfyty.database.generate.GenerateSourcesBufferedWriter;
import com.kfyty.database.generate.info.AbstractTableStructInfo;
import com.kfyty.database.generate.template.AbstractGenerateTemplate;
import com.kfyty.database.generate.template.AbstractTemplateEngine;
import com.kfyty.database.util.TemplateEngineUtil;
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

    public FreemarkerTemplate(String prefix, String template) {
        super(prefix, template);
    }

    public FreemarkerTemplate create(String prefix, String template) {
        return new FreemarkerTemplate(prefix, template);
    }

    @Override
    public List<? extends AbstractGenerateTemplate> loadTemplates(String prefix) throws Exception {
        return TemplateEngineUtil.loadFreemarkerTemplates(this, prefix);
    }

    @Override
    public void generate(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        try {
            loadVariables(tableInfo, basePackage);
            TemplateEngineUtil.loadFreemarkerTemplate(this.prefix, this.template).process(this.variable, out);
        } catch (TemplateException e) {
            log.error("generate source error: ", e);
        }
    }
}
