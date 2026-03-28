package io.github.danburen.springlaunchmonitor.spring.flame;

import io.github.danburen.springlaunchmonitor.data.LaunchRecordsCtx;
import io.github.danburen.springlaunchmonitor.spring.flame.builder.PackageTreeModelBuilder;
import io.github.danburen.springlaunchmonitor.spring.flame.model.FlameGraphModel;

public class PackageTreeFlameGraphDataGenerator implements FlameGraphDataGenerator {
    private final PackageTreeModelBuilder modelBuilder = new PackageTreeModelBuilder();
    private final FlameGraphTextRenderer textRenderer = new FlameGraphTextRenderer();
    private final FlameGraphJsonRenderer jsonRenderer = new FlameGraphJsonRenderer();
    private final FlameGraphHtmlRenderer htmlRenderer = new FlameGraphHtmlRenderer();

    @Override
    public String generate(LaunchRecordsCtx records) {
        FlameGraphModel model = modelBuilder.build(records);
        return textRenderer.render(model);
    }

    @Override
    public String generateJsonTree(LaunchRecordsCtx records) {
        FlameGraphModel model = modelBuilder.build(records);
        return jsonRenderer.render(model);
    }

    @Override
    public String generateHtml(LaunchRecordsCtx records, String reportText, HtmlI18nLabels labels) {
        FlameGraphModel model = modelBuilder.build(records);
        return htmlRenderer.renderWithReport(model, reportText, labels);
    }
}
