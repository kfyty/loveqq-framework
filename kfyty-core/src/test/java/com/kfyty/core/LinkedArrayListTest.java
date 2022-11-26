package com.kfyty.core;

import com.kfyty.core.lang.LinkedArrayList;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.IOUtil;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/11/19 10:08
 * @email kfyty725@hotmail.com
 */
public class LinkedArrayListTest {

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void test1() {
        List<Integer> list = new LinkedArrayList<>(3);
        list.add(1);
        list.remove(0);
        list.add(1);
        list.add(3);
        list.add(1, 2);
        list.add(0, 0);
        Assert.assertEquals(list, Arrays.asList(0, 1, 2, 3));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
        oos.writeObject(list);
        IOUtil.close(oos);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        List<Integer> o = (List<Integer>) ois.readObject();
        IOUtil.close(ois);
        Assert.assertEquals(o, Arrays.asList(0, 1, 2, 3));
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
        list.clear();
        list.addAll(Arrays.asList(1, 2));
        Assert.assertEquals(list, Arrays.asList(1, 2));
    }

    @Test
    public void test5() {
        int count = 3000;
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

    @Test
    @SneakyThrows
    public void performanceTest() {
        int retry = 100;
        int count = 10000;
        // Supplier<List<Integer>> list = () -> new LinkedList<>();
        // Supplier<List<Integer>> list = () -> new ArrayList<>(count / 200);
        Supplier<List<Integer>> list = () -> new LinkedArrayList<>(count / 200);
        CountDownLatch latch = new CountDownLatch(3);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.execute(this.addFirstPerformanceTest(retry, count, list, latch));
        executorService.execute(this.addLastPerformanceTest(retry, count, list, latch));
        executorService.execute(this.addRandomPerformanceTest(retry, count, list, latch));
        latch.await();
    }

    private Runnable addFirstPerformanceTest(int retry, int count, Supplier<List<Integer>> list, CountDownLatch latch) {
        return () -> {
            try {
                List<Long> time = new ArrayList<>();
                for (int i = 0; i < retry; i++) {
                    List<Integer> testList = list.get();
                    long start = System.currentTimeMillis();
                    for (int j = 0; j < count; j++) {
                        testList.add(0, j);
                    }
                    time.add(System.currentTimeMillis() - start);
                }
                System.out.println(CommonUtil.format("{}: addFirst:{} min:{} ms, max:{} ms, avg: {} ms",
                        list.get().getClass().getSimpleName(),
                        count,
                        time.stream().mapToLong(e -> e).min().getAsLong(),
                        time.stream().mapToLong(e -> e).max().getAsLong(),
                        time.stream().mapToLong(e -> e).average().getAsDouble()));
            } finally {
                latch.countDown();
            }
        };
    }

    private Runnable addLastPerformanceTest(int retry, int count, Supplier<List<Integer>> list, CountDownLatch latch) {
        return () -> {
            try {
                List<Long> time = new ArrayList<>();
                for (int i = 0; i < retry; i++) {
                    List<Integer> testList = list.get();
                    long start = System.currentTimeMillis();
                    for (int j = 0; j < count; j++) {
                        testList.add(j);
                    }
                    time.add(System.currentTimeMillis() - start);
                }
                System.out.println(CommonUtil.format("{}: addLast:{} min:{} ms, max:{} ms, avg: {} ms",
                        list.get().getClass().getSimpleName(),
                        count,
                        time.stream().mapToLong(e -> e).min().getAsLong(),
                        time.stream().mapToLong(e -> e).max().getAsLong(),
                        time.stream().mapToLong(e -> e).average().getAsDouble()));
            } finally {
                latch.countDown();
            }
        };
    }

    private Runnable addRandomPerformanceTest(int retry, int count, Supplier<List<Integer>> list, CountDownLatch latch) {
        return () -> {
            try {
                Random random = new Random();
                List<Long> time = new ArrayList<>();
                for (int i = 0; i < retry; i++) {
                    List<Integer> testList = list.get();
                    testList.add(0);
                    long start = System.currentTimeMillis();
                    for (int j = 0; j < count; j++) {
                        testList.add(random.nextInt(testList.size()), j);
                    }
                    time.add(System.currentTimeMillis() - start);
                }
                System.out.println(CommonUtil.format("{}: addRandom:{} min:{} ms, max:{} ms, avg: {} ms",
                        list.get().getClass().getSimpleName(),
                        count,
                        time.stream().mapToLong(e -> e).min().getAsLong(),
                        time.stream().mapToLong(e -> e).max().getAsLong(),
                        time.stream().mapToLong(e -> e).average().getAsDouble()));
            } finally {
                latch.countDown();
            }
        };
    }
}
