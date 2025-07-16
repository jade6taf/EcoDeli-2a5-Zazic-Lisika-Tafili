package com.ecodeli.analytics.ui.components;

import com.ecodeli.analytics.ui.styles.ColorPalette;
import com.ecodeli.analytics.ui.styles.StyleFactory;
import java.util.Map;


public class KpiComponent {

    public static String createKpiCard(String icon, String label, String value, String trend) {
        StringBuilder kpi = new StringBuilder();

        kpi.append("<div style=\"")
           .append(StyleFactory.createKpiCardStyle())
           .append(" padding: 30px; text-align: center; min-height: 140px; display: flex; flex-direction: column; justify-content: center;\">");

        kpi.append("<div style=\"").append(StyleFactory.createEmojiIconStyle()).append("\">")
           .append(icon).append("</div>");

        kpi.append("<div style=\"").append(StyleFactory.createKpiValueStyle())
           .append(" margin: 12px 0;\">").append(value).append("</div>");

        kpi.append("<div style=\"").append(StyleFactory.createKpiLabelStyle()).append("\">")
           .append(label).append("</div>");

        if (trend != null && !trend.isEmpty()) {
            String trendColor = trend.startsWith("+") ? ColorPalette.GREEN_SUCCESS :
                               trend.startsWith("-") ? ColorPalette.ERROR : ColorPalette.INFO;
            kpi.append("<div style=\"font-size: 12px; color: ").append(trendColor)
               .append("; margin-top: 8px;\">").append(trend).append("</div>");
        }

        kpi.append("</div>");

        return kpi.toString();
    }

    public static String createKpiSection(Map<String, Object> data) {
        StringBuilder section = new StringBuilder();

        section.append("<div style=\"margin: 30px 0 20px 0;\">");
        section.append("<h2 style=\"").append(StyleFactory.createSectionTitleStyle()).append("\">")
               .append("Indicateurs Clés de Performance</h2>");
        section.append("</div>");

        section.append("<div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 25px; margin-bottom: 30px;\">");

        KpiMetrics metrics = calculateMetrics(data);

        section.append(createKpiCard("👥", "Total Utilisateurs",
                                    String.valueOf(metrics.totalUsers),
                                    metrics.userTrend));

        section.append(createKpiCard("💰", "CA Mensuel",
                                    String.format("%.1fK€", metrics.monthlyRevenue / 1000),
                                    metrics.revenueTrend));

        section.append(createKpiCard("⭐", "Clients Actifs",
                                    String.format("%.0f%%", metrics.activeClientsPercent),
                                    metrics.activeClientsTrend));

        section.append(createKpiCard("📈", "Taux Succès",
                                    String.format("%.0f%%", metrics.successRate),
                                    metrics.successTrend));

        section.append("</div>");
        return section.toString();
    }

    public static String createCompactKpi(String icon, String value, String label) {
        StringBuilder kpi = new StringBuilder();

        kpi.append("<div style=\"display: flex; align-items: center; gap: 12px; padding: 15px; background-color: ")
           .append(ColorPalette.GREEN_SUBTLE).append("; border-radius: 10px; border-left: 4px solid ")
           .append(ColorPalette.GREEN_PRIMARY).append(";\">");
        kpi.append("<span style=\"font-size: 24px;\">").append(icon).append("</span>");
        kpi.append("<div>");
        kpi.append("<div style=\"").append(StyleFactory.createKpiValueStyle())
           .append(" font-size: 20px; margin: 0;\">").append(value).append("</div>");
        kpi.append("<div style=\"").append(StyleFactory.createKpiLabelStyle())
           .append(" font-size: 12px;\">").append(label).append("</div>");
        kpi.append("</div>");
        kpi.append("</div>");

        return kpi.toString();
    }

    private static KpiMetrics calculateMetrics(Map<String, Object> data) {
        KpiMetrics metrics = new KpiMetrics();

        if (data == null) {
            return metrics;
        }

        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> users = (java.util.List<Map<String, Object>>) data.get("users");
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> livraisons = (java.util.List<Map<String, Object>>) data.get("livraisons");
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, Object>> services = (java.util.List<Map<String, Object>>) data.get("services");

        metrics.totalUsers = users != null ? users.size() : 0;
        metrics.userTrend = "+12%";

        double revenueFromDeliveries = livraisons != null ?
            livraisons.stream().mapToDouble(l -> (Double) l.getOrDefault("prix", 0.0)).sum() : 0;
        double revenueFromServices = services != null ?
            services.stream().mapToDouble(s -> (Double) s.getOrDefault("prix", 0.0)).sum() : 0;

        metrics.monthlyRevenue = revenueFromDeliveries + revenueFromServices;
        metrics.revenueTrend = "+8.5%";

        long activeClients = users != null ?
            users.stream().filter(u -> "CLIENT".equals(u.get("type"))).count() : 0;
        metrics.activeClientsPercent = metrics.totalUsers > 0 ?
            (activeClients * 100.0 / metrics.totalUsers) : 0;
        metrics.activeClientsTrend = "+5.2%";

        long completedDeliveries = livraisons != null ?
            livraisons.stream().filter(l -> "TERMINEE".equals(l.get("statut"))).count() : 0;
        metrics.successRate = livraisons != null && !livraisons.isEmpty() ?
            (completedDeliveries * 100.0 / livraisons.size()) : 0;
        metrics.successTrend = "+2.1%";

        return metrics;
    }

    private static class KpiMetrics {
        int totalUsers = 0;
        double monthlyRevenue = 0;
        double activeClientsPercent = 0;
        double successRate = 0;
        String userTrend = "";
        String revenueTrend = "";
        String activeClientsTrend = "";
        String successTrend = "";
    }

    public static String createKpiGrid(String... kpiCards) {
        StringBuilder grid = new StringBuilder();

        grid.append("<div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 25px;\">");

        for (String kpiCard : kpiCards) {
            grid.append(kpiCard);
        }

        grid.append("</div>");

        return grid.toString();
    }

    public static String getKpiStyles() {
        return StyleFactory.createKpiCardStyle();
    }
}
