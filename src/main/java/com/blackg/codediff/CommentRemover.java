package com.blackg.codediff;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class CommentRemover {
    // 策略接口
    private interface CommentStrategy {
        String removeComments(String source);
    }

    // 策略工厂
    public static CommentStrategy createStrategy(String extension) {
        if (C_STYLE_EXT.contains(extension)) {
            return new CStyleStrategy();
        } else if (PYTHON_EXT.contains(extension)) {
            return new PythonStrategy();
        } else if (SHELL_EXT.contains(extension)) {
            return new ShellStrategy();
        } else if (HTML_EXT.contains(extension)) {
            return new HtmlStrategy();
        } else {
            return new CStyleStrategy(); // 默认策略
        }
    }

    // JDK 8兼容的扩展名集合
    private static final Set<String> C_STYLE_EXT = new HashSet<>(
            Arrays.asList("c", "cpp", "h", "java", "cs", "js", "go"));
    private static final Set<String> PYTHON_EXT = new HashSet<>(
            Collections.singletonList("py"));
    private static final Set<String> SHELL_EXT = new HashSet<>(
            Collections.singletonList("sh"));
    private static final Set<String> HTML_EXT = new HashSet<>(
            Arrays.asList("html", "htm", "xml"));

    // C风格语言策略
    private static class CStyleStrategy implements CommentStrategy {
        @Override
        public String removeComments(String source) {
            return new CommentProcessor(source)
                    .setLineComment("//")
                    .setBlockComment("/*", "*/")
                    .process();
        }
    }

    // Python策略
    private static class PythonStrategy implements CommentStrategy {
        @Override
        public String removeComments(String source) {
            return new CommentProcessor(source)
                    .setLineComment("#")
                    .setBlockComment("\"\"\"", "\"\"\"")
                    .setTripleQuotes(true)
                    .process();
        }
    }

    // Shell策略
    private static class ShellStrategy implements CommentStrategy {
        @Override
        public String removeComments(String source) {
            return new CommentProcessor(source)
                    .setLineComment("#")
                    .process();
        }
    }

    // HTML策略
    private static class HtmlStrategy implements CommentStrategy {
        @Override
        public String removeComments(String source) {
            return new CommentProcessor(source)
                    .setBlockComment("<!--", "-->")
                    .process();
        }
    }

    // 核心状态处理逻辑
    private static class CommentProcessor {
        private final String source;
        private String lineStart = null;
        private String blockStart = null;
        private String blockEnd = null;
        private boolean tripleQuotes = false;

        public CommentProcessor(String source) {
            this.source = source;
        }

        public CommentProcessor setLineComment(String marker) {
            this.lineStart = marker;
            return this;
        }

        public CommentProcessor setBlockComment(String start, String end) {
            this.blockStart = start;
            this.blockEnd = end;
            return this;
        }

        public CommentProcessor setTripleQuotes(boolean enable) {
            this.tripleQuotes = enable;
            return this;
        }

        public String process() {
            StringBuilder output = new StringBuilder();
            State state = State.CODE;
            int blockStartLen = blockStart != null ? blockStart.length() : 0;
            int blockEndLen = blockEnd != null ? blockEnd.length() : 0;
            boolean inString = false;
            char stringChar = ' ';
            int quoteCount = 0;
            int i = 0;

            while (i < source.length()) {
                char c = source.charAt(i);

                switch (state) {
                    case CODE:
                        if (!inString) {
                            // 检测行注释
                            if (lineStart != null && startsWith(source, i, lineStart)) {
                                state = State.LINE_COMMENT;
                                i += lineStart.length() - 1;
                            }
                            // 检测块注释
                            else if (blockStart != null && startsWith(source, i, blockStart)) {
                                state = State.BLOCK_COMMENT;
                                i += blockStartLen - 1;
                            }
                            // 检测三引号
                            else if (tripleQuotes && (c == '\'' || c == '"')) {
                                quoteCount = countConsecutive(source, i, c);
                                if (quoteCount >= 3) {
                                    inString = true;
                                    stringChar = c;
                                    i += quoteCount - 1;
                                    output.append(source.substring(i - quoteCount + 1, i + 1));
                                } else {
                                    output.append(c);
                                }
                            }
                            // 处理普通字符
                            else {
                                handleStringChar(c, output);
                                if (checkStringStart(c, i)) {
                                    inString = true;
                                    stringChar = c;
                                }
                            }
                        } else {
                            handleStringChar(c, output);
                            if (checkStringEnd(c, i, stringChar, quoteCount)) {
                                inString = false;
                            }
                        }
                        break;

                    case LINE_COMMENT:
                        if (c == '\n') {
                            state = State.CODE;
                            output.append(c); // 保留换行符
                        }
                        break;

                    case BLOCK_COMMENT:
                        if (blockEnd != null && startsWith(source, i, blockEnd)) {
                            state = State.CODE;
                            i += blockEndLen - 1; // 跳过结束标记
                        }
                        break;
                }
                i++;
            }
            return output.toString();
        }

        private boolean startsWith(String str, int start, String prefix) {
            if (start + prefix.length() > str.length()) return false;
            for (int j = 0; j < prefix.length(); j++) {
                if (str.charAt(start + j) != prefix.charAt(j)) return false;
            }
            return true;
        }

        private int countConsecutive(String str, int start, char target) {
            int count = 0;
            for (int j = start; j < str.length() && str.charAt(j) == target; j++) {
                count++;
            }
            return count;
        }

        private void handleStringChar(char c, StringBuilder output) {
            output.append(c);
        }

        private boolean checkStringStart(char c, int i) {
            return (c == '\'' || c == '"') &&
                    (i == 0 || source.charAt(i - 1) != '\\');
        }

        private boolean checkStringEnd(char c, int i, char stringChar, int quoteCount) {
            if (c == stringChar) {
                if (i == 0 || source.charAt(i - 1) != '\\') {
                    quoteCount--;
                    return quoteCount <= 0;
                }
            }
            return false;
        }
    }

    enum State {
        CODE, LINE_COMMENT, BLOCK_COMMENT
    }

    // 主入口方法
    public static String removeComments(String source, String fileName) {
        return createStrategy(getExtension(fileName)).removeComments(source);
    }

    private static String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return (idx > 0 && idx < fileName.length() - 1) ?
                fileName.substring(idx + 1).toLowerCase() : "";
    }

    // JDK 8兼容的文件读写
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("用法: java CommentRemover <源文件>");
            return;
        }

        String inputFile = args[0];
        String source = readFileContent(inputFile);
        String cleaned = removeComments(source, inputFile);
        String outputFile = "clean_" + inputFile;
        writeFileContent(outputFile, cleaned);
        System.out.println("注释已成功移除并保存到: " + outputFile);
    }

    private static String readFileContent(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("读取文件失败: " + e.getMessage());
            return "";
        }
    }

    private static void writeFileContent(String filePath, String content) {
        try {
            Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("写入文件失败: " + e.getMessage());
        }
    }
}