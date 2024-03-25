package com.kfyty.database.generator.template.enjoy;

import com.jfinal.template.Directive;
import com.jfinal.template.Engine;
import com.jfinal.template.Env;
import com.jfinal.template.Template;
import com.jfinal.template.io.Writer;
import com.jfinal.template.source.StringSource;
import com.jfinal.template.stat.Scope;
import com.kfyty.core.io.SimpleBufferedWriter;
import com.kfyty.core.utils.IOUtil;
import com.kfyty.database.generator.config.GeneratorConfiguration;
import com.kfyty.database.generator.info.AbstractTableStructInfo;
import com.kfyty.database.generator.template.AbstractTemplateEngine;
import com.kfyty.database.generator.template.GeneratorTemplate;
import com.kfyty.database.util.TemplateEngineUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 功能描述: enjoy 模板
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/7 14:30
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public class EnjoyTemplate extends AbstractTemplateEngine {
    private static Engine engine;

    private Template template;

    public EnjoyTemplate(String prefix, String template) {
        super(prefix, template);
    }

    public EnjoyTemplate create(String prefix, String template) {
        return new EnjoyTemplate(prefix, template);
    }

    @Override
    public List<? extends GeneratorTemplate> loadTemplates(String prefix) {
        return TemplateEngineUtil.loadEnjoyTemplates(this, prefix);
    }

    @Override
    public void doGenerate(AbstractTableStructInfo tableInfo, GeneratorConfiguration configuration, SimpleBufferedWriter out) {
        this.initTemplate();
        loadVariables(tableInfo, configuration);
        this.template.render(this.variable, out);
    }

    protected void initTemplate() {
        if (engine == null) {
            engine = Engine.create("enjoyTemplateGenerator");
            engine.addDirective("now", NowDirective.class);
        }
        if (this.template == null) {
            URL resource = this.getClass().getResource(TemplateEngineUtil.getTemplatePath(prefix) + "/" + super.template);
            String templateString = IOUtil.toString(IOUtil.newInputStream(Objects.requireNonNull(resource, "enjoy template not found")));
            this.template = engine.getTemplate(new StringSource(templateString, super.template));
        }
    }

    public static class NowDirective extends Directive {

        public void exec(Env env, Scope scope, Writer writer) {
            write(writer, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
    }
}
