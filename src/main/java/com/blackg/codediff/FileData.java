package com.blackg.codediff;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;
import java.util.List;

/**
 * 文件数据容器，存储文件元信息和内容
 */
@Data
@SuperBuilder
@NoArgsConstructor(force = true)
public class FileData {
    String id;
    String fileName;
    final Path basePath;
    final Path filePath;          // 文件绝对路径
    final String relativePath;    // 相对于工程根目录的相对路径
    final String md5;             // 文件内容的MD5值
    final List<String> lines;     // 文件内容行列表
    final boolean isBinary;
    final long fileSize;
    final Path parentDir;

    public FileData(Path basePath, Path filePath, String relativePath, String md5, List<String> lines, boolean isBinary, long fileSize, Path parentDir) {
        this.basePath = basePath;
        this.filePath = filePath;
        this.fileName = filePath.getFileName().toString();
        this.relativePath = relativePath;
        this.md5 = md5;
        this.lines = lines;
        this.isBinary = isBinary;
        this.fileSize = fileSize;
        this.parentDir = parentDir;
    }

    public int getLineCount() {
        return lines.size();
    }
}