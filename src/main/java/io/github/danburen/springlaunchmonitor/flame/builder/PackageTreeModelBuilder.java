package io.github.danburen.springlaunchmonitor.spring.flame.builder;

import io.github.danburen.springlaunchmonitor.data.BeanInitRecord;
import io.github.danburen.springlaunchmonitor.data.LaunchRecordsCtx;
import io.github.danburen.springlaunchmonitor.spring.flame.model.FlameGraphModel;
import io.github.danburen.springlaunchmonitor.spring.flame.model.PackageTreeNode;

public class PackageTreeModelBuilder {

    public FlameGraphModel build(LaunchRecordsCtx records) {
        PackageTreeNode root = new PackageTreeNode("bean-init");
        for (BeanInitRecord record : records.getBeanRecords()) {
            long duration = Math.max(0, record.getDurationMs());
            String packageName = extractPackage(record.getClassName());
            String[] segments = packageName.split("\\.");

            PackageTreeNode current = root;
            current.addToTotal(duration);
            current.incrementBeanCount();

            for (String segment : segments) {
                if (segment.isBlank()) {
                    continue;
                }
                current = current.getChildren().computeIfAbsent(segment, PackageTreeNode::new);
                current.addToTotal(duration);
                current.incrementBeanCount();
            }
            current.addToSelf(duration);
        }
        return new FlameGraphModel(records.getEvents(), root);
    }

    private String extractPackage(String className) {
        if (className == null || className.isBlank()) {
            return "default";
        }
        int lastDot = className.lastIndexOf('.');
        if (lastDot <= 0) {
            return "default";
        }
        return className.substring(0, lastDot);
    }
}

