package com.kfyty.core;

import com.kfyty.loveqq.framework.core.generic.Generic;
import com.kfyty.loveqq.framework.core.generic.QualifierGeneric;
import com.kfyty.loveqq.framework.core.generic.SimpleGeneric;
import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 描述: 泛型测试
 *
 * @author kfyty725
 * @date 2021/6/24 17:58
 * @email kfyty725@hotmail.com
 */
public class QualifierGenericTest {

    public Entity t() {
        return null;
    }

    public Entity[] arrT() {
        return null;
    }

    public List<Entity> listT() {
        return null;
    }

    public Set<Entity> setT() {
        return null;
    }

    public Map<String, Object> map() {
        return null;
    }

    public Map<String, Entity> mapT() {
        return null;
    }

    public List<Map<String, Object>> mapList() {
        return null;
    }

    public <T> List<Map<T[], Map<List<T>[], byte[]>>> nested() {
        return null;
    }

    static class AAA<T> {}
    static class BBB extends AAA<QualifierGenericTest> {}

    public static class III<K, V> {
        Map<K, V[]> test;
    }

    public static class CCC<T> extends III<String, List<T>> {
        Value<T> valueUser;
    }

    public static class DDD extends CCC<QualifierGenericTest> {}

    @Test
    public void test1() throws Exception {
        SimpleGeneric bbb = SimpleGeneric.from(BBB.class);
        SimpleGeneric valueUser = SimpleGeneric.from(DDD.class, ReflectUtil.getField(DDD.class, "valueUser"));
        SimpleGeneric test = SimpleGeneric.from(DDD.class, ReflectUtil.getField(DDD.class, "test"));
        SimpleGeneric t = SimpleGeneric.from(QualifierGenericTest.class.getMethod("t"));
        SimpleGeneric arrT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("arrT"));
        SimpleGeneric listT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("listT"));
        SimpleGeneric setT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("setT"));
        SimpleGeneric map = SimpleGeneric.from(QualifierGenericTest.class.getMethod("map"));
        SimpleGeneric mapT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("mapT"));
        SimpleGeneric mapList = SimpleGeneric.from(QualifierGenericTest.class.getMethod("mapList"));
        QualifierGeneric nested = QualifierGeneric.from(QualifierGenericTest.class.getMethod("nested"));
        Assertions.assertEquals(valueUser.getGeneric().get(), QualifierGenericTest.class);
        Assertions.assertEquals(test.getFirst().get(), String.class);
        Assertions.assertTrue(test.getSecond().isArray());
        Assertions.assertEquals(test.getSecond().get(), List.class);
        Assertions.assertEquals(test.getNested(new Generic(List.class, true)).getGeneric().get(), QualifierGenericTest.class);
        Assertions.assertTrue(bbb.isSimpleGeneric());
        Assertions.assertEquals(bbb.getSimpleActualType(), QualifierGenericTest.class);
        Assertions.assertFalse(t.isSimpleGeneric());
        Assertions.assertTrue(arrT.isSimpleArray());
        Assertions.assertEquals(listT.getSimpleActualType(), Entity.class);
        Assertions.assertEquals(setT.getSimpleActualType(), Entity.class);
        Assertions.assertTrue(map.isMapGeneric() && map.getMapValueType().get().equals(Object.class));
        Assertions.assertTrue(mapT.isMapGeneric() && mapT.getMapValueType().get().equals(Entity.class));
        Assertions.assertEquals(mapList.getFirst().get(), Map.class);
        Assertions.assertEquals(mapList.getNested(new Generic(Map.class)).getFirst().get(), String.class);
    }

    @Test
    public void test2() {
        Field t = ReflectUtil.getField(DefaultController.class, "t");
        Field k = ReflectUtil.getField(DefaultController.class, "k");
        Field baseT = ReflectUtil.getField(DefaultController.class, "baseT");
        Field arrT = ReflectUtil.getField(DefaultController.class, "arrT");
        Field service = ReflectUtil.getField(DefaultController.class, "service");
        Field baseService = ReflectUtil.getField(DefaultController.class, "baseService");
        Field entityClass = ReflectUtil.getField(DefaultController.class, "entityClass");
        SimpleGeneric fromT = SimpleGeneric.from(DefaultController.class, t);
        SimpleGeneric fromK = SimpleGeneric.from(DefaultController.class, k);
        SimpleGeneric fromBaseT = SimpleGeneric.from(DefaultController.class, baseT);
        SimpleGeneric fromArrT = SimpleGeneric.from(DefaultController.class, arrT);
        SimpleGeneric fromService = SimpleGeneric.from(DefaultController.class, service);
        SimpleGeneric fromBaseService = SimpleGeneric.from(DefaultController.class, baseService);
        SimpleGeneric fromEntityClass = SimpleGeneric.from(DefaultController.class, entityClass);
        Assertions.assertTrue(fromT.isSimpleGeneric());
        Assertions.assertEquals(fromT.getGeneric().get(), Entity.class);
        Assertions.assertEquals(fromK.getGeneric().get(), Integer.class);
        Assertions.assertEquals(fromBaseT.getGeneric().get(), Entity.class);
        Assertions.assertTrue(fromArrT.getGeneric().get() == Entity.class && fromArrT.getGeneric().isArray());
        Assertions.assertEquals(fromService.getFirst().get(), Integer.class);
        Assertions.assertEquals(fromService.getSecond().get(), Entity.class);
        Assertions.assertEquals(fromBaseService.getFirst().get(), Integer.class);
        Assertions.assertEquals(fromBaseService.getSecond().get(), Entity.class);
        Assertions.assertEquals(fromEntityClass.getSimpleActualType(), Entity.class);
    }
}

class Entity {}

interface Base<K, T> {}

class BaseController<K, T> {
    protected T t;
    protected K k;
    protected T[] arrT;
    protected Base<K, T> service;
    protected Class<T> entityClass;
}

class DefaultBase<T> extends BaseController<Integer, T> {
    protected T baseT;
    protected Base<Integer, T> baseService;
}

class CommonBase extends DefaultBase<Entity> {}

class DefaultController extends CommonBase {}
