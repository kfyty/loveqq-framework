package com.kfyty.generate.template.jsp;

import com.kfyty.generate.GenerateSourcesBufferedWriter;
import com.kfyty.generate.info.AbstractTableStructInfo;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.generate.template.AbstractTemplateEngine;
import com.kfyty.kjte.JstlRenderEngine;
import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import com.kfyty.util.TemplateEngineUtil;
import javassist.ClassPool;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.kfyty.kjte.JstlTemplateEngine.CLASS_SUFFIX;

/**
 * 描述: jsp 模板
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/7 14:30
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public class JspTemplate extends AbstractTemplateEngine {
    /**
     * jsp 模板 class 缓存
     */
    private static final ThreadLocal<Map<File, String>> cache = new ThreadLocal<>();

    /**
     * 当前模板 jsp 文件
     */
    private File jsp;

    /**
     * jsp 文件的 class 对象
     */
    private Class<?> jspClass;

    private JstlTemplateEngineConfig config;

    public JspTemplate(String prefix, String template, File jsp) {
        super(prefix, template);
        this.jsp = jsp;
        this.config = new JstlTemplateEngineConfig(TemplateEngineUtil.getTemplatePath(prefix), Collections.singletonList(jsp));
    }

    @Override
    public List<? extends AbstractGenerateTemplate> loadTemplates(String prefix) throws Exception {
        return TemplateEngineUtil.loadJspTemplates(prefix, cache);
    }

    @Override
    public void generate(AbstractTableStructInfo tableInfo, String basePackage, GenerateSourcesBufferedWriter out) throws IOException {
        this.initClass();
        this.loadVariables(tableInfo, basePackage);
        this.config.clearVar().putVar(variable);
        this.config.setOut(out);
        JstlRenderEngine render = new JstlRenderEngine(Collections.singletonList(jspClass), config);
        render.doRenderHtml();
    }

    private void initClass() {
        if(this.jspClass != null) {
            return;
        }
        try {
            String clazz = cache.get().get(this.jsp).replace(CLASS_SUFFIX, "").replace(".", "_") + CLASS_SUFFIX;
            this.jspClass = ClassPool.getDefault().makeClass(new FileInputStream(clazz)).toClass();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
