package com.kfyty.core;

import com.kfyty.core.lang.LinkedArrayList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/11/19 10:08
 * @email kfyty725@hotmail.com
 */
public class LinkedArrayListTest {

    @Test
    public void test1() {
        List<Integer> list = new LinkedArrayList<>(3);
        list.add(1);
        list.add(3);
        list.add(1, 2);
        list.add(0, 0);
        Assert.assertEquals(list, Arrays.asList(0, 1, 2, 3));
    }

    @Test
    public void test2() {
        List<Integer> list = new LinkedArrayList<>(3);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(2, 5);
        list.add(5, 6);
        Assert.assertEquals(list, Arrays.asList(1, 2, 5, 3, 4, 6));
    }

    @Test
    public void test3() {
        List<Integer> list = new LinkedArrayList<>(3);
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.remove(3);
        list.remove(1);
        Assert.assertEquals(list, Arrays.asList(1, 3));
    }

    @Test
    public void test4() {
        List<Integer> list = new LinkedArrayList<>(3);
        list.add(0, 1);
        list.add(0, 2);
        list.add(0, 3);
        list.add(0, 4);
        list.add(0, 5);
        list.add(0, 6);
        list.add(0, 7);
        Assert.assertEquals(list, Arrays.asList(7, 6, 5, 4, 3, 2, 1));
    }

    @Test
    public void test5() {
        int count = 10000;
        Random random = new Random();
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new LinkedArrayList<>();
        list1.add(1);
        list2.add(1);
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(list1.size());
            int value = random.nextInt(count);
            list1.add(index, value);
            list2.add(index, value);
            Assert.assertEquals(list1, list2);
        }
        for (int i = 0; i < count - 10; i++) {
            int index = random.nextInt(list1.size());
            list1.remove(index);
            list2.remove(index);
            Assert.assertEquals(list1, list2);
        }
    }
}
