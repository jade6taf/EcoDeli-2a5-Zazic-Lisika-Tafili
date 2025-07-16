package com.ecodeli.analytics.ui.styles;

import java.awt.*;

public class ChartColors {

    public static final Color BLUE_CORPORATE = new Color(41, 98, 255);
    public static final Color ORANGE_MODERN = new Color(255, 111, 0);
    public static final Color PURPLE_ELEGANT = new Color(124, 77, 255);
    public static final Color TEAL_PROFESSIONAL = new Color(0, 137, 123);
    public static final Color RED_ACCENT = new Color(244, 67, 54);
    public static final Color GREEN_ECODELI = new Color(46, 125, 50);
    public static final Color AMBER_WARM = new Color(255, 193, 7);
    public static final Color INDIGO_DEEP = new Color(63, 81, 181);
    public static final Color PINK_MODERN = new Color(236, 64, 122);
    public static final Color CYAN_VIBRANT = new Color(0, 188, 212);

    public static Color[] getProfessionalPalette() {
        return new Color[] {
            BLUE_CORPORATE,
            ORANGE_MODERN,
            PURPLE_ELEGANT,
            TEAL_PROFESSIONAL,
            RED_ACCENT,
            GREEN_ECODELI
        };
    }

    public static Color[] getExtendedPalette() {
        return new Color[] {
            BLUE_CORPORATE,
            ORANGE_MODERN,
            PURPLE_ELEGANT,
            TEAL_PROFESSIONAL,
            RED_ACCENT,
            AMBER_WARM,
            GREEN_ECODELI,
            INDIGO_DEEP,
            PINK_MODERN,
            CYAN_VIBRANT
        };
    }

    public static Color[] getPiePalette() {
        return new Color[] {
            BLUE_CORPORATE,
            ORANGE_MODERN,
            PURPLE_ELEGANT,
            TEAL_PROFESSIONAL,
            AMBER_WARM,
            RED_ACCENT
        };
    }

    public static Color[] getBarPalette() {
        return new Color[] {
            BLUE_CORPORATE,
            TEAL_PROFESSIONAL,
            PURPLE_ELEGANT,
            ORANGE_MODERN,
            GREEN_ECODELI
        };
    }

    public static Color getColorByIndex(int index) {
        Color[] palette = getProfessionalPalette();
        return palette[index % palette.length];
    }

    public static Color getLighterColor(Color color) {
        int r = Math.min(255, color.getRed() + 30);
        int g = Math.min(255, color.getGreen() + 30);
        int b = Math.min(255, color.getBlue() + 30);
        return new Color(r, g, b);
    }

    public static Color getDarkerColor(Color color) {
        int r = Math.max(0, color.getRed() - 30);
        int g = Math.max(0, color.getGreen() - 30);
        int b = Math.max(0, color.getBlue() - 30);
        return new Color(r, g, b);
    }
}
