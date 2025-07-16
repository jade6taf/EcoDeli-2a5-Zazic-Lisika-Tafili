package com.ecodeli.analytics.ui.styles;


public class Styles {
    public static String background() {
        return "-fx-background-color: " + UIColors.WHITE + ";";
    }

    public static String header() {
        return "-fx-background-color: " + UIColors.GREEN_MAIN + "; " +
               "-fx-padding: 20; -fx-spacing: 15;";
    }

    public static String card() {
        return "-fx-background-color: " + UIColors.WHITE + "; " +
               "-fx-border-color: " + UIColors.GREEN_SOFT + "; " +
               "-fx-border-width: 2; -fx-border-radius: 15; " +
               "-fx-background-radius: 15;";
    }

    public static String button() {
        return "-fx-background-color: " + UIColors.GREEN_MAIN + "; " +
               "-fx-text-fill: " + UIColors.WHITE + "; " +
               "-fx-font-size: 14; -fx-padding: 12 25; " +
               "-fx-border-radius: 8; -fx-background-radius: 8;";
    }

    public static String title() {
        return "-fx-font-family: 'Segoe UI'; -fx-font-size: 24; " +
               "-fx-font-weight: bold; -fx-text-fill: " + UIColors.GREEN_MAIN + ";";
    }

}
