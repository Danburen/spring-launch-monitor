package io.github.danburen.springlaunchmonitor.spring.flame.model;

import io.github.danburen.springlaunchmonitor.data.TimelineEvent;

import java.util.List;

public class FlameGraphModel {
    private final List<TimelineEvent> timelineEvents;
    private final PackageTreeNode root;

    public FlameGraphModel(List<TimelineEvent> timelineEvents, PackageTreeNode root) {
        this.timelineEvents = timelineEvents;
        this.root = root;
    }

    public List<TimelineEvent> getTimelineEvents() {
        return timelineEvents;
    }

    public PackageTreeNode getRoot() {
        return root;
    }
}

