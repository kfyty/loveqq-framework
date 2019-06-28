package com.kfyty;

import com.kfyty.jdbc.SqlSession;
import com.kfyty.mapper.TestMapper;
import com.kfyty.util.DataSourceUtil;


/**
 * 功能描述: 测试
 *
 * @author zhangkun@wisdombud.com
 * @date 2019/6/14 11:36
 * @since JDK 1.8
 */
public class Main  {

    public static void main(String[] args) throws Exception {
        SqlSession session = new SqlSession(DataSourceUtil.getDataSource("/druid.properties"));
        TestMapper testMapper = (TestMapper) session.getProxyObject(TestMapper.class);
        testMapper.findAllTest().stream().forEach(System.out::println);
    }
}
