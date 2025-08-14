package com.blackg.codediff;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("用法: java SourceCodeComparator <工程1路径> <工程2路径> [选项]");
            System.out.println("选项:");
            System.out.println("  -t <阈值>         设置相似度阈值 (0.0-1.0)");
            System.out.println("  -w                忽略空白字符");
            System.out.println("  -i                忽略大小写");
            System.out.println("  -e <模式>          排除文件模式 (通配符)");
            System.out.println("  -tree             显示树状结构");
            System.out.println("  -diff-only        只显示有差异的部分");
            System.out.println("  -side-by-side     并排对比显示（与-tree一起使用）");
            System.out.println("  -ignore-comments  忽略代码注解");
//            return;
        }

        // 解析命令行参数
        Path project1 = Paths.get(args[0]);
        Path project2 = Paths.get(args[1]);

        ComparatorConfig config = new ComparatorConfig();

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "-t":
                    if (i + 1 < args.length) {
                        config.setSimilarityThreshold(Double.parseDouble(args[++i]));
                    }
                    break;
                case "-w":
                    config.setIgnoreWhitespace(true);
                    break;
                case "-i":
                    config.setIgnoreCase(true);
                    break;
                case "-e":
                    if (i + 1 < args.length) {
                        config.addExcludePattern(args[++i]);
                    }
                    break;
                case "-tree":
                    config.setShowTree(true);
                    break;
                case "-diff-only":
                    config.setShowOnlyDiff(true);
                    break;
                case "-side-by-side":
                    config.setShowSideBySide(true);
                    break;
                case "-ignore-comments":
                    config.setIgnoreComments(true);
                    break;
            }
        }

        // 执行对比
        try {
            SourceCodeComparator comparator = new SourceCodeComparator(config);
            ComparisonResult result = comparator.compareProjects(project1, project2);

            // 输出报告
            ResultReporter.report(result, System.out, config.isShowTree(), config.isShowOnlyDiff(), config.isShowSideBySide());
        } catch (Exception e) {
            System.err.println("对比过程中出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}