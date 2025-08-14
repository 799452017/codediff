package com.blackg.codediff;

import com.blackg.codediff.tree.DirectoryTree;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ComparisonResult {
    private final List<FileMatch> exactMatches = new ArrayList<>();  // 完全匹配的文件
    private final List<FileMatch> diffMatches = new ArrayList<>();   // 部分匹配的文件
    private final Set<FileData> unmatched1 = new HashSet<>();        // 工程1未匹配文件
    private final Set<FileData> unmatched2 = new HashSet<>();        // 工程2未匹配文件
    private double similarityScore = 0.0;                           // 整体相似度

    private DirectoryTree directoryTree1;
    private DirectoryTree directoryTree2;

    private long fileSize1 = 0L;
    private long fileSize2 = 0L;

    // 获取完全匹配文件数量
    public int getExactMatchCount() {
        return exactMatches.size();
    }

    // 获取部分匹配文件数量
    public int getDiffMatchCount() {
        return diffMatches.size();
    }

    // 获取工程1未匹配文件数量
    public int getUnmatchedCount1() {
        return unmatched1.size();
    }

    // 获取工程2未匹配文件数量
    public int getUnmatchedCount2() {
        return unmatched2.size();
    }

    // 获取所有匹配的文件对
    public List<FileMatch> getAllMatches() {
        List<FileMatch> all = new ArrayList<>(exactMatches);
        all.addAll(diffMatches);
        return all;
    }

    // 获取工程1所有文件
    public Set<FileData> getAllFiles1() {
        Set<FileData> all = new HashSet<>(unmatched1);
        exactMatches.forEach(m -> all.add(m.file1));
        diffMatches.forEach(m -> all.add(m.file1));
        return all;
    }

    // 获取工程2所有文件
    public Set<FileData> getAllFiles2() {
        Set<FileData> all = new HashSet<>(unmatched2);
        exactMatches.forEach(m -> all.add(m.file2));
        diffMatches.forEach(m -> all.add(m.file2));
        return all;
    }

    // 添加完全匹配
    public void addExactMatch(FileMatch match) {
        exactMatches.add(match);
    }

    // 添加差异匹配
    public void addDiffMatch(FileMatch match) {
        diffMatches.add(match);
    }

    // 添加工程1未匹配文件
    public void addUnmatched1(FileData file) {
        unmatched1.add(file);
    }

    // 添加工程2未匹配文件
    public void addUnmatched2(FileData file) {
        unmatched2.add(file);
    }

    //获取工程1代码行数
    public int getCodeCount1() {
        return getAllFiles1().stream().mapToInt(FileData::getLineCount).sum();
    }

    // 获取工程2代码行数
    public int getCodeCount2() {
        return getAllFiles2().stream().mapToInt(FileData::getLineCount).sum();
    }

}