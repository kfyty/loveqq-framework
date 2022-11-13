package com.kfyty.boot.context.env;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.autoconfig.env.PlaceholdersResolver;
import com.kfyty.core.autoconfig.env.PropertyContext;
import com.kfyty.core.utils.PlaceholdersUtil;
import lombok.Data;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/11/12 18:02
 * @email kfyty725@hotmail.com
 */
@Data
@Component
public class DefaultPropertyPlaceholdersResolver implements PlaceholdersResolver {
    /**
     * 占位符
     */
    protected String placeholder;

    /**
     * 左占位符
     */
    protected String left;

    /**
     * 右占位符
     */
    protected String right;

    @Autowired
    protected PropertyContext propertyContext;

    public DefaultPropertyPlaceholdersResolver() {
        this("$");
    }

    public DefaultPropertyPlaceholdersResolver(String placeholder) {
        this(placeholder, "{", "}");
    }

    public DefaultPropertyPlaceholdersResolver(String placeholder, String left, String right) {
        this.placeholder = placeholder;
        this.left = left;
        this.right = right;
    }

    @Override
    public String resolvePlaceholders(String value) {
        return PlaceholdersUtil.resolve(value, this.placeholder, this.left, this.right, this.propertyContext.getProperties());
    }
}
