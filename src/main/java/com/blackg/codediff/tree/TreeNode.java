package com.blackg.codediff.tree;

import com.blackg.codediff.FileData;
import com.blackg.codediff.FileMatch;
import com.blackg.codediff.enums.NodeType;
import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TreeNode {
    private final String name;
    private final NodeType type;
    private final Path fullPath;
    private final Map<String, TreeNode> children = new HashMap<>();
    private TreeNode parent;
    private boolean root = false;

    // 文件节点特有属性
    private FileData fileData;
    private FileMatch fileMatch;

    public TreeNode(String name, NodeType type, Path fullPath) {
        this.name = name;
        this.type = type;
        this.fullPath = fullPath;
    }

    public void addChild(TreeNode node) {
        node.parent = this;
        children.put(node.name, node);
    }

    public TreeNode getChild(String name) {
        return children.get(name);
    }


    /**
     * 获取排序后的子节点列表
     */
    public List<TreeNode> getSortedChildren() {
        List<TreeNode> children = new ArrayList<>(this.children.values());

        // 排序规则：目录在前，文件在后，按名称排序
        children.sort((a, b) -> {
            if (a.type != b.type) {
                return a.type == NodeType.DIRECTORY ? -1 : 1;
            }
            return a.name.compareToIgnoreCase(b.name);
        });

        return children;
    }
}