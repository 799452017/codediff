# 源代码对比工具

源代码对比工具是一个强大的Java应用程序，用于分析和比较两个源代码工程之间的相似性和差异。它不仅能识别完全相同的文件，还能检测内容相似的文件，并提供直观的树状结构展示工程的组织结构。

## 功能特性

### 🔍 高级文件对比
- **相同文件检测**：通过MD5哈希值识别内容完全相同的文件
- **相似文件检测**：使用LCS算法检测相似度超过阈值的文件
- **文件名匹配**：识别同名但内容不同的文件
- **相似度百分比**：精确计算文件间的相似度百分比

### 🌳 可视化工程结构
- **树状结构展示**：直观展示工程的文件和目录结构
- **差异高亮显示**：使用颜色标识文件状态（✓ 相同，Δ 相似，✗ 不同）
- **并排对比**：支持两个工程的并排结构对比
- **完整路径显示**：清晰展示文件在工程中的完整路径

### ⚙️ 高度可配置
- **相似度阈值**：自定义文件相似度判定阈值（默认60%）
- **内容预处理**：可选忽略空白字符和大小写
- **文件过滤**：支持正则表达式排除特定文件
- **输出控制**：可选择显示完整路径或仅文件名

## 快速开始

### 前提条件
- Java 8 或更高版本

### 安装与构建

1. 克隆仓库：
   ```bash
   git clone https://github.com/yourusername/codediff.git
   cd codediff
   ```

2. 使用Maven构建项目：
   ```bash
   mvn clean package
   ```

3. 构建完成后，在`target`目录下会生成可执行JAR文件：
    - `codediff-1.0.0.jar`（主JAR）
    - `codediff-tool-1.0.0.jar`（包含所有依赖的FAT JAR）

### 使用说明

#### 基本用法
```bash
java -jar codediff-tool-1.0.0.jar <project1_path> <project2_path>
```

#### 完整选项
```bash
java -jar codediff-tool-1.0.0.jar <project1_path> <project2_path> [options]
```

#### 命令行选项

| 选项 | 说明 | 默认值                                   |
|------|------|---------------------------------------|
| `-t <阈值>` | 设置相似度阈值 (0.0-1.0) | 0.6                                   |
| `-w` | 忽略空白字符 | true                                  |
| `-i` | 忽略大小写 | true                                  |
| `-e <模式>` | 排除文件模式 (正则表达式) | target/**、.idea/**、.mvn/**、.git/** |
| `-tree` | 显示树状结构 | true                                  |
| `-diff-only` | 只显示有差异的部分 | true                                  |
| `-side-by-side` | 并排对比显示（与-tree一起使用） | true                                  |


### 使用示例

#### 基本比较
```bash
java -jar code-diff-tool.jar /path/to/projectA /path/to/projectB
```

#### 高级比较（带树状结构）
```bash
java -jar code-diff-tool.jar projectA projectB -tree -diff-only
```

#### 输出示例

```
============ 源码对比结果摘要 ============
工程1文件总数: 42
工程2文件总数: 38
----------------------------------------
完全匹配文件数: 12
部分匹配文件数: 8
工程1未匹配文件数: 5
工程2未匹配文件数: 3
----------------------------------------
整体相似度: 76.33%

============ 工程结构对比 ============
工程 1                                          工程 2
----------------------------------------------------------------------------------------------------
src                                            src
├── main                                       ├── main
│   ├── java                                   │   ├── java
│   │   ├── com                                │   │   ├── com
│   │   │   ├── example                        │   │   │   ├── example
│   │   │   │   ├── service                    │   │   │   │   ├── service
│   │   │   │   │   ├── ✓ UserService.java     │   │   │   │   │   ├── ✓ UserService.java
│   │   │   │   │   ├── Δ ProductService.java  │   │   │   │   │   ├── Δ ProductService.java (85.7%)
│   │   │   │   │   └── ✗ OrderService.java    │   │   │   │   │   └── ✗ OrderService.java
│   │   │   │   └── util                       │   │   │   │   └── util
│   │   │   │       ├── ✓ StringUtils.java     │   │   │   │       ├── ✓ StringUtils.java
│   │   │   │       └── Δ DateUtils.java       │   │   │   │       └── Δ DateUtils.java (92.1%)
...
```


### 作为Maven依赖使用

#### 添加依赖

```xml
<dependency>
    <groupId>com.blackg</groupId>
    <artifactId>codediff</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 基本使用示例

```java
import com.example.codediff.SourceCodeComparator;
import com.example.codediff.ComparatorConfig;
import com.example.codediff.ComparisonResult;
import java.nio.file.Paths;

public class BasicUsageExample {
    public static void main(String[] args) throws Exception {
        // 1. 创建配置
        ComparatorConfig config = new ComparatorConfig()
            .setSimilarityThreshold(0.7)
            .setIgnoreWhitespace(true)
            .setIgnoreCase(true)
            .addExcludePattern("**.log");
        
        // 2. 创建比较器
        SourceCodeComparator comparator = new SourceCodeComparator(config);
        
        // 3. 执行比较
        ComparisonResult result = comparator.compareProjects(
            Paths.get("/path/to/project1"),
            Paths.get("/path/to/project2")
        );
        
        // 4. 处理结果
        System.out.println("整体相似度: " + result.getSimilarityScore() + "%");
        System.out.println("完全匹配文件: " + result.getExactMatchCount());
        System.out.println("部分匹配文件: " + result.getDiffMatchCount());
    }
}
```

#### 生成树状结构报告

```java
import com.example.codediff.ResultReporter;
import com.example.codediff.ComparisonResult;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class TreeReportExample {
    public static void main(String[] args) throws Exception {
        // 执行比较...
        ComparisonResult result = ...;
        
        // 输出到控制台
        ResultReporter.printProjectTree(
            result.getDirectoryTree1(), 
            System.out, 
            "工程1",
            true,   // 只显示差异
            true    // 显示完整路径
        );
        
        // 输出到文件
        try (PrintStream fileOut = new PrintStream(new FileOutputStream("tree-report.txt"))) {
            ResultReporter.printTreeComparison(
                result.getDirectoryTree1(),
                result.getDirectoryTree2(),
                fileOut,
                true    // 只显示差异
            );
        }
    }
}
```

### API参考摘要

#### SourceCodeComparator 类

| 方法 | 说明 |
|------|------|
| `compareProjects(Path, Path)` | 比较两个工程 |

#### ComparatorConfig 类

| 方法 | 说明 |
|------|------|
| `setSimilarityThreshold(double)` | 设置相似度阈值 |
| `setIgnoreWhitespace(boolean)` | 设置是否忽略空白 |
| `setIgnoreCase(boolean)` | 设置是否忽略大小写 |
| `addExcludePattern(String)` | 添加文件排除模式 |

#### ComparisonResult 类

| 方法 | 说明 |
|------|------|
| `getSimilarityScore()` | 获取整体相似度 |
| `getExactMatchCount()` | 获取完全匹配文件数 |
| `getDiffMatchCount()` | 获取部分匹配文件数 |
| `getUnmatchedCount1()` | 获取工程1未匹配文件数 |
| `getUnmatchedCount2()` | 获取工程2未匹配文件数 |
| `getAllMatches()` | 获取所有匹配文件对 |
| `getDirectoryTree1()` | 获取工程1目录树 |
| `getDirectoryTree2()` | 获取工程2目录树 |

### 项目结构

```
code-diff-tool/
├── pom.xml
└── src
    └── main
        └── java
            └── com
                └── example
                    └── codediff
                        ├── SourceCodeComparator.java      # 主类
                        ├── ComparatorConfig.java          # 配置类
                        ├── DirectoryTree.java             # 目录树结构
                        ├── FileData.java                  # 文件数据容器
                        ├── FileMatch.java                 # 文件匹配结果
                        ├── ComparisonResult.java          # 对比结果容器
                        └── ResultReporter.java            # 结果报告生成器
```

## 技术细节

### 匹配算法
1. **MD5匹配阶段**：通过文件内容哈希值匹配完全相同的文件
2. **同名文件匹配**：匹配同名文件并计算相似度
3. **内容相似度匹配**：在剩余文件中寻找内容相似度超过阈值的文件

### 相似度计算
- 使用基于最长公共子序列（LCS）的算法
- 相似度公式：`相似度 = (2 * LCS长度) / (文件A行数 + 文件B行数)`
- 整体相似度：`Σ(每个文件相似度得分) / 文件总数 * 100`

### 树状结构生成
- 基于文件路径构建树状结构
- 支持目录排序（目录优先）
- 智能过滤无差异节点
- 彩色状态标识

## 联系方式

 - 

**源代码对比工具** - 让代码比较变得简单直观！