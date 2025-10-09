package com.blackg.codediff;

import lombok.SneakyThrows;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class HighConcurrencyOptimized {
    // 根据CPU核心数动态设置线程池大小
    private static final int THREAD_POOL_SIZE = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
    private static final ExecutorService executor = new ForkJoinPool(THREAD_POOL_SIZE);

    @SneakyThrows
    public static void main(String[] args) {
        // 模拟大规模数据集 (1万到100万元素)
        List<Integer> list = IntStream.rangeClosed(1, 100_000)
                .boxed()
                .collect(Collectors.toList());

        long startTime = System.nanoTime();

        // 1. 并行流处理 + 结果聚合
        ConcurrentLinkedQueue<Object> size = new ConcurrentLinkedQueue<>();
        List<Integer> results = executor.submit(() -> list.parallelStream()
                .map(a -> {
                    // 模拟实际业务处理
                    System.out.println(Thread.currentThread().getName());
                    int processed = processItem(a);
                    size.add(processed);
                    return processed;
                })
                .collect(Collectors.toList())).get();

        long endTime = System.nanoTime();

        System.out.println("处理结果数量: " + results.size());
        System.out.println("处理结果大小: " + size.size());
        System.out.printf("总耗时: %.3f ms%n", (endTime - startTime) / 1_000_000.0);

        // 优雅关闭线程池
        executor.shutdown();
    }

    private static int processItem(int item) {
        // 模拟实际处理逻辑
        return item * 2;
    }
}