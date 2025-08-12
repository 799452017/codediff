package com.blackg.codediff;

import com.blackg.codediff.enums.MatchType;
import com.blackg.codediff.enums.NodeType;
import com.blackg.codediff.tree.DirectoryTree;
import com.blackg.codediff.tree.TreeNode;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 结果报告工具类
 */
public class ResultReporter {
    private static final int MAX_WIDTH = 100; // 控制台最大宽度

    public static void report(ComparisonResult result, PrintStream out, boolean showTree, boolean showOnlyDiff, boolean showSideBySide) {
        // 输出统计摘要
        out.println("\n============ 源码对比结果摘要 ============");
        out.printf("工程1文件总数: %d\n", result.getAllFiles1().size());
        out.printf("工程2文件总数: %d\n", result.getAllFiles2().size());
        out.println("----------------------------------------");
        out.printf("完全匹配文件数: %d\n", result.getExactMatchCount());
        out.printf("部分匹配文件数: %d\n", result.getDiffMatchCount());
        out.printf("工程1未匹配文件数: %d\n", result.getUnmatchedCount1());
        out.printf("工程2未匹配文件数: %d\n", result.getUnmatchedCount2());
        out.println("----------------------------------------");
        out.printf("整体相似度: %.2f%%\n", result.getSimilarityScore() * 100);

        // 输出完全匹配文件
        if (!result.getExactMatches().isEmpty()) {
            out.println("\n============ 完全匹配文件 ============");
            result.getExactMatches().forEach(match ->
                    out.printf("[MD5相同]%s <-> %s\n",
                            match.file1.relativePath, match.file2.relativePath));
        }

        // 输出部分匹配文件
        if (!result.getDiffMatches().isEmpty()) {
            out.println("\n============ 部分匹配文件 ============");
            result.getDiffMatches().forEach(match -> {
                String matchType = match.matchType.getDescription();
                out.printf("[%s] %s <-> %s (相似度: %.2f%%)\n",
                        matchType, match.file1.relativePath,
                        match.file2.relativePath, match.similarity * 100);
            });
        }

        // 输出未匹配文件
        if (!result.getUnmatched1().isEmpty()) {
            out.println("\n============ 工程1未匹配文件 ============");
            result.getUnmatched1().forEach(file ->
                    out.println(file.relativePath));
        }

        if (!result.getUnmatched2().isEmpty()) {
            out.println("\n============ 工程2未匹配文件 ============");
            result.getUnmatched2().forEach(file ->
                    out.println(file.relativePath));
        }

        // 添加树状结构展示
        if (result.getDirectoryTree1() != null && result.getDirectoryTree2() != null && showTree) {
            // 选项1：单独展示每个工程的结构
            printProjectTree(result.getDirectoryTree1(), out, "工程1", showOnlyDiff);
            printProjectTree(result.getDirectoryTree2(), out, "工程2", showOnlyDiff);

            if (showSideBySide) {
                // 选项2：并排对比展示
                printTreeComparison(
                        result.getDirectoryTree1(),
                        result.getDirectoryTree2(),
                        out,
                        showOnlyDiff
                );
            }
        }
    }

    private static void printTree(
            TreeNode node,
            String prefix,
            PrintStream out,
            boolean showOnlyDiff
    ) {
        if (node == null) return;

        // 确定节点显示符号
        String nodePrefix;
        String childPrefix;

        if (node.isRoot()) {
            nodePrefix = "";
            childPrefix = prefix;
        } else {
            boolean isLast = node.getParent() == null ||
                    node == node.getParent().getChildren().values().stream()
                            .reduce((a, b) -> b).orElse(null);

            nodePrefix = prefix + (isLast ? "└── " : "├── ");
            childPrefix = prefix + (isLast ? "    " : "│   ");
        }

        // 节点状态标识
        String statusSymbol = "";
        String statusColor = "";

        if (node.getType() == NodeType.FILE) {
            if (node.getFileMatch() != null) {
                switch (node.getFileMatch().matchType) {
                    case EXACT_MATCH:
                        statusSymbol = "✓ ";
                        statusColor = "\u001B[32m"; // 绿色
                        break;
                    case NAME_MATCH:
                    case CONTENT_MATCH:
                        statusSymbol = "Δ ";
                        statusColor = "\u001B[33m"; // 黄色
                        break;
                }
            } else {
                statusSymbol = "✗ ";
                statusColor = "\u001B[31m"; // 红色
            }
        }

        // 跳过不需要显示的节点
        boolean shouldDisplay = true;
        if (showOnlyDiff) {
            if (node.getType() == NodeType.DIRECTORY) {
                // 检查目录是否包含差异
                boolean hasDiff = false;
                for (TreeNode child : node.getChildren().values()) {
                    if (child.getType() == NodeType.FILE &&
                            (child.getFileMatch() == null ||
                                    child.getFileMatch().matchType != MatchType.EXACT_MATCH)) {
                        hasDiff = true;
                        break;
                    }
                }
                shouldDisplay = hasDiff;
            } else {
                // 文件：只显示非完全匹配的
                shouldDisplay = node.getFileMatch() == null ||
                        node.getFileMatch().matchType != MatchType.EXACT_MATCH;
            }
        }

        // 打印节点
        if (shouldDisplay) {
            String resetColor = "\u001B[0m";
            String nodeName = statusColor + statusSymbol + node.getName() + resetColor;

            if (node.getType() == NodeType.FILE && node.getFileMatch() != null) {
                double similarity = node.getFileMatch().similarity * 100;
                nodeName += String.format(" (%.1f%%)", similarity);
            }

            out.println(nodePrefix + nodeName);
        }

        // 递归打印子节点
        for (TreeNode child : node.getChildren().values()) {
            printTree(child, childPrefix, out, showOnlyDiff);
        }
    }

    /**
     * 打印工程树状结构
     */
    public static void printProjectTree(
            DirectoryTree tree,
            PrintStream out,
            String projectName,
            boolean showOnlyDiff
    ) {
        out.println("\n============ " + projectName + " 工程结构 ============");
        printTree(tree.getRoot(), "", out, showOnlyDiff);
    }

    public static String repeat(String str, int count) {
        if (count < 0) throw new IllegalArgumentException("Count must be non-negative");
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 打印并排的树状结构比较
     */
    public static void printTreeComparison(
            DirectoryTree tree1,
            DirectoryTree tree2,
            PrintStream out,
            boolean showOnlyDiff
    ) {
        out.println("\n============ 工程结构对比 ============");
        out.println("工程 1" + repeat(" ", 45) + "工程 2");
        out.println(repeat("-", MAX_WIDTH));
        out.flush();

        // 使用列表收集输出行
        List<String> leftLines = new ArrayList<>();
        List<String> rightLines = new ArrayList<>();

        // 分别生成两棵树的文本表示
        generateTreeLines(tree1.getRoot(), "", leftLines, showOnlyDiff);
        generateTreeLines(tree2.getRoot(), "", rightLines, showOnlyDiff);

        // 确定最大行数
        int maxLines = Math.max(leftLines.size(), rightLines.size());

        // 打印对齐的树状结构
        for (int i = 0; i < maxLines; i++) {
            String left = i < leftLines.size() ? leftLines.get(i) : "";
            String right = i < rightLines.size() ? rightLines.get(i) : "";

            // 控制最大宽度
            int leftWidth = Math.min(45, MAX_WIDTH - 45);
            String formattedLeft = String.format("%-" + leftWidth + "s", left);
            if (formattedLeft.length() > leftWidth) {
                formattedLeft = formattedLeft.substring(0, leftWidth - 3) + "...";
            }

            out.printf("%s  %s%n", formattedLeft, right);
        }
    }

    /**
     * 生成树状结构的文本行
     */
    private static void generateTreeLines(
            TreeNode node,
            String prefix,
            List<String> lines,
            boolean showOnlyDiff
    ) {
        if (node == null) return;

        // 检查是否需要显示此节点
        if (showOnlyDiff && shouldSkipNode(node, showOnlyDiff)) {
            return;
        }

        // 确定节点显示符号
        String nodeSymbol = "";
        if (!node.isRoot()) {
            boolean isLast = isLastChild(node.getParent(), node);
            nodeSymbol = isLast ? "└── " : "├── ";
        }

        // 节点状态标识
        String statusSymbol = getStatusSymbol(node);
        String colorCode = getStatusColor(node);
        String resetColor = "\u001B[0m";

        // 构建节点行
        String nodeName = node.getName();
        if (node.getType() == NodeType.FILE && node.getFileMatch() != null) {
            double similarity = node.getFileMatch().similarity * 100;
            nodeName += String.format(" (%.1f%%)", similarity);
        }

        // 添加节点行
        lines.add(prefix + nodeSymbol + colorCode + statusSymbol + nodeName + resetColor);

        // 获取排序后的子节点
        List<TreeNode> children = node.getSortedChildren();
        for (int i = 0; i < children.size(); i++) {
            TreeNode child = children.get(i);

            // 确定子节点前缀
            String childPrefix;
            if (node.isRoot()) {
                childPrefix = prefix;
            } else {
                boolean isLast = (i == children.size() - 1);
                childPrefix = prefix + (isLast ? "    " : "│   ");
            }

            generateTreeLines(child, childPrefix, lines, showOnlyDiff);
        }
    }

    /**
     * 检查是否应该跳过此节点（仅显示差异模式）
     */
    private static boolean shouldSkipNode(TreeNode node, boolean showOnlyDiff) {
        if (!showOnlyDiff) return false;

        if (node.getType() == NodeType.DIRECTORY) {
            // 检查目录是否包含差异
            boolean hasDiff = false;
            for (TreeNode child : node.getChildren().values()) {
                if (!shouldSkipNode(child, showOnlyDiff)) {
                    hasDiff = true;
                    break;
                }
            }
            return !hasDiff;
        } else {
            // 文件：只显示非完全匹配的
            return node.getFileMatch() != null &&
                    node.getFileMatch().matchType == MatchType.EXACT_MATCH;
        }
    }


    /**
     * 获取节点状态符号
     */
    private static String getStatusSymbol(TreeNode node) {
        if (node.getType() != NodeType.FILE) return "";

        if (node.getFileMatch() != null) {
            switch (node.getFileMatch().matchType) {
                case EXACT_MATCH:
                    return "✓ ";
                case NAME_MATCH:
                case CONTENT_MATCH:
                    return "Δ ";
            }
        }
        return "✗ ";
    }

    /**
     * 获取节点状态颜色
     */
    private static String getStatusColor(TreeNode node) {
        if (node.getType() != NodeType.FILE) return "";

        if (node.getFileMatch() != null) {
            switch (node.getFileMatch().matchType) {
                case EXACT_MATCH:
                    return "\u001B[32m"; // 绿色
                case NAME_MATCH:
                case CONTENT_MATCH:
                    return "\u001B[33m"; // 黄色
            }
        }
        return "\u001B[31m"; // 红色
    }

    private static void printTreesSideBySide(
            TreeNode node1,
            TreeNode node2,
            String prefix1,
            String prefix2,
            PrintStream out,
            boolean showOnlyDiff
    ) {
        // 确定节点显示
        String line1 = formatTreeNode(node1, prefix1, showOnlyDiff);
        String line2 = formatTreeNode(node2, prefix2, showOnlyDiff);

        if (line1 != null || line2 != null) {
            out.printf("%-50s%s%n",
                    line1 != null ? line1 : "",
                    line2 != null ? line2 : "");
        }

        // 收集子节点
        List<TreeNode> children1 = node1 != null ?
                new ArrayList<>(node1.getChildren().values()) : Collections.emptyList();
        List<TreeNode> children2 = node2 != null ?
                new ArrayList<>(node2.getChildren().values()) : Collections.emptyList();

        int maxChildren = Math.max(children1.size(), children2.size());

        for (int i = 0; i < maxChildren; i++) {
            TreeNode child1 = i < children1.size() ? children1.get(i) : null;
            TreeNode child2 = i < children2.size() ? children2.get(i) : null;

            // 确定前缀
            String childPrefix1 = prefix1 + (isLastChild(node1, i) ? "    " : "│   ");
            String childPrefix2 = prefix2 + (isLastChild(node2, i) ? "    " : "│   ");

            printTreesSideBySide(child1, child2, childPrefix1, childPrefix2, out, showOnlyDiff);
        }
    }

    private static String formatTreeNode(TreeNode node, String prefix, boolean showOnlyDiff) {
        if (node == null) return null;

        // 跳过不需要显示的节点
        if (showOnlyDiff) {
            if (node.getType() == NodeType.DIRECTORY) {
                boolean hasDiff = false;
                for (TreeNode child : node.getChildren().values()) {
                    if (child.getType() == NodeType.FILE &&
                            (child.getFileMatch() == null ||
                                    child.getFileMatch().matchType != MatchType.EXACT_MATCH)) {
                        hasDiff = true;
                        break;
                    }
                }
                if (!hasDiff) return null;
            } else {
                if (node.getFileMatch() != null &&
                        node.getFileMatch().matchType == MatchType.EXACT_MATCH) {
                    return null;
                }
            }
        }

        // 确定节点符号
        String nodeSymbol = "";
        if (!node.isRoot()) {
            boolean isLast = isLastChild(node.getParent(), node);
            nodeSymbol = isLast ? "└── " : "├── ";
        }

        // 节点状态标识
        String statusSymbol = "";
        String statusColor = "";

        if (node.getType() == NodeType.FILE) {
            if (node.getFileMatch() != null) {
                switch (node.getFileMatch().matchType) {
                    case EXACT_MATCH:
                        statusSymbol = "✓ ";
                        statusColor = "\u001B[32m"; // 绿色
                        break;
                    case NAME_MATCH:
                    case CONTENT_MATCH:
                        statusSymbol = "Δ ";
                        statusColor = "\u001B[33m"; // 黄色
                        break;
                }
            } else {
                statusSymbol = "✗ ";
                statusColor = "\u001B[31m"; // 红色
            }
        }

        String resetColor = "\u001B[0m";
        String nodeName = prefix + nodeSymbol + statusColor + statusSymbol + node.getName() + resetColor;

        // 添加相似度信息
        if (node.getType() == NodeType.FILE && node.getFileMatch() != null) {
            double similarity = node.getFileMatch().similarity * 100;
            nodeName += String.format(" (%.1f%%)", similarity);
        }

        return nodeName;
    }

    private static boolean isLastChild(TreeNode parent, int index) {
        if (parent == null) return true;
        int size = parent.getChildren().size();
        return index == size - 1;
    }

    /**
     * 判断是否是父节点的最后一个子节点
     */
    private static boolean isLastChild(TreeNode parent, TreeNode child) {
        if (parent == null) return true;
        List<TreeNode> children = new ArrayList<>(parent.getChildren().values());
        return !children.isEmpty() && children.get(children.size() - 1) == child;
    }

    // 在 DirectoryTree 类中添加

    /**
     * 获取节点的相对路径
     */
    public String getRelativePath(TreeNode node) {
        if (node.isRoot()) return "";

        StringBuilder path = new StringBuilder(node.getName());
        TreeNode current = node.getParent();

        while (current != null && !current.isRoot()) {
            path.insert(0, current.getName() + "/");
            current = current.getParent();
        }

        return path.toString();
    }

    /**
     * 获取节点深度
     */
    public int getNodeDepth(TreeNode node) {
        int depth = 0;
        TreeNode current = node;

        while (current != null && !current.isRoot()) {
            depth++;
            current = current.getParent();
        }

        return depth;
    }
}