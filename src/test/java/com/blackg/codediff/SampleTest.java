package com.blackg.codediff;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class SampleTest {

    @Test
    public void test() throws IOException, NoSuchAlgorithmException {
        ComparatorConfig comparatorConfig = new ComparatorConfig()
                .setIgnoreComments(true)
                .setIgnoreWhitespace(true)
                .setIgnoreCase(false)
                .setSimilarityThreshold(0.3);
        comparatorConfig.setIgnoreEmptyLines(true);

        SourceCodeComparator comparator = new SourceCodeComparator(comparatorConfig);
        File file1 = new File("C:\\Users\\Administrator\\Downloads\\JavaSecLab-main");
        File file2 = new File("C:\\Users\\Administrator\\Downloads\\java+py混合文件");

        ComparisonResult comparisonResult = comparator.compareProjects(file1.toPath(), file2.toPath());
        ResultReporter.report(comparisonResult, System.out, true, false, true, true);
    }

    @Test
    public void lineCommentRemover() {
        String removeComments = CommentRemover.removeComments("def process_sql_file(input_file, output_file):\n" +
                "    \"\"\"\n" +
                "    处理SQL文件并删除etl_date字段和值\n" +
                "    \"\"\"\n" +
                "    try:\n" +
                "        with open(input_file, 'r', encoding='utf-8') as f:\n" +
                "            sql_content = f.read()\n" +
                "\n" +
                "        processed_content = remove_etl_date(sql_content)\n" +
                "\n" +
                "        with open(output_file, 'w', encoding='utf-8') as f:\n" +
                "            f.write(processed_content)\n" +
                "\n" +
                "        print(f\"成功删除etl_date字段和值\")\n" +
                "        print(f\"结果已保存到: {output_file}\")\n" +
                "\n" +
                "    except Exception as e:\n" +
                "        print(f\"处理文件时出错: {e}\")", "delete_deta.py");

        System.out.println(removeComments);
    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        test2();
    }

    public static void test1() {
        List<Integer> list
                = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));

        ConcurrentLinkedQueue<Integer> size = new ConcurrentLinkedQueue<>();
        List<CompletableFuture<Void>> completableFutures = list.stream().map(a -> CompletableFuture.runAsync(() -> {
            if (StrUtil.isNotBlank(a.toString())) {
                size.add(a);
            }
        })).collect(Collectors.toList());

        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();

        System.out.println(size);
        System.out.println(size.size());
    }

    public static void test2() throws ExecutionException, InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = ThreadUtil.newExecutor(3);
        ForkJoinPool forkJoinPool = new ForkJoinPool(3);
        List<Integer> list
                = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));

        forkJoinPool.submit(() -> list.parallelStream()
                .forEach(a -> {
                    StrUtil.isNotBlank(a.toString());
                    System.out.println(Thread.currentThread().getName());
                })).get();

        List<CompletableFuture<Void>> completableFutures = list.stream().map(a -> CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName());
        }, forkJoinPool)).collect(Collectors.toList());

        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
    }
}
