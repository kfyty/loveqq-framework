package com.kfyty.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述: 测试基类
 *
 * @author zhangkun@wisdombud.com
 * @date 2019/8/2 16:30
 * @since JDK 1.8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseTest {
    protected Integer id;
    protected String name;
}
