package com.kfyty.configuration;

import com.kfyty.generate.GenerateSources;
import com.kfyty.generate.configuration.GenerateConfigurable;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 功能描述: 应用配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:33
 * @since JDK 1.8
 */
@NoArgsConstructor
public class ApplicationConfigurable {
    @Getter
    private GenerateConfigurable generateConfigurable;

    public void initApplicationConfigurable() throws Exception {
        this.generateConfigurable = new GenerateConfigurable();
    }

    public void executeAutoGenerateSources() throws Exception {
        if(!this.getGenerateConfigurable().isAutoGenerate()) {
            return;
        }
        GenerateSources generateSources = new GenerateSources(generateConfigurable);
        for (AbstractGenerateTemplate generateTemplate : generateConfigurable.getGenerateTemplateSet()) {
            generateSources.refreshGenerateTemplate(generateTemplate).generate();
        }
    }
}
