package com.blackg.codediff.tree;

import com.blackg.codediff.FileData;
import com.blackg.codediff.enums.NodeType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 表示目录树结构的类
 */
public class DirectoryTree {

    private final TreeNode root;
    private final Path basePath;

    public DirectoryTree(Path basePath) {
        this.basePath = basePath;
        this.root = new TreeNode("", basePath.toFile().isDirectory() ? NodeType.DIRECTORY : NodeType.FILE, basePath.toString(), basePath.getFileName().toString(), "");
        this.root.setRoot(true);
    }

    public TreeNode getRoot() {
        return root;
    }

    public Path getBasePath() {
        return basePath;
    }

    /**
     * 将文件添加到目录树结构中
     *
     * @param fileData 包含文件信息的FileData对象
     */
    public void addFile(FileData fileData) {
        String normalizedPath = fileData.getFilePath().replace('\\', '/');
        Path filePath = Paths.get(normalizedPath).normalize();
        // 计算文件相对于基础路径的相对路径
        Path relativePath = basePath.relativize(filePath);
        // 从根节点开始遍历
        TreeNode current = root;

        // 遍历路径组件，逐级创建目录节点
        Path parentPath = basePath;
        for (Path component : relativePath) {
            // 获取组件名称
            String name = component.toString();
            // 检查当前节点是否已存在该子节点
            TreeNode child = current.getChild(name);
            // 如果子节点不存在，则创建新节点
            if (child == null) {
                // 构建当前节点的完整路径
                Path resolve = parentPath.resolve(component);
                // 构建当前节点相对路径
                Path relativize = basePath.relativize(resolve);
                // 创建新节点并添加到当前节点的子节点中
                child = new TreeNode(name, resolve.toFile().isDirectory() ? NodeType.DIRECTORY : NodeType.FILE, resolve.toString(), relativize.toString(), fileData.getMd5());
                child.setId(fileData.getId());
                current.addChild(child);
            }

            // 移动到下一个节点
            current = child;
            // 更新父路径为当前节点的完整路径
            parentPath = Paths.get(current.getFullPath());
        }

        // 设置文件数据
//        if (current.getType() == NodeType.FILE) {
//            current.setFileData(fileData);
//        }
    }


    /**
     * 获取树中所有文件节点
     */
    public List<TreeNode> fileNodes() {
        List<TreeNode> fileNodes = new ArrayList<>();
        traverseTree(root, node -> {
            if (node.getType() == NodeType.FILE) {
                fileNodes.add(node);
            }
        });
        return fileNodes;
    }

    /**
     * 递归遍历树
     */
    private void traverseTree(TreeNode node, java.util.function.Consumer<TreeNode> action) {
        action.accept(node);
        for (TreeNode child : node.getChildren().values()) {
            traverseTree(child, action);
        }
    }
}