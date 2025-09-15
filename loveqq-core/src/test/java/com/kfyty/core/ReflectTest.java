package com.kfyty.core;

import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/8/6 9:15
 * @email kfyty725@hotmail.com
 */
public class ReflectTest {
    private interface AAA {

        void test1();

        default void test2() {

        }
    }

    private abstract static class BBB {

        void test3() {

        }
    }

    private static class CCC extends BBB implements AAA {

        @Override
        public void test1() {

        }

        public void test3() {

        }

        public void test4() {

        }
    }

    @Test
    public void test() {
        ReflectUtil.getMethods(ReflectTest.class);
        long l = System.currentTimeMillis();
        Method[] methods = ReflectUtil.getMethods(CCC.class);
        System.out.println(System.currentTimeMillis() - l);
    }
}
