package com.kfyty.support.jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/3 15:43
 * @email kfyty725@hotmail.com
 */
@Data
@AllArgsConstructor
public class MethodParameter {
    private Class<?> paramType;
    private Object value;
}
