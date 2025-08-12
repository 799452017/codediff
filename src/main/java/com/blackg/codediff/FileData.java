package com.blackg.codediff;

import lombok.Data;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * 文件数据容器，存储文件元信息和内容
 */
@Data
public class FileData {
    final Path filePath;          // 文件绝对路径
    final String relativePath;    // 相对于工程根目录的相对路径
    final String md5;             // 文件内容的MD5值
    final List<String> lines;     // 文件内容行列表
    final boolean isBinary;
    final long fileSize;
    final Path parentDir;

    FileData(Path filePath, String relativePath, String md5, List<String> lines, boolean isBinary, long fileSize, Path parentDir) {
        this.filePath = filePath;
        this.relativePath = relativePath;
        this.md5 = md5;
        this.lines = lines;
        this.isBinary = isBinary;
        this.fileSize = fileSize;
        this.parentDir = parentDir;
    }
}