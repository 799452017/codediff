package com.blackg.codediff.tree;

import com.alibaba.fastjson.annotation.JSONField;
import com.blackg.codediff.FileData;
import com.blackg.codediff.FileMatch;
import com.blackg.codediff.enums.NodeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TreeNode {
    private String id;
    private String name;
    private NodeType type;
//    @JsonIgnore //忽略jackson序列化
//    @JSONField(serialize = false) //忽略fastjson序列化
    private String fullPath;
//    @JsonIgnore //忽略jackson序列化
//    @JSONField(serialize = false) //忽略fastjson序列化
    private String relativePath;
    private String md5;
    private Map<String, TreeNode> children = new HashMap<>();
    @JsonIgnore //忽略jackson序列化
    @JSONField(serialize = false) //忽略fastjson序列化
    private TreeNode parent;
    private boolean root = false;

    // 文件节点特有属性
    private FileData fileData;
    private FileMatch fileMatch;

    public TreeNode(String name, NodeType type, String fullPath, String relativePath, String md5) {
        this.name = name;
        this.type = type;
        this.fullPath = fullPath;
        this.relativePath = relativePath;
        this.md5 = md5;
    }

    public void addChild(TreeNode node) {
        node.parent = this;
        children.put(node.name, node);
    }

    @JsonIgnore //忽略jackson序列化
    @JSONField(serialize = false) //忽略fastjson序列化
    public TreeNode getChild(String name) {
        return children.get(name);
    }


    /**
     * 获取排序后的子节点列表
     */
    @JsonIgnore //忽略jackson序列化
    @JSONField(serialize = false) //忽略fastjson序列化
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