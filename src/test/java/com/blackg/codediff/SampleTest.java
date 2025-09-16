package com.blackg.codediff;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class SampleTest {

    @Test
    public void test() throws IOException, NoSuchAlgorithmException {
        ComparatorConfig comparatorConfig = new ComparatorConfig()
                .setIgnoreComments(true)
                .setIgnoreWhitespace(false)
                .setIgnoreCase(false)
                .setSimilarityThreshold(0.3);
        comparatorConfig.setIgnoreEmptyLines(true);

        SourceCodeComparator comparator = new SourceCodeComparator(comparatorConfig);
        File file1 = new File("C:\\Users\\Administrator\\Downloads\\空行测试文件1");
        File file2 = new File("C:\\Users\\Administrator\\Downloads\\空行测试文件2");

        ComparisonResult comparisonResult = comparator.compareProjects(file1.toPath(), file2.toPath());
        ResultReporter.report(comparisonResult, System.out, true, false, true, true);
    }

    @Test
    public void lineCommentRemover(){
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
}
