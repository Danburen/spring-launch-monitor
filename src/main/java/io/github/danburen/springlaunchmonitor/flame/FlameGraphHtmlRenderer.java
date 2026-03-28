package io.github.danburen.springlaunchmonitor.flame;

import io.github.danburen.springlaunchmonitor.flame.model.FlameGraphModel;
import io.github.danburen.springlaunchmonitor.flame.model.PackageTreeNode;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.stream.Collectors;

public class FlameGraphHtmlRenderer {

    public String render(FlameGraphModel model) {
        return renderWithReport(model, "", HtmlI18nLabels.englishDefaults());
    }

    public String renderWithReport(FlameGraphModel model, String reportText, HtmlI18nLabels labels) {
        HtmlI18nLabels effective = labels == null ? HtmlI18nLabels.englishDefaults() : labels;
        PackageTreeNode root = model.getRoot();
        long max = Math.max(1, root.getTotalMs());
        String treeHtml = renderNodeHtml(root, max, effective);
        String reportHtml = reportText == null ? "" : reportText;
        String pageTitle = escapeHtml(effective.getPageTitle());

        return """
                <!DOCTYPE html>
                <html lang="%s">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>%s</title>
                  <style>
                    :root { color-scheme: light dark; }
                    body { font-family: Inter, Segoe UI, Arial, sans-serif; margin: 24px; background: #0f172a; color: #e2e8f0; }
                    .card { background: #111827; border: 1px solid #1f2937; border-radius: 12px; padding: 16px 18px; }
                    h1 { margin: 0 0 12px; font-size: 20px; }
                    .hint { color: #94a3b8; font-size: 12px; margin-bottom: 12px; }
                    details { margin-left: 10px; }
                    details > summary { list-style: none; cursor: pointer; padding: 4px 0; }
                    details > summary::-webkit-details-marker { display: none; }
                    .row { display: grid; grid-template-columns: 1fr auto; gap: 8px; align-items: center; }
                    .label { font-size: 13px; color: #cbd5e1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                    .metrics { font-size: 12px; color: #93c5fd; }
                    .bar-wrap { height: 16px; border-radius: 4px; background: #1e293b; margin-top: 4px; overflow: hidden; }
                    .bar { height: 100%%; background: linear-gradient(90deg, #f97316, #ef4444); }
                    .section-title { margin-top: 18px; margin-bottom: 8px; font-size: 16px; color: #e5e7eb; }
                    .report { background: #0b1220; border: 1px solid #1f2937; border-radius: 8px; padding: 12px; white-space: pre-wrap; line-height: 1.5; color: #d1d5db; }
                    .toolbar { display: flex; justify-content: flex-end; gap: 8px; margin-bottom: 8px; }
                    .btn { border: 1px solid #334155; background: #1e293b; color: #e2e8f0; padding: 6px 10px; border-radius: 6px; cursor: pointer; font-size: 12px; }
                    .btn:hover { background: #334155; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>%s</h1>
                    <div class="section-title">%s</div>
                    <div class="report">%s</div>
                    <div class="section-title">%s</div>
                    <div class="toolbar">
                      <button class="btn" type="button" onclick="toggleAll(true)">%s</button>
                      <button class="btn" type="button" onclick="toggleAll(false)">%s</button>
                    </div>
                    <div class="hint">%s</div>
                    %s
                  </div>
                  <script>
                    function toggleAll(open) {
                      document.querySelectorAll('details.tree-node').forEach(function (item) {
                        item.open = open;
                      });
                    }
                  </script>
                </body>
                </html>
                """.formatted(
                escapeHtml(effective.getLang()),
                pageTitle,
                pageTitle,
                escapeHtml(effective.getStartupReportTitle()),
                reportHtml,
                escapeHtml(effective.getPackageFlameTreeTitle()),
                escapeHtml(effective.getExpandAll()),
                escapeHtml(effective.getCollapseAll()),
                escapeHtml(effective.getTreeHint()),
                treeHtml
        );
    }

    private String renderNodeHtml(PackageTreeNode node, long rootMs, HtmlI18nLabels labels) {
        int width = (int) Math.max(1, Math.min(100, Math.round((node.getTotalMs() * 100.0) / rootMs)));
        String children = node.getChildren().values().stream()
                .sorted(Comparator.comparing(PackageTreeNode::getName))
                .map(child -> renderNodeHtml(child, rootMs, labels))
                .collect(Collectors.joining());

        String metrics = MessageFormat.format(labels.getMetricsTemplate(),
                node.getTotalMs(), node.getSelfMs(), node.getBeanCount());

        return """
                <details class="tree-node" open>
                  <summary>
                    <div class="row">
                      <div class="label">%s</div>
                      <div class="metrics">%s</div>
                    </div>
                    <div class="bar-wrap"><div class="bar" style="width:%d%%"></div></div>
                  </summary>
                  %s
                </details>
                """.formatted(escapeHtml(node.getName()), escapeHtml(metrics), width, children);
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

