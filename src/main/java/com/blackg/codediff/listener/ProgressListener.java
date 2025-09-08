package com.blackg.codediff.listener;

public interface ProgressListener {

    void onMessage(String message);

    void onProgress(int currentPhase, int totalPhases, String phaseName, double progress);
    
    void onPhaseStart(int currentPhase, int totalPhases, String phaseName, int fileCount);
    
    void onPhaseComplete(int currentPhase, int totalPhases, String phaseName, int matchedFiles);
    
    void onFileProgress(String fileName, int currentFile, int totalFiles, double similarity);
}