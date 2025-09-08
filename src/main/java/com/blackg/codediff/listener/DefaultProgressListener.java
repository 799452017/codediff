package com.blackg.codediff.listener;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class DefaultProgressListener implements ProgressListener {
    private long startTime;
    private Map<Integer, Long> phaseStartTimes = new HashMap<>();
    private int currentPhase = 0;
    private String currentPhaseName = "";
    private int currentPhaseFiles = 0;
    private int currentFile = 0;
    private int totalFiles = 0;

    private boolean printConsole = true;

    @Override
    public void onMessage(String message) {
        if (printConsole) {
            System.out.print(message);
        }
    }

    @Override
    public void onProgress(int currentPhase, int totalPhases, String phaseName, double progress) {
        // 主进度条更新
        String format = String.format("\n=== 阶段 %d/%d: %s - %.1f%% ===\n",
                currentPhase, totalPhases, phaseName, progress * 100);
        onMessage(format);
    }

    @Override
    public void onPhaseStart(int currentPhase, int totalPhases, String phaseName, int fileCount) {
        this.currentPhase = currentPhase;
        this.currentPhaseName = phaseName;
        this.currentPhaseFiles = fileCount;
        this.totalFiles = fileCount;
        this.currentFile = 0;
        phaseStartTimes.put(currentPhase, System.currentTimeMillis());

        String format = String.format("\n=== 阶段 %d/%d: %s (%d 个文件) ===\n",
                currentPhase, totalPhases, phaseName, fileCount);
        onMessage(format);
    }

    @Override
    public void onPhaseComplete(int currentPhase, int totalPhases, String phaseName, int matchedFiles) {
        long endTime = System.currentTimeMillis();
        long startTime = phaseStartTimes.getOrDefault(currentPhase, endTime);
        double duration = (endTime - startTime) / 1000.0;

        String format = String.format(">>> 阶段完成: %s | 耗时: %.2fs | 匹配文件: %d\n",
                phaseName, duration, matchedFiles);
        onMessage(format);
    }

    @Override
    public void onFileProgress(String fileName, int currentFile, int totalFiles, double similarity) {
        this.currentFile = currentFile;
        this.totalFiles = totalFiles;

        String shortName = fileName;
//        if (fileName.length() > 40) {
//            shortName = "..." + fileName.substring(fileName.length() - 37);
//        }

        // 进度百分比
        double progress = totalFiles > 0 ? (currentFile * 100.0) / totalFiles : 0;

        // JDK 8 兼容的进度条实现
        int barWidth = 50;
        int progressBars = (int) (progress * barWidth / 100);

        StringBuilder progressBarBuilder = new StringBuilder();
        progressBarBuilder.append("[");

        // 添加已完成部分
        for (int i = 0; i < progressBars; i++) {
            progressBarBuilder.append("=");
        }

        // 添加未完成部分
        for (int i = progressBars; i < barWidth; i++) {
            progressBarBuilder.append(" ");
        }

        progressBarBuilder.append("]");
        String progressBar = progressBarBuilder.toString();

        String format = String.format("\r%s %6.2f%% | %3d/%3d | %s | 相似度: %.1f%%",
                progressBar, progress, currentFile, totalFiles,
                shortName, similarity * 100);
        onMessage(format);

        // 每完成10个文件或最后一个文件时换行
        if (currentFile % 10 == 0 || currentFile == totalFiles) {
            onMessage("\n");
        }
    }
}