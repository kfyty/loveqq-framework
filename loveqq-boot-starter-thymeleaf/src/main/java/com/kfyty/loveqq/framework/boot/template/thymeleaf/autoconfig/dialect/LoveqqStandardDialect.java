package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.dialect;

import lombok.RequiredArgsConstructor;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import java.util.Set;

/**
 * 描述: loveqq 方言实现
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class LoveqqStandardDialect extends StandardDialect {
    private final Set<IProcessor> processors;

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        Set<IProcessor> processorSet = super.getProcessors(dialectPrefix);
        if (this.processors != null) {
            processorSet.addAll(this.processors);
        }
        return processorSet;
    }
}
