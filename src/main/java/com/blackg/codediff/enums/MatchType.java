package com.blackg.codediff.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 匹配类型枚举
 */
@Getter
@AllArgsConstructor
public enum MatchType {
    EXACT_MATCH("MD5完全匹配"),    // MD5完全匹配
    NAME_MATCH("文件名相同，内容相同或相似"),
    CONTENT_MATCH("文件名不同，内容相同或相似")
    ;

    private final String description;
}