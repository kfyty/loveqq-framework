package com.kfyty.core;

import com.kfyty.loveqq.framework.core.lang.util.LinkedArrayList;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/11/19 10:08
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class LinkedArrayListTest {

    @Test
    public void streamTest1() {
        List<Integer> list = new LinkedArrayList<>(3);
        list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        List<Integer> collect1 = list.stream().map(e -> e * 2).collect(Collectors.toList());
        List<Integer> collect2 = list.parallelStream().map(e -> e * 2).sorted().collect(Collectors.toList());
        Assertions.assertEquals(collect1, Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20));
        Assertions.assertEquals(collect2, Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20));
    }

    @Test
    public void streamTest2() {
        List<Integer> list = new LinkedArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        List<Integer> collect1 = list.stream().map(e -> e * 2).collect(Collectors.toList());
        List<Integer> collect2 = list.parallelStream().map(e -> e * 2).sorted().collect(Collectors.toList());
        Assertions.assertEquals(collect1, Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20));
        Assertions.assertEquals(collect2, Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20));
    }

    @Test
    public void streamTest3() {
        List<Integer> list = new LinkedArrayList<>(1);
        list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        List<Integer> collect1 = list.stream().map(e -> e * 2).collect(Collectors.toList());
        List<Integer> collect2 = list.parallelStream().map(e -> e * 2).sorted().collect(Collectors.toList());
        Assertions.assertEquals(collect1, Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20));
        Assertions.assertEquals(collect2, Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20));
    }

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
        Assertions.assertEquals(list, Arrays.asList(0, 1, 2, 3));
        Assertions.assertArrayEquals(list.toArray(Integer[]::new), new Integer[]{0, 1, 2, 3});

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
        oos.writeObject(list);
        IOUtil.close(oos);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        List<Integer> o = (List<Integer>) ois.readObject();
        IOUtil.close(ois);
        Assertions.assertEquals(o, Arrays.asList(0, 1, 2, 3));
        Assertions.assertEquals(((LinkedArrayList<?>) list).clone(), Arrays.asList(0, 1, 2, 3));
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
        Assertions.assertEquals(list, Arrays.asList(1, 2, 5, 3, 4, 6));
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
        Assertions.assertEquals(list, Arrays.asList(1, 3));
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
        Assertions.assertEquals(list, Arrays.asList(7, 6, 5, 4, 3, 2, 1));
        list.clear();
        list.addAll(Arrays.asList(1, 2));
        Assertions.assertEquals(list, Arrays.asList(1, 2));
    }

    @Test
    public void test51() {
        List<Integer> list = new LinkedArrayList<>(5);
        list.add(1);
        list.add(2);
        list.add(10);
        list.addAll(2, Arrays.asList(3, 4, 5, 6, 7, 8, 9));
        Assertions.assertEquals(list, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void test52() {
        List<Integer> list = new LinkedArrayList<>(5);
        list.add(9);
        list.add(10);
        list.add(11);
        list.addAll(0, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        Assertions.assertEquals(list, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
    }

    @Test
    public void test53() {
        List<Integer> list = new LinkedArrayList<>(3);
        list.add(9);
        list.add(10);
        list.add(11);
        list.addAll(0, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        Assertions.assertEquals(list, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
    }

    @Test
    public void test54() {
        List<Integer> list = new LinkedArrayList<>(1);
        list.add(9);
        list.add(10);
        list.add(11);
        list.addAll(0, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        Assertions.assertEquals(list, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
    }

    @Test
    public void test55() {
        List<Integer> list = new LinkedArrayList<>(2);
        list.add(9);
        list.add(10);
        list.add(11);
        list.addAll(0, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        Assertions.assertEquals(list, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
    }

    @Test
    public void test56() {
        List<Integer> list = new LinkedArrayList<>(3);
        list.add(0);
        list.add(1);
        list.addAll(Arrays.asList(2, 3, 4, 5));
        for (int i = 0; i < list.size(); i++) {
            Assertions.assertEquals(i, list.get(i));
        }
    }

    @Test
    public void test57() {
        List<Integer> list = new LinkedArrayList<>(3);
        list.add(0);
        list.add(1);
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);
        list.add(8);
        list.add(9);
        list.addAll(2, Arrays.asList(2, 3));
        for (int i = 0; i < list.size(); i++) {
            Assertions.assertEquals(i, list.get(i));
        }
    }

    @Test
    public void test6() {
        int count = 10000;
        Random random = new Random();
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new LinkedArrayList<>();
        list1.add(1);
        list2.add(1);
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(list1.size());
            int value = random.nextInt(count);
            if (value % 2 == 0) {
                list1.add(index, value);
                list2.add(index, value);
            } else {
                list1.addAll(index, Collections.singletonList(value));
                list2.addAll(index, Collections.singletonList(value));
            }
            Assertions.assertEquals(list2, list1);
            Assertions.assertArrayEquals(list2.toArray(new Integer[0]), list1.toArray(new Integer[0]));
        }
        for (int i = 0; i < count - 10; i++) {
            int index = random.nextInt(list1.size());
            list1.remove(index);
            list2.remove(index);
            Assertions.assertEquals(list2, list1);
            Assertions.assertArrayEquals(list2.toArray(new Integer[0]), list1.toArray(new Integer[0]));
        }
        log.info("Correctness testing complete");
    }

    @Test
    @SneakyThrows
    public void performanceTest() {
        int retry = 100;
        int count = 10000;
//         Supplier<List<Integer>> list = () -> new LinkedList<>();
//         Supplier<List<Integer>> list = () -> new ArrayList<>(count / 200);
        Supplier<List<Integer>> list = () -> new LinkedArrayList<>(count / 200);
        CountDownLatch latch = new CountDownLatch(3);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.execute(this.addFirstPerformanceTest(retry, count, list, latch));
        executorService.execute(this.addLastPerformanceTest(retry, count, list, latch));
        executorService.execute(this.addRandomPerformanceTest(retry, count, list, latch));
        latch.await();
        executorService.shutdown();
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
