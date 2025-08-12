package com.blackg.codediff.tree;

import com.blackg.codediff.FileData;
import com.blackg.codediff.enums.NodeType;

import java.nio.file.Path;
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
        this.root = new TreeNode("", NodeType.DIRECTORY, basePath);
        this.root.setRoot(true);
    }

    public TreeNode getRoot() {
        return root;
    }

    public Path getBasePath() {
        return basePath;
    }

    /**
     * 添加文件到树结构中
     */
    public void addFile(FileData fileData) {
        Path relativePath = basePath.relativize(fileData.getFilePath());
        TreeNode current = root;

        // 遍历路径组件
        Path parentPath = basePath;
        for (Path component : relativePath) {
            String name = component.toString();
            TreeNode child = current.getChild(name);

            if (child == null) {
                parentPath = parentPath.resolve(name);
                boolean isDir = !parentPath.equals(fileData.getFilePath());
                NodeType type = isDir ? NodeType.DIRECTORY : NodeType.FILE;

                child = new TreeNode(name, type, parentPath);
                current.addChild(child);
            }

            current = child;
            parentPath = current.getFullPath();
        }

        // 设置文件数据
        if (current.getType() == NodeType.FILE) {
            current.setFileData(fileData);
        }
    }

    /**
     * 获取树中所有文件节点
     */
    public List<TreeNode> getAllFileNodes() {
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