package com.recipebook.nutritionhistory;

import com.recipebook.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

@Route(value = "nutrition-history", layout = MainLayout.class)
@JsModule("./apexcharts-loader.js")
public class NutritionHistoryView extends VerticalLayout {

    private final NutritionHistoryService service;

    private int periodDays = 7;
    private Button btn7Days;
    private Button btn30Days;

    private final VerticalLayout contentLayout = new VerticalLayout();

    public NutritionHistoryView(NutritionHistoryService service) {
        this.service = service;

        setAlignItems(Alignment.CENTER);
        setPadding(true);
        setSpacing(true);

        VerticalLayout container = new VerticalLayout();
        container.setMaxWidth("1200px");
        container.setWidthFull();
        container.setPadding(false);
        container.setSpacing(true);

        // Title
        H2 title = new H2(getTranslation("nutritionHistory.title", "Nutrition Trends"));
        title.getStyle().set("margin", "0");
        Span subtitle = new Span(getTranslation("nutritionHistory.subtitle", "Track your nutrition over time"));
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Period selector
        btn7Days = new Button(getTranslation("nutritionHistory.period.7days", "7 Days"));
        btn30Days = new Button(getTranslation("nutritionHistory.period.30days", "30 Days"));

        btn7Days.addClickListener(e -> setPeriod(7));
        btn30Days.addClickListener(e -> setPeriod(30));

        HorizontalLayout periodSelector = new HorizontalLayout(btn7Days, btn30Days);
        periodSelector.setSpacing(true);

        HorizontalLayout headerRow = new HorizontalLayout(new VerticalLayout(title, subtitle), periodSelector);
        headerRow.setWidthFull();
        headerRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerRow.setAlignItems(Alignment.END);

        contentLayout.setPadding(false);
        contentLayout.setSpacing(true);
        contentLayout.setWidthFull();

        container.add(headerRow, contentLayout);
        add(container);

        updatePeriodButtons();
        loadData();
    }

    private void setPeriod(int days) {
        this.periodDays = days;
        updatePeriodButtons();
        loadData();
    }

    private void updatePeriodButtons() {
        if (periodDays == 7) {
            btn7Days.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btn7Days.removeClassName("toggle-inactive");
            btn30Days.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btn30Days.addClassName("toggle-inactive");
        } else {
            btn30Days.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btn30Days.removeClassName("toggle-inactive");
            btn7Days.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btn7Days.addClassName("toggle-inactive");
        }
    }

    private void loadData() {
        contentLayout.removeAll();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(periodDays - 1);

        List<DailyNutritionSummary> data;
        try {
            data = service.getNutritionHistory(startDate, endDate);
        } catch (Exception e) {
            Span error = new Span(getTranslation("nutritionHistory.noData", "No food log entries found for this period"));
            error.getStyle().set("color", "var(--lumo-secondary-text-color)");
            contentLayout.add(error);
            return;
        }

        if (data.isEmpty()) {
            Span noData = new Span(getTranslation("nutritionHistory.noData", "No food log entries found for this period"));
            noData.getStyle().set("color", "var(--lumo-secondary-text-color)");
            contentLayout.add(noData);
            return;
        }

        // Average stats
        contentLayout.add(buildAverageStats(data));

        // Calories chart
        Div caloriesChart = buildChartCard(
                getTranslation("nutritionHistory.chart.calories", "Calories Over Time"),
                buildCaloriesChartJs(data));
        contentLayout.add(caloriesChart);

        // Macros chart
        Div macrosChart = buildChartCard(
                getTranslation("nutritionHistory.chart.macros", "Daily Macro Breakdown"),
                buildMacrosChartJs(data));
        contentLayout.add(macrosChart);
    }

    private HorizontalLayout buildAverageStats(List<DailyNutritionSummary> data) {
        int count = data.size();
        double avgCal = data.stream().mapToDouble(DailyNutritionSummary::totalCalories).sum() / count;
        double avgPro = data.stream().mapToDouble(DailyNutritionSummary::totalProtein).sum() / count;
        double avgCarbs = data.stream().mapToDouble(DailyNutritionSummary::totalCarbs).sum() / count;
        double avgFat = data.stream().mapToDouble(DailyNutritionSummary::totalFat).sum() / count;

        double avgTargetCal = data.stream().mapToDouble(DailyNutritionSummary::targetCalories).sum() / count;
        double avgTargetPro = data.stream().mapToDouble(DailyNutritionSummary::targetProtein).sum() / count;
        double avgTargetCarbs = data.stream().mapToDouble(DailyNutritionSummary::targetCarbs).sum() / count;
        double avgTargetFat = data.stream().mapToDouble(DailyNutritionSummary::targetFat).sum() / count;

        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("flex-wrap", "wrap");

        row.add(createAvgStatCard(VaadinIcon.FIRE,
                getTranslation("nutritionHistory.avgCalories", "Avg Calories"),
                String.format("%.0f / %.0f kcal", avgCal, avgTargetCal),
                "var(--lumo-error-color)"));
        row.add(createAvgStatCard(VaadinIcon.DROP,
                getTranslation("nutritionHistory.avgProtein", "Avg Protein"),
                String.format("%.0fg / %.0fg", avgPro, avgTargetPro),
                "var(--lumo-primary-color)"));
        row.add(createAvgStatCard(VaadinIcon.CLUSTER,
                getTranslation("nutritionHistory.avgCarbs", "Avg Carbs"),
                String.format("%.0fg / %.0fg", avgCarbs, avgTargetCarbs),
                "var(--lumo-success-color)"));
        row.add(createAvgStatCard(VaadinIcon.DIAMOND,
                getTranslation("nutritionHistory.avgFat", "Avg Fat"),
                String.format("%.0fg / %.0fg", avgFat, avgTargetFat),
                "var(--recipe-warning-color)"));

        return row;
    }

    private VerticalLayout createAvgStatCard(VaadinIcon vaadinIcon, String label, String value, String accentColor) {
        Icon icon = vaadinIcon.create();
        icon.setSize("28px");
        icon.setColor(accentColor);

        H3 valueLabel = new H3(value);
        valueLabel.getStyle()
                .set("margin", "var(--lumo-space-xs) 0 0 0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("color", accentColor);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout card = new VerticalLayout(icon, valueLabel, labelSpan);
        card.setPadding(true);
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.addClassName("dashboard-stat-card");
        card.setWidth(null);
        card.getStyle()
                .set("flex", "1 1 200px")
                .set("min-width", "180px")
                .set("background", "#232838")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)");

        return card;
    }

    private Div buildChartCard(String title, String chartJs) {
        Div card = new Div();
        card.addClassName("nutrition-chart-card");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("color", "var(--lumo-body-text-color)")
                .set("display", "block")
                .set("margin-bottom", "var(--lumo-space-s)");
        card.add(titleSpan);

        Div chartDiv = new Div();
        chartDiv.setId("chart-" + System.identityHashCode(chartDiv));
        chartDiv.setWidthFull();
        chartDiv.getStyle().set("min-height", "320px");
        card.add(chartDiv);

        // Use addAttachListener to ensure DOM element exists before executing JS
        chartDiv.addAttachListener(event -> {
            String js = chartJs.replace("__CHART_ID__", chartDiv.getId().orElse(""));
            chartDiv.getElement().executeJs(js);
        });

        return card;
    }

    private String buildCaloriesChartJs(List<DailyNutritionSummary> data) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");

        StringJoiner categories = new StringJoiner(",");
        StringJoiner actualData = new StringJoiner(",");
        StringJoiner targetData = new StringJoiner(",");

        for (DailyNutritionSummary d : data) {
            LocalDate date = LocalDate.parse(d.date());
            categories.add("'" + date.format(fmt) + "'");
            actualData.add(String.format("%.0f", d.totalCalories()));
            targetData.add(String.format("%.0f", d.targetCalories()));
        }

        return """
            var el = document.getElementById('__CHART_ID__');
            if (!el || !window.ApexCharts) return;
            var options = {
                chart: {
                    type: 'line',
                    height: 320,
                    background: 'transparent',
                    toolbar: { show: false },
                    fontFamily: 'inherit'
                },
                series: [
                    { name: 'Actual', data: [%s] },
                    { name: 'Target', data: [%s] }
                ],
                xaxis: {
                    categories: [%s],
                    labels: { style: { colors: '#8890a0' } }
                },
                yaxis: {
                    labels: { style: { colors: '#8890a0' },
                              formatter: function(v) { return Math.round(v) + ' kcal'; } }
                },
                colors: ['#c75050', '#8890a0'],
                stroke: {
                    width: [3, 2],
                    dashArray: [0, 5]
                },
                markers: { size: [4, 0], strokeWidth: 0 },
                grid: {
                    borderColor: 'rgba(216,220,228,0.08)',
                    strokeDashArray: 3
                },
                tooltip: {
                    theme: 'dark',
                    y: { formatter: function(v) { return Math.round(v) + ' kcal'; } }
                },
                legend: { labels: { colors: '#8890a0' } },
                theme: { mode: 'dark' }
            };
            var chart = new window.ApexCharts(el, options);
            chart.render();
            """.formatted(actualData, targetData, categories);
    }

    private String buildMacrosChartJs(List<DailyNutritionSummary> data) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");

        StringJoiner categories = new StringJoiner(",");
        StringJoiner proteinData = new StringJoiner(",");
        StringJoiner carbsData = new StringJoiner(",");
        StringJoiner fatData = new StringJoiner(",");

        for (DailyNutritionSummary d : data) {
            LocalDate date = LocalDate.parse(d.date());
            categories.add("'" + date.format(fmt) + "'");
            proteinData.add(String.format("%.0f", d.totalProtein()));
            carbsData.add(String.format("%.0f", d.totalCarbs()));
            fatData.add(String.format("%.0f", d.totalFat()));
        }

        return """
            var el = document.getElementById('__CHART_ID__');
            if (!el || !window.ApexCharts) return;
            var options = {
                chart: {
                    type: 'bar',
                    height: 320,
                    stacked: true,
                    background: 'transparent',
                    toolbar: { show: false },
                    fontFamily: 'inherit'
                },
                series: [
                    { name: 'Protein', data: [%s] },
                    { name: 'Carbs', data: [%s] },
                    { name: 'Fat', data: [%s] }
                ],
                xaxis: {
                    categories: [%s],
                    labels: { style: { colors: '#8890a0' } }
                },
                yaxis: {
                    labels: { style: { colors: '#8890a0' },
                              formatter: function(v) { return Math.round(v) + 'g'; } }
                },
                colors: ['#c08050', '#5a9e7e', '#c0903c'],
                plotOptions: {
                    bar: { borderRadius: 2, columnWidth: '60%%' }
                },
                grid: {
                    borderColor: 'rgba(216,220,228,0.08)',
                    strokeDashArray: 3
                },
                tooltip: {
                    theme: 'dark',
                    y: { formatter: function(v) { return Math.round(v) + 'g'; } }
                },
                legend: { labels: { colors: '#8890a0' } },
                theme: { mode: 'dark' }
            };
            var chart = new window.ApexCharts(el, options);
            chart.render();
            """.formatted(proteinData, carbsData, fatData, categories);
    }
}
