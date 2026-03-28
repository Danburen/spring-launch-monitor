package io.github.danburen.springlaunchmonitor.flame;

import io.github.danburen.springlaunchmonitor.flame.model.FlameGraphModel;
import io.github.danburen.springlaunchmonitor.flame.model.PackageTreeNode;

import java.util.Comparator;
import java.util.stream.Collectors;

public class FlameGraphJsonRenderer {

    public String render(FlameGraphModel model) {
        return "{\"packageTree\":" + buildNodeJson(model.getRoot()) + "}";
    }

    private String buildNodeJson(PackageTreeNode node) {
        String childrenJson = node.getChildren().values().stream()
                .sorted(Comparator.comparing(PackageTreeNode::getName))
                .map(this::buildNodeJson)
                .collect(Collectors.joining(",", "[", "]"));

        return "{"
                + "\"name\":\"" + escapeJson(node.getName()) + "\"," 
                + "\"totalMs\":" + node.getTotalMs() + ","
                + "\"selfMs\":" + node.getSelfMs() + ","
                + "\"beanCount\":" + node.getBeanCount() + ","
                + "\"children\":" + childrenJson
                + "}";
    }


    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

