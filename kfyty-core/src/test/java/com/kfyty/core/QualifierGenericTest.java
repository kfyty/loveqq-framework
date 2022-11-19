package com.kfyty.core;

import com.kfyty.core.generic.ActualGeneric;
import com.kfyty.core.generic.QualifierGeneric;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.utils.ReflectUtil;
import org.junit.Assert;
import org.junit.Test;

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

    @Test
    public void test1() throws Exception {
        SimpleGeneric t = SimpleGeneric.from(QualifierGenericTest.class.getMethod("t"));
        SimpleGeneric arrT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("arrT"));
        SimpleGeneric listT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("listT"));
        SimpleGeneric setT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("setT"));
        SimpleGeneric map = SimpleGeneric.from(QualifierGenericTest.class.getMethod("map"));
        SimpleGeneric mapT = SimpleGeneric.from(QualifierGenericTest.class.getMethod("mapT"));
        SimpleGeneric mapList = SimpleGeneric.from(QualifierGenericTest.class.getMethod("mapList"));
        QualifierGeneric nested = QualifierGeneric.from(QualifierGenericTest.class.getMethod("nested"));
        Assert.assertFalse(t.isSimpleGeneric());
        Assert.assertTrue(arrT.isSimpleArray());
        Assert.assertTrue(List.class.isAssignableFrom(listT.getSourceType()) && !Map.class.isAssignableFrom(listT.getFirst().get()));
        Assert.assertTrue(Set.class.isAssignableFrom(setT.getSourceType()));
        Assert.assertTrue(map.isMapGeneric() && map.getMapValueType().get().equals(Object.class));
        Assert.assertTrue(mapT.isMapGeneric() && mapT.getMapValueType().get().equals(Entity.class));
        Assert.assertTrue(List.class.isAssignableFrom(mapList.getSourceType()) && mapList.getFirst().get().equals(Map.class));
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
        ActualGeneric fromT = ActualGeneric.from(DefaultController.class, t);
        ActualGeneric fromK = ActualGeneric.from(DefaultController.class, k);
        ActualGeneric fromBaseT = ActualGeneric.from(DefaultController.class, baseT);
        ActualGeneric fromArrT = ActualGeneric.from(DefaultController.class, arrT);
        ActualGeneric fromService = ActualGeneric.from(DefaultController.class, service);
        ActualGeneric fromBaseService = ActualGeneric.from(DefaultController.class, baseService);
        ActualGeneric fromEntityClass = ActualGeneric.from(DefaultController.class, entityClass);
        Assert.assertFalse(fromT.isSimpleGeneric());
        Assert.assertEquals(fromT.getSourceType(), Entity.class);
        Assert.assertEquals(fromK.getSourceType(), Integer.class);
        Assert.assertEquals(fromBaseT.getSourceType(), Entity.class);
        Assert.assertEquals(fromArrT.getSourceType(), Entity[].class);
        Assert.assertEquals(fromArrT.getFirst().get(), Entity.class);
        Assert.assertEquals(fromService.getFirst().get(), Integer.class);
        Assert.assertEquals(fromService.getSecond().get(), Entity.class);
        Assert.assertEquals(fromBaseService.getFirst().get(), Integer.class);
        Assert.assertEquals(fromBaseService.getSecond().get(), Entity.class);
        Assert.assertEquals(fromEntityClass.getSimpleActualType(), Entity.class);
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
