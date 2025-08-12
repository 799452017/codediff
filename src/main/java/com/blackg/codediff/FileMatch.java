package com.blackg.codediff;

import com.blackg.codediff.enums.MatchType;

/**
 * 文件匹配结果
 */
public class FileMatch {
    final FileData file1;
    final FileData file2;
    final MatchType matchType;    // 匹配类型
    final double similarity;      // 相似度得分

    FileMatch(FileData file1, FileData file2, MatchType matchType, double similarity) {
        this.file1 = file1;
        this.file2 = file2;
        this.matchType = matchType;
        this.similarity = similarity;
    }
}