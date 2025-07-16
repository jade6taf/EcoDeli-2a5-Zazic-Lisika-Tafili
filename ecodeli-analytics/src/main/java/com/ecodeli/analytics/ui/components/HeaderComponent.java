package com.ecodeli.analytics.ui.components;

import com.ecodeli.analytics.ui.styles.ColorPalette;
import com.ecodeli.analytics.ui.styles.StyleFactory;

public class HeaderComponent {

    public static String createMainHeader() {
        StringBuilder header = new StringBuilder();

        header.append("<div style=\"").append(StyleFactory.createHeaderStyle()).append("\">");

        header.append("<div style=\"display: flex; align-items: center; gap: 15px;\">");

        header.append("<span style=\"").append(StyleFactory.createEmojiIconStyle())
              .append(" color: ").append(ColorPalette.GREEN_SUCCESS).append(";\">")
              .append("🌱").append("</span>");

        header.append("<div>");
        header.append("<h1 style=\"").append(StyleFactory.createMainTitleStyle()).append("\">")
              .append("EcoDeli Analytics</h1>");
        header.append("<p style=\"").append(StyleFactory.createSubtitleStyle())
              .append(" color: ").append(ColorPalette.GREEN_SUBTLE).append(";\">")
              .append("Dashboard des Performances</p>");
        header.append("</div>");

        header.append("</div>");
        header.append("</div>");

        return header.toString();
    }

    public static String createFiltersBar() {
        StringBuilder filters = new StringBuilder();

        filters.append("<div style=\"padding: 15px 20px; background-color: ")
               .append(ColorPalette.WHITE_PRIMARY).append("; border-bottom: 2px solid ")
               .append(ColorPalette.GREEN_SOFT).append(";\">");


        filters.append("<div style=\"display: flex; gap: 10px; align-items: center;\">");
        filters.append("<label style=\"").append(StyleFactory.createKpiLabelStyle())
               .append("\">Période:</label>");
        filters.append("<select style=\"").append(StyleFactory.createComboBoxStyle()).append("\">");
        filters.append("<option>Mois actuel</option>");
        filters.append("<option>Trimestre</option>");
        filters.append("<option>Semestre</option>");
        filters.append("<option>Année</option>");
        filters.append("</select>");
        filters.append("</div>");
        
        // Filtre catégorie
        filters.append("<div style=\"display: flex; gap: 10px; align-items: center;\">");
        filters.append("<label style=\"").append(StyleFactory.createKpiLabelStyle())
               .append("\">Catégorie:</label>");
        filters.append("<select style=\"").append(StyleFactory.createComboBoxStyle()).append("\">");
        filters.append("<option>Toutes</option>");
        filters.append("<option>Clients</option>");
        filters.append("<option>Commerçants</option>");
        filters.append("<option>Livreurs</option>");
        filters.append("<option>Prestations</option>");
        filters.append("</select>");
        filters.append("</div>");

        filters.append("<div style=\"margin-left: auto;\">");
        filters.append("<span style=\"").append(StyleFactory.createKpiLabelStyle())
               .append(" color: ").append(ColorPalette.GREEN_SUCCESS).append(";\">")
               .append("Prêt</span>");
        filters.append("</div>");
        filters.append("</div>");
        filters.append("</div>");

        return filters.toString();
    }

    public static String createModalHeader(String title) {
        StringBuilder header = new StringBuilder();

        header.append("<div style=\"padding: 20px; background-color: ")
               .append(ColorPalette.GREEN_PRIMARY).append("; border-radius: 15px 15px 0 0;\">");

        header.append("<h2 style=\"").append(StyleFactory.createMainTitleStyle())
               .append(" font-size: 24px; margin: 0;\">").append(title).append("</h2>");

        header.append("</div>");

        return header.toString();
    }

    public static String getHeaderStyles() {
        return StyleFactory.createHeaderStyle();
    }


    public static boolean isLogoReady() {
        return false;
    }
}
