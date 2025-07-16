package com.ecodeli.analytics.ui.styles;

public class StyleFactory {

    public static String createMainBackgroundStyle() {
        return "-fx-background-color: " + ColorPalette.WHITE_PRIMARY + ";";
    }

    public static String createHeaderStyle() {
        return "-fx-background-color: " + ColorPalette.GREEN_PRIMARY + "; " +
               "-fx-border-color: " + ColorPalette.GREEN_MODERN + "; " +
               "-fx-border-width: 0 0 2 0; " +
               "-fx-padding: 20; -fx-spacing: 15;";
    }

    public static String createKpiCardStyle() {
        return "-fx-background-color: " + ColorPalette.WHITE_PRIMARY + "; " +
               "-fx-border-color: " + ColorPalette.GREEN_SOFT + "; " +
               "-fx-border-width: 2; " +
               "-fx-border-radius: 15; " +
               "-fx-background-radius: 15; " +
               "-fx-effect: dropshadow(gaussian, rgba(46,125,50,0.15), 8, 0, 0, 2);";
    }

    public static String createChartContainerStyle() {
        return "-fx-background-color: " + ColorPalette.WHITE_PRIMARY + "; " +
               "-fx-border-color: " + ColorPalette.GREEN_SOFT + "; " +
               "-fx-border-width: 2; " +
               "-fx-border-radius: 15; " +
               "-fx-background-radius: 15; " +
               "-fx-effect: dropshadow(gaussian, rgba(46,125,50,0.1), 6, 0, 0, 2);";
    }

    public static String createPrimaryButtonStyle() {
        return "-fx-background-color: " + ColorPalette.GREEN_MODERN + "; " +
               "-fx-text-fill: " + ColorPalette.WHITE_PRIMARY + "; " +
               "-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " +
               "-fx-font-size: 14; " +
               "-fx-font-weight: bold; " +
               "-fx-padding: 12 25; " +
               "-fx-border-radius: 8; " +
               "-fx-background-radius: 8; " +
               "-fx-cursor: hand; " +
               "-fx-effect: dropshadow(gaussian, rgba(46,125,50,0.2), 4, 0, 0, 1);";
    }

    public static String createSecondaryButtonStyle() {
        return "-fx-background-color: " + ColorPalette.GREEN_SUBTLE + "; " +
               "-fx-text-fill: " + ColorPalette.GREEN_PRIMARY + "; " +
               "-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " +
               "-fx-font-size: 14; " +
               "-fx-font-weight: bold; " +
               "-fx-padding: 12 25; " +
               "-fx-border-radius: 8; " +
               "-fx-background-radius: 8; " +
               "-fx-cursor: hand; " +
               "-fx-effect: dropshadow(gaussian, rgba(46,125,50,0.1), 4, 0, 0, 1);";
    }

    public static String createComboBoxStyle() {
        return "-fx-background-color: " + ColorPalette.WHITE_PRIMARY + "; " +
               "-fx-border-color: " + ColorPalette.GREEN_SOFT + "; " +
               "-fx-border-width: 2; " +
               "-fx-border-radius: 8; " +
               "-fx-background-radius: 8; " +
               "-fx-padding: 10 15; " +
               "-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " +
               "-fx-font-size: 14;";
    }

    public static String createSectionTitleStyle() {
        return "-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " +
               "-fx-font-size: 24; " +
               "-fx-font-weight: bold; " +
               "-fx-text-fill: " + ColorPalette.GREEN_PRIMARY + ";";
    }

    public static String createSubtitleStyle() {
        return "-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " +
               "-fx-font-size: 16; " +
               "-fx-font-weight: normal; " +
               "-fx-text-fill: " + ColorPalette.TEXT_MEDIUM + ";";
    }

    public static String createMainTitleStyle() {
        return "-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " +
               "-fx-font-size: 32; " +
               "-fx-font-weight: bold; " +
               "-fx-text-fill: " + ColorPalette.WHITE_PRIMARY + ";";
    }

    public static String createKpiValueStyle() {
        return "-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " +
               "-fx-font-size: 32; " +
               "-fx-font-weight: bold; " +
               "-fx-text-fill: " + ColorPalette.GREEN_PRIMARY + ";";
    }

    public static String createKpiLabelStyle() {
        return "-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " +
               "-fx-font-size: 14; " +
               "-fx-font-weight: normal; " +
               "-fx-text-fill: " + ColorPalette.TEXT_MEDIUM + ";";
    }

    public static String createFooterStyle() {
        return "-fx-background-color: " + ColorPalette.WHITE_PRIMARY + "; " +
               "-fx-border-color: " + ColorPalette.GREEN_SOFT + "; " +
               "-fx-border-width: 2 0 0 0;";
    }

    public static String createModalStyle() {
        return "-fx-background-color: " + ColorPalette.WHITE_PRIMARY + "; " +
               "-fx-border-color: " + ColorPalette.GREEN_SOFT + "; " +
               "-fx-border-width: 2; " +
               "-fx-border-radius: 15; " +
               "-fx-background-radius: 15;";
    }

    public static String createEmojiIconStyle() {
        return "-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', sans-serif; " +
               "-fx-font-size: 28; " +
               "-fx-font-weight: normal;";
    }
}
