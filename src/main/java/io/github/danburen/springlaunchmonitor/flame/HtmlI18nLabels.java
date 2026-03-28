package io.github.danburen.springlaunchmonitor.flame;

public class HtmlI18nLabels {
    private final String lang;
    private final String pageTitle;
    private final String startupReportTitle;
    private final String packageFlameTreeTitle;
    private final String expandAll;
    private final String collapseAll;
    private final String treeHint;
    private final String metricsTemplate;

    public HtmlI18nLabels(String lang,
                          String pageTitle,
                          String startupReportTitle,
                          String packageFlameTreeTitle,
                          String expandAll,
                          String collapseAll,
                          String treeHint,
                          String metricsTemplate) {
        this.lang = lang;
        this.pageTitle = pageTitle;
        this.startupReportTitle = startupReportTitle;
        this.packageFlameTreeTitle = packageFlameTreeTitle;
        this.expandAll = expandAll;
        this.collapseAll = collapseAll;
        this.treeHint = treeHint;
        this.metricsTemplate = metricsTemplate;
    }

    public String getLang() {
        return lang;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getStartupReportTitle() {
        return startupReportTitle;
    }

    public String getPackageFlameTreeTitle() {
        return packageFlameTreeTitle;
    }

    public String getExpandAll() {
        return expandAll;
    }

    public String getCollapseAll() {
        return collapseAll;
    }

    public String getTreeHint() {
        return treeHint;
    }

    public String getMetricsTemplate() {
        return metricsTemplate;
    }

    public static HtmlI18nLabels englishDefaults() {
        return new HtmlI18nLabels(
                "en",
                "Spring Launch Monitor Startup Report",
                "Startup Report",
                "Package Flame Tree",
                "Expand All",
                "Collapse All",
                "Package delay tree is expanded by default. Each bar width is proportional to root total latency.",
                "total={0}ms, self={1}ms, beans={2}"
        );
    }
}

