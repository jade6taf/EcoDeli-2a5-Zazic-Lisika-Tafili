package com.ecodeli.analytics.ui.styles;

import javafx.scene.paint.Color;

public class ColorPalette {

    public static final String WHITE_PRIMARY = "#FFFFFF";
    public static final String GREEN_PRIMARY = "#2E7D32";
    public static final String GREEN_MODERN = "#4CAF50";
    public static final String GREEN_SOFT = "#66BB6A";
    public static final String GREEN_SUCCESS = "#81C784";
    public static final String GREEN_SUBTLE = "#E8F5E8";
    public static final String GRAY_ELEGANT = "#F5F5F5";
    public static final String GRAY_LIGHT = "#EEEEEE";
    public static final String TEXT_DARK = "#1B5E20";
    public static final String TEXT_MEDIUM = "#388E3C";
    public static final String BLUE_ACCENT = "#E3F2FD";
    public static final String ORANGE_ACCENT = "#FFF3E0";
    public static final String PURPLE_ACCENT = "#F3E5F5";
    public static final String WARNING = "#FF9800";
    public static final String ERROR = "#F44336";
    public static final String INFO = "#2196F3";

    public static Color toFxColor(String hexColor) {
        return Color.web(hexColor);
    }

    public static String getPrimary() {
        return GREEN_PRIMARY;
    }

    public static String getBackground() {
        return WHITE_PRIMARY;
    }

    public static String getTextPrimary() {
        return TEXT_DARK;
    }

    public static String getGreenByIntensity(int intensity) {
        if (intensity >= 80) return GREEN_PRIMARY;
        if (intensity >= 60) return GREEN_MODERN;
        if (intensity >= 40) return GREEN_SOFT;
        if (intensity >= 20) return GREEN_SUCCESS;
        return GREEN_SUBTLE;
    }
}
