package com.ecodeli.analytics.ui.components;

import com.ecodeli.analytics.ui.styles.Styles;
import com.ecodeli.analytics.ui.styles.UIColors;

/**
 * Composants KPI ultra-simplifiés
 */
public class SimpleKPI {

    public static String create(String icon, String value, String label) {
        return "<div style=\"" + Styles.card() + " padding: 30px; text-align: center;\">" +
               "<div style=\"font-size: 28px;\">" + icon + "</div>" +
               "<div style=\"font-size: 32px; font-weight: bold; color: " + UIColors.GREEN_MAIN + ";\">" + value + "</div>" +
               "<div style=\"font-size: 14px; color: " + UIColors.TEXT_MEDIUM + ";\">" + label + "</div>" +
               "</div>";
    }

    public static String createGrid(String... kpis) {
        StringBuilder grid = new StringBuilder();
        grid.append("<div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 25px;\">");
        for (String kpi : kpis) {
            grid.append(kpi);
        }
        grid.append("</div>");
        return grid.toString();
    }

}
