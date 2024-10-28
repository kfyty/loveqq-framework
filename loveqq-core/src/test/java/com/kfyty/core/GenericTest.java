package com.kfyty.core;

import com.kfyty.loveqq.framework.core.event.GenericApplicationEvent;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/10/27 20:16
 * @email kfyty725@hotmail.com
 */
public class GenericTest {

    static class Generic<EventType> implements Serializable {}
    static class AAA<E, S> extends Generic<E> {}
    static class BBB<Data, Event> extends AAA<Event, Data> {}
    static class CCC extends BBB<String, GenericTest> {}

    public void test(BBB<Integer, GenericTest> test) {

    }

    public void test(AAA<GenericTest, Long> test) {

    }

    public void test(GenericApplicationEvent<GenericTest, String> event) {

    }

    @Test
    public void typeVariableTest() {
        Class<?> superGeneric = ReflectUtil.getSuperGeneric(CCC.class, 0, e -> e.getRawType() == AAA.class);
        Assertions.assertEquals(superGeneric, GenericTest.class);
    }

    @Test
    public void typeVariableTest2() throws NoSuchMethodException {
        Method method = GenericTest.class.getMethod("test", BBB.class);
        Parameter parameter = method.getParameters()[0];
        Class<?> superGeneric = ReflectUtil.getSuperGeneric(parameter.getType(), parameter.getParameterizedType(), 0, e -> e.getRawType() == AAA.class);
        Assertions.assertEquals(superGeneric, GenericTest.class);
    }

    @Test
    public void typeVariableTest3() throws NoSuchMethodException {
        Method method = GenericTest.class.getMethod("test", AAA.class);
        Parameter parameter = method.getParameters()[0];
        Class<?> superGeneric = ReflectUtil.getSuperGeneric(parameter.getType(), parameter.getParameterizedType(), 0, e -> e.getRawType() == AAA.class);
        Assertions.assertEquals(superGeneric, GenericTest.class);
    }

    @Test
    public void typeVariableTest4() throws NoSuchMethodException {
        Method method = GenericTest.class.getMethod("test", GenericApplicationEvent.class);
        Parameter parameter = method.getParameters()[0];
        Class<?> superGeneric = ReflectUtil.getSuperGeneric(parameter.getType(), parameter.getParameterizedType(), 0, e -> e.getRawType() == GenericApplicationEvent.class);
        Assertions.assertEquals(superGeneric, GenericTest.class);
    }
}
