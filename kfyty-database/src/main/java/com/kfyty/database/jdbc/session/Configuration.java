package com.kfyty.database.jdbc.session;

import com.kfyty.database.jdbc.intercept.Interceptor;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.sql.DataSource;
import java.util.List;

/**
 * 描述: 全局配置
 *
 * @author kfyty725
 * @date 2021/8/8 10:42
 * @email kfyty725@hotmail.com
 */
@Data
@Accessors(chain = true)
public class Configuration {
    private DataSource dataSource;
    private List<Interceptor> interceptors;
}
