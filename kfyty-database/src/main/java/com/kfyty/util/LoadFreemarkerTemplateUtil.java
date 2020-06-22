package com.kfyty.util;

import com.kfyty.generate.template.freemarker.FreemarkerTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class LoadFreemarkerTemplateUtil {
    public static List<FreemarkerTemplate> loadTemplates(String prefix) throws Exception {
        log.debug(": load template for prefix: '" + prefix + "' !");
        String key = CommonUtil.empty(prefix) ? "template" : prefix + ".template";
        String templateNames = FreemarkerUtil.loadGenerateProperties().getProperty(key);
        return CommonUtil.empty(templateNames) ? null : Arrays.stream(templateNames.split(","))
                .map(e -> new FreemarkerTemplate(prefix, e) {
                    @Override
                    public String classSuffix() {
                        String suffix = e.substring(0, e.indexOf("."));
                        return suffix.endsWith("_NoSu") ? "" : CommonUtil.convert2Hump(suffix, true);
                    }

                    @Override
                    public String fileTypeSuffix() {
                        String s = e.toLowerCase().replace(".ftl", "");
                        return s.substring(s.lastIndexOf("."));
                    }
                })
                .collect(Collectors.toList());
    }
}
