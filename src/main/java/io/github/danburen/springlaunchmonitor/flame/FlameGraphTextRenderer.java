package io.github.danburen.springlaunchmonitor.spring.flame;

import io.github.danburen.springlaunchmonitor.data.TimelineEvent;
import io.github.danburen.springlaunchmonitor.spring.flame.model.FlameGraphModel;
import io.github.danburen.springlaunchmonitor.spring.flame.model.PackageTreeNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FlameGraphTextRenderer {

    public String render(FlameGraphModel model) {
        String folded = renderFolded(model);
        String tree = renderTree(model.getRoot());
        return """
                FlameGraphData
                --- Folded ---
                %s

                --- Package Delay Tree (auto-expanded) ---
                %s
                """.formatted(folded.stripTrailing(), tree.stripTrailing());
    }

    private String renderFolded(FlameGraphModel model) {
        StringBuilder sb = new StringBuilder();
        for (TimelineEvent event : model.getTimelineEvents()) {
            sb.append("springboot;")
                    .append(sanitize(event.getPhase()))
                    .append(' ')
                    .append(Math.max(0, event.getDurationMs()))
                    .append('\n');
        }

        appendPackageFolded(model.getRoot(), new ArrayList<>(), sb);
        return sb.toString();
    }

    private String renderTree(PackageTreeNode root) {
        StringBuilder sb = new StringBuilder();
        sb.append("- bean-init (total: ")
                .append(root.getTotalMs())
                .append("ms, beans: ")
                .append(root.getBeanCount())
                .append(")\n");
        appendTree(root, "  ", sb);
        return sb.toString();
    }

    private void appendPackageFolded(PackageTreeNode node, List<String> path, StringBuilder sb) {
        List<PackageTreeNode> children = node.getChildren().values().stream()
                .sorted(Comparator.comparing(PackageTreeNode::getName))
                .toList();

        for (PackageTreeNode child : children) {
            path.add(child.getName());
            sb.append("springboot;bean-init;")
                    .append(String.join(";", path.stream().map(this::sanitize).toList()))
                    .append(' ')
                    .append(child.getTotalMs())
                    .append('\n');
            appendPackageFolded(child, path, sb);
            path.remove(path.size() - 1);
        }
    }

    private void appendTree(PackageTreeNode parent, String indent, StringBuilder sb) {
        List<PackageTreeNode> children = parent.getChildren().values().stream()
                .sorted(Comparator.comparing(PackageTreeNode::getName))
                .toList();

        for (PackageTreeNode child : children) {
            sb.append(indent)
                    .append("- ")
                    .append(child.getName())
                    .append(" (total: ")
                    .append(child.getTotalMs())
                    .append("ms, self: ")
                    .append(child.getSelfMs())
                    .append("ms, beans: ")
                    .append(child.getBeanCount())
                    .append(")\n");
            appendTree(child, indent + "  ", sb);
        }
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.replace(';', '_').replace(' ', '_');
    }
}

