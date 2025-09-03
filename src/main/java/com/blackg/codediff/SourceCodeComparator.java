package com.blackg.codediff;

import com.alibaba.fastjson.JSONObject;
import com.blackg.codediff.enums.MatchType;
import com.blackg.codediff.tree.DirectoryTree;
import com.blackg.codediff.tree.TreeNode;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 源代码对比工具类，用于分析两个源码工程之间的相似性
 */
@Data
public class SourceCodeComparator {

    // 配置参数
    private final ComparatorConfig config;

    // 对比结果
    private final ComparisonResult result = new ComparisonResult();

    /**
     * 构造函数
     *
     * @param config 对比配置
     */
    public SourceCodeComparator(ComparatorConfig config) {
        this.config = config;
    }

    /**
     * 默认配置的构造函数
     */
    public SourceCodeComparator() {
        this(new ComparatorConfig());
    }

    /**
     * 构建工程目录树
     */
    public static DirectoryTree buildDirectoryTree(Path projectPath, List<FileData> files) {
        DirectoryTree tree = new DirectoryTree(projectPath);
        for (FileData file : files) {
            tree.addFile(file);
        }
        return tree;
    }

    /**
     * 将文件匹配结果应用到目录树
     */
    private void applyMatchesToTrees(
            DirectoryTree tree1,
            DirectoryTree tree2,
            ComparisonResult result
    ) {
        // 应用完全匹配
        for (FileMatch match : result.getExactMatches()) {
            TreeNode node1 = findFileNode(tree1, match.file1.relativePath);
            TreeNode node2 = findFileNode(tree2, match.file2.relativePath);

            if (node1 != null && node2 != null) {
                node1.setFileMatch(match);
                node2.setFileMatch(match);
            }
        }

        // 应用差异匹配
        for (FileMatch match : result.getDiffMatches()) {
            TreeNode node1 = findFileNode(tree1, match.file1.relativePath);
            TreeNode node2 = findFileNode(tree2, match.file2.relativePath);

            if (node1 != null && node2 != null) {
                node1.setFileMatch(match);
                node2.setFileMatch(match);
            }
        }
    }

    /**
     * 在树中查找文件节点
     */
    private TreeNode findFileNode(DirectoryTree tree, String relativePath) {
        Path path = tree.getBasePath().resolve(relativePath);
        Path relative = tree.getBasePath().relativize(path);

        TreeNode current = tree.getRoot();
        for (Path component : relative) {
            current = current.getChild(component.toString());
            if (current == null) return null;
        }

        return current;
    }

    public ComparisonResult compareProjects(List<FileData> files1, List<FileData> files2) {
        Path basePath1 = files1.get(0).getBasePath();
        Path basePath2 = files2.get(0).getBasePath();
        //获取两个工程的大小
        long fileSize1 = basePath1.toFile().length();
        long fileSize2 = basePath2.toFile().length();
        result.setFileSize1(fileSize1);
        result.setFileSize2(fileSize2);
        //获取两个工程文件名称
        result.setFileName1(basePath1.getFileName().toString());
        result.setFileName2(basePath2.getFileName().toString());
        // 初始化未匹配文件集合
        result.getUnmatched1().addAll(files1);
        result.getUnmatched2().addAll(files2);

        // 执行多阶段匹配
        matchByMD5();
        matchByNameAndContent();
        matchByContentSimilarity();

        // 计算整体相似度
        calculateOverallSimilarity();

        // 构建目录树
        if (config.isShowTree()) {
            DirectoryTree tree1 = buildDirectoryTree(basePath1, files1);
            DirectoryTree tree2 = buildDirectoryTree(basePath2, files2);

            // 应用匹配结果到树
            applyMatchesToTrees(tree1, tree2, result);

            // 将树添加到结果
            result.setDirectoryTree1(tree1);
            result.setDirectoryTree2(tree2);
        }
        return result;
    }

    /**
     * 执行源码工程对比
     *
     * @param project1Path 工程1路径
     * @param project2Path 工程2路径
     * @return 对比结果
     */
    public ComparisonResult compareProjects(Path project1Path, Path project2Path) {
        // 收集两个工程的所有文件
        List<FileData> files1 = collectFiles(project1Path);
        List<FileData> files2 = collectFiles(project2Path);

        return compareProjects(files1, files2);
    }

    /**
     * 第一阶段匹配：通过MD5值匹配完全相同的文件
     */
    private void matchByMD5() {
        // 创建MD5到文件的映射
        //根据md5值分组
        Set<FileData> files1 = result.getUnmatched1();
        Set<FileData> files2 = result.getUnmatched2();
        Map<String, FileData> md5Map1 = createMD5Map(files1);
        Map<String, FileData> md5Map2 = createMD5Map(files2);

        // 遍历所有MD5值，寻找匹配项
        for (Map.Entry<String, FileData> entry : md5Map1.entrySet()) {
            String md5 = entry.getKey();
            FileData file1 = entry.getValue();

            if (md5Map2.containsKey(md5)) {
                FileData file2 = md5Map2.get(md5);

                // 创建匹配记录
                FileMatch match = new FileMatch(file1, file2, MatchType.EXACT_MATCH, 1.0);
                result.addExactMatch(match);

                // 从未匹配集合中移除
                files1.remove(file1);
                result.getUnmatched2().remove(file2);
            }
        }
    }

    /**
     * 第二阶段匹配：匹配同名文件并计算相似度
     */
    private void matchByNameAndContent() {
        // 创建相对路径到文件的映射
        Map<String, FileData> pathMap2 = result.getUnmatched2().stream()
                .collect(Collectors.toMap(f -> f.relativePath, f -> f));

        // 遍历工程1未匹配文件
        for (Iterator<FileData> it = result.getUnmatched1().iterator(); it.hasNext(); ) {
            FileData file1 = it.next();
            FileData file2 = pathMap2.get(file1.relativePath);

            if (file2 != null && result.getUnmatched2().contains(file2)) {
                // 计算相似度
                double similarity = calculateSimilarity(file1, file2);

                if (similarity >= config.getSimilarityThreshold()) { // MD5不同所以一定<1
                    // 创建匹配记录
                    FileMatch match = new FileMatch(file1, file2, MatchType.NAME_MATCH, similarity);
                    result.addDiffMatch(match);

                    // 从未匹配集合中移除
                    it.remove();
                    result.getUnmatched2().remove(file2);
                }
            }
        }
    }

    /**
     * 第三阶段匹配：通过内容相似度匹配文件
     */
    private void matchByContentSimilarity() {
        // 为未匹配文件创建索引
        List<FileData> unmatched1 = new ArrayList<>(result.getUnmatched1());
        List<FileData> unmatched2 = new ArrayList<>(result.getUnmatched2());

        // 遍历所有可能的文件对
        for (Iterator<FileData> it1 = unmatched1.iterator(); it1.hasNext(); ) {
            FileData file1 = it1.next();
            FileMatch bestMatch = null;

            for (Iterator<FileData> it2 = unmatched2.iterator(); it2.hasNext(); ) {
                FileData file2 = it2.next();

                // 计算相似度
                double similarity = calculateSimilarity(file1, file2);

                if (similarity >= config.getSimilarityThreshold()) {
                    bestMatch = new FileMatch(file1, file2, MatchType.CONTENT_MATCH, similarity);
                }
            }

            // 处理找到的最佳匹配
            if (bestMatch != null) {
                result.addDiffMatch(bestMatch);
                it1.remove();
                unmatched2.remove(bestMatch.file2);
                result.getUnmatched1().remove(bestMatch.file1);
                result.getUnmatched2().remove(bestMatch.file2);
            }
        }
    }

    /**
     * 计算整体相似度得分
     */
    private void calculateOverallSimilarity() {
        int identicalFiles = result.getExactMatches().size();
        double[] similarFilesScores = result.getDiffMatches().stream()
                .mapToDouble(match -> match.similarity)
                .toArray();
        int uniqueFilesA = result.getUnmatched1().size();
        int uniqueFilesB = result.getUnmatched2().size();
        double calculateOverallSimilarity = calculateOverallSimilarity(identicalFiles, similarFilesScores, uniqueFilesA, uniqueFilesB);
        result.setSimilarityScore(calculateOverallSimilarity);
    }

    /**
     * 计算两个工程之间的相似度
     * 相似度计算说明
     * 您的两个工程文件相似度通过以下规则计算：
     * <p>
     * 匹配文件量化
     * <p>
     * 完全相同的文件：每个计 1分
     * <p>
     * 高度相似的文件：按实际相似度比例计分（0.0-1.0）
     * → 得到 相似文件总得分
     * <p>
     * 工程规模基准
     * <p>
     * 工程A总文件量 = 相似文件数 + A独有文件数
     * <p>
     * 工程B总文件量 = 相似文件数 + B独有文件数
     * → 取两工程文件量的平均值作为基准
     * <p>
     * 最终相似度
     * = 相似文件总得分 ÷ 平均文件量
     * （结果以百分比形式呈现）
     *
     * @param identicalFiles     完全相同的文件数量
     * @param similarFilesScores 部分相似文件的相似度数组
     * @param uniqueFilesA       A工程的独有文件数量
     * @param uniqueFilesB       B工程的独有文件数量
     * @return 两个工程的整体相似度（0.0 - 1.0）
     */
    public static double calculateOverallSimilarity(
            int identicalFiles,
            double[] similarFilesScores,
            int uniqueFilesA,
            int uniqueFilesB
    ) {
        // 1. 计算相似文件总得分
        double totalSimilarityScore = identicalFiles;  // 每个完全相同的文件得分为1

        // 添加部分相似文件的得分
        for (double score : similarFilesScores) {
            totalSimilarityScore += score;
        }

        // 2. 计算每个工程的总文件量
        int totalFilesA = identicalFiles + similarFilesScores.length + uniqueFilesA;
        int totalFilesB = identicalFiles + similarFilesScores.length + uniqueFilesB;

        // 3. 计算平均文件量
        double averageFileCount = (totalFilesA + totalFilesB) / 2.0;

        // 4. 处理边界情况（空工程）
        if (averageFileCount == 0) {
            return 1.0; // 两个空工程视为100%相似
        }

        // 5. 计算最终相似度
        return totalSimilarityScore / averageFileCount;
    }

    /**
     * 收集工程目录下的所有文件
     */
    @SneakyThrows
    public List<FileData> collectFiles(Path projectPath) {
        List<FileData> files = new ArrayList<>();

        // 递归遍历目录
        try (Stream<Path> stream = Files.walk(projectPath)) {
            stream.filter(Files::isRegularFile)
                    .filter(file -> !isExcluded(file, projectPath))
                    .forEach(file -> {
                        try {
                            files.add(processFile(file, projectPath));
                        } catch (Exception e) {
                            System.err.println("处理文件出错: " + file + " - " + e.getMessage());
                        }
                    });
        }

        return files;
    }

    private static final org.springframework.util.AntPathMatcher pathMatcher = new org.springframework.util.AntPathMatcher();

    /**
     * 检查文件是否在排除模式中
     */
    private boolean isExcluded(Path file, Path projectPath) {
        String relativePath = projectPath.relativize(file).toString().replace('\\', '/');
        return config.getExcludePatterns().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern.replace('\\', '/'), relativePath));
    }

    /**
     * 处理单个文件：计算MD5并读取内容
     */
    private FileData processFile(Path file, Path projectPath) throws IOException, NoSuchAlgorithmException {
        // 计算相对路径
        String relativePath = projectPath.relativize(file).toString();

        // 读取文件内容
        byte[] content = Files.readAllBytes(file);

        // 计算MD5
        String md5 = calculateMD5(content);

        List<String> lines = new ArrayList<>();

        //判断文件是否是二进制文件
        boolean isBinary = !isTextFile(file);

        // 只有文本文件才处理行内容
        if (!isBinary) {
            // 处理文件内容
            String contentStr = new String(content, StandardCharsets.UTF_8);
            // 如果配置了排除注释，则进行注释处理
            if (config.isIgnoreComments()) {
                contentStr = CommentRemover.removeComments(contentStr, file.getFileName().toString());
            }
            lines = Arrays.asList(contentStr.split("\\R"));

            // 应用预处理（如果需要）
            if (config.isIgnoreWhitespace() || config.isIgnoreCase()) {
                lines = preprocessLines(lines);
            }
        }

        File file1 = file.toFile();
        return new FileData(projectPath, file, relativePath, md5, lines, isBinary, file1.length(), file.getParent());
    }

    public static boolean isTextFile(Path filePath) throws IOException {
        // 1. 空文件视为文本
        if (Files.size(filePath) == 0) return true;

        // 2. 仅读取前1024字节（兼顾性能）
        byte[] buffer = Files.readAllBytes(filePath);
        int bytesRead = Math.min(buffer.length, 1024);

        int suspiciousBytes = 0;
        boolean hasNullByte = false;

        for (int i = 0; i < bytesRead; i++) {
            int unsignedByte = buffer[i] & 0xFF; // 关键修复：转为无符号整数(0-255)

            // 规则1：存在空字节 → 二进制（除UTF-16 BOM外，但文本文件不用UTF-16）
            if (unsignedByte == 0x00) {
                hasNullByte = true;
                // 提前终止（空字节在文本中极其罕见）
                if (i > 0) return false;
            }

            // 规则2：统计非常规控制字符（仅允许\t,\n,\r）
            if (unsignedByte < 0x09 ||
                    (unsignedByte > 0x0D && unsignedByte < 0x20)) {
                suspiciousBytes++;
            }
        }

        // 规则3：控制字符比例 > 10% → 二进制（放宽阈值适应特殊文本）
        double suspiciousRatio = (double) suspiciousBytes / bytesRead;

        // 终极判定逻辑
        return !hasNullByte && suspiciousRatio <= 0.1;
    }

    /**
     * 行内容预处理
     */
    private List<String> preprocessLines(List<String> lines) {
        List<String> processedLines = lines.stream()
                .map(line -> {
                    String processed = line;
                    if (config.isIgnoreWhitespace()) {
                        processed = processed.replaceAll("\\s+", "");
                    }
                    if (config.isIgnoreCase()) {
                        processed = processed.toLowerCase();
                    }
                    return processed;
                })
                .collect(Collectors.toList());

        return processedLines;
    }

    /**
     * 计算MD5值
     */
    private String calculateMD5(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 创建MD5到文件的映射
     */
    private Map<String, FileData> createMD5Map(Set<FileData> files) {
        return files.stream()
                .collect(Collectors.toMap(
                        f -> f.md5,
                        f -> f,
                        (existing, replacement) -> existing
                ));
    }


    /**
     * 计算两个文件内容之间的相似度
     * 使用基于最长公共子序列（LCS）的相似度算法
     */
    private double calculateSimilarity(FileData fileData1, FileData fileData2) {
        if (fileData1.isBinary && fileData2.isBinary) {
            if (fileData1.md5.equals(fileData2.md5)) {
                //如果都是二进制文件，并且md5相同
                return 1.0;
            }
            return 0.0;
        }

        if (fileData1.isBinary || fileData2.isBinary) {
            //如果有一个文件是二进制文件，则直接返回0.0
            return 0.0;
        }

        List<String> lines1 = fileData1.lines;
        List<String> lines2 = fileData2.lines;
        int m = lines1.size();
        int n = lines2.size();

        // 处理空文件情况
        if (m == 0 && n == 0) {
            return 1.0;
        }
        if (m == 0 || n == 0) return 0.0;

        // 创建DP表计算LCS
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (lines1.get(i - 1).equals(lines2.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        int lcs = dp[m][n];

        // 使用Dice系数计算相似度：2 * |A∩B| / (|A| + |B|)
        return (2.0 * lcs) / (m + n);
    }

    public static void main(String[] args) {
        SourceCodeComparator comparator = new SourceCodeComparator();
        ComparisonResult result = comparator.compareProjects(Paths.get("/Users/chenwenzhe/git/codediff")
                , Paths.get("/Users/chenwenzhe/git/easy-ai"));

        System.out.println(JSONObject.toJSONString(result.getDirectoryTree1()));
    }

}