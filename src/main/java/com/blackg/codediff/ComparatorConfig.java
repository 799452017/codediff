package com.blackg.codediff;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置类，用于自定义对比参数
 */
@Data
public class ComparatorConfig {
    private double similarityThreshold = 0.6;  // 相似度阈值
    private boolean ignoreWhitespace = true;  // 是否忽略空白字符
    private boolean ignoreCase = false;       // 是否忽略大小写
    private List<String> excludePatterns = new ArrayList<>(); // 排除文件模式
    private boolean ignoreComments = true; //是否忽略注释
    private boolean showTree = true;
    private boolean showOnlyDiff = true;
    private boolean showSideBySide = true;

    public ComparatorConfig() {
        excludePatterns.add("**/target/**");
        excludePatterns.add("**/.svn/**");
        excludePatterns.add("**/.idea/**");
        excludePatterns.add("**/.mvn/**");
        excludePatterns.add("**/.git/**");
        excludePatterns.add("**/__MACOSX/**");
        excludePatterns.add("**/**.svg");
        excludePatterns.add("node_modules/**");

    }

    public ComparatorConfig setSimilarityThreshold(double threshold) {
        this.similarityThreshold = threshold;
        return this;
    }

    public ComparatorConfig setIgnoreWhitespace(boolean ignore) {
        this.ignoreWhitespace = ignore;
        return this;
    }

    public ComparatorConfig setIgnoreCase(boolean ignore) {
        this.ignoreCase = ignore;
        return this;
    }

    public ComparatorConfig addExcludePattern(String pattern) {
        this.excludePatterns.add(pattern);
        return this;
    }

    public ComparatorConfig setShowTree(boolean showTree) {
        this.showTree = showTree;
        return this;
    }

    public ComparatorConfig setShowOnlyDiff(boolean showOnlyDiff) {
        this.showOnlyDiff = showOnlyDiff;
        return this;
    }

    public ComparatorConfig setShowSideBySide(boolean showSideBySide) {
        this.showSideBySide = showSideBySide;
        return this;
    }

    public ComparatorConfig setIgnoreComments(boolean ignoreComments) {
        this.ignoreComments = ignoreComments;
        return this;
    }
}