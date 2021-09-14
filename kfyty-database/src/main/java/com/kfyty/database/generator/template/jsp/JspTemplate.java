package com.kfyty.database.generator.template.jsp;

import com.kfyty.database.generator.config.GeneratorConfiguration;
import com.kfyty.support.io.SimpleBufferedWriter;
import com.kfyty.database.generator.info.AbstractTableStructInfo;
import com.kfyty.database.generator.template.GeneratorTemplate;
import com.kfyty.database.generator.template.AbstractTemplateEngine;
import com.kfyty.kjte.JstlRenderEngine;
import com.kfyty.kjte.JstlTemplateEngine;
import com.kfyty.kjte.config.JstlTemplateEngineConfig;
import com.kfyty.database.util.CodeGeneratorTemplateEngineUtil;
import javassist.CannotCompileException;
import javassist.ClassPool;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
     * 当前 jsp 模板编译生成的 class 文件路径
     */
    private String classPath;

    /**
     * jsp 文件的 class 对象
     */
    private Class<?> jspClass;

    private JstlTemplateEngine templateEngine;

    public JspTemplate(String prefix, File jsp, String classPath) {
        super(prefix, jsp.getName());
        this.classPath = classPath;
        JstlTemplateEngineConfig config = new JstlTemplateEngineConfig(CodeGeneratorTemplateEngineUtil.getTemplatePath(prefix), Collections.singletonList(jsp));
        this.templateEngine = new JstlTemplateEngine(config);
    }

    public JspTemplate create(String prefix, File jsp, String classPath) {
        return new JspTemplate(prefix, jsp, classPath);
    }

    @Override
    public List<? extends GeneratorTemplate> loadTemplates(String prefix) {
        return CodeGeneratorTemplateEngineUtil.loadJspTemplates(this, prefix);
    }

    @Override
    public void doGenerate(AbstractTableStructInfo tableInfo, GeneratorConfiguration configuration, SimpleBufferedWriter out) throws IOException {
        this.initClass();
        this.loadVariables(tableInfo, configuration);
        this.templateEngine.getConfig().clearVar().putVar(this.variable);
        this.templateEngine.getConfig().setOut(out);
        JstlRenderEngine render = new JstlRenderEngine(this.templateEngine, Collections.singletonList(this.jspClass));
        render.doRenderTemplate();
    }

    protected void initClass() {
        if (this.jspClass == null) {
            try {
                String clazz = this.classPath.replace(CLASS_SUFFIX, "").replace(".", "_") + CLASS_SUFFIX;
                this.jspClass = ClassPool.getDefault().makeClass(new FileInputStream(clazz)).toClass();
            } catch (IOException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
