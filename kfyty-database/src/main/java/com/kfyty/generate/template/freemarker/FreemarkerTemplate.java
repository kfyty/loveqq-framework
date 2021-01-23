package com.kfyty.generate.template.freemarker;

import com.kfyty.generate.GenerateSourcesBufferedWriter;
import com.kfyty.generate.info.AbstractTableStructInfo;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.generate.template.AbstractTemplateEngine;
import com.kfyty.util.TemplateEngineUtil;
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

    @Override
    public List<? extends AbstractGenerateTemplate> loadTemplates(String prefix) throws Exception {
        return TemplateEngineUtil.loadFreemarkerTemplates(prefix);
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
