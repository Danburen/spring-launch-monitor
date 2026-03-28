package io.github.danburen.springlaunchmonitor.flame.model;

import java.util.Map;
import java.util.TreeMap;

public class PackageTreeNode {
    private final String name;
    private long totalMs;
    private long selfMs;
    private int beanCount;
    private final Map<String, PackageTreeNode> children = new TreeMap<>();

    public PackageTreeNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getTotalMs() {
        return totalMs;
    }

    public long getSelfMs() {
        return selfMs;
    }

    public int getBeanCount() {
        return beanCount;
    }

    public Map<String, PackageTreeNode> getChildren() {
        return children;
    }

    public void addToTotal(long durationMs) {
        this.totalMs += durationMs;
    }

    public void addToSelf(long durationMs) {
        this.selfMs += durationMs;
    }

    public void incrementBeanCount() {
        this.beanCount += 1;
    }
}

