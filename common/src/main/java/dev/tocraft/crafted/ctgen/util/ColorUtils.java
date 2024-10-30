package dev.tocraft.crafted.ctgen.util;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ColorUtils {
    public static int compareColors(@NotNull Color c1, @NotNull Color c2) {
        int a = Math.abs(c1.getAlpha() - c2.getAlpha());
        int r = Math.abs(c1.getRed() - c2.getRed());
        int g = Math.abs(c1.getGreen() - c2.getGreen());
        int b = Math.abs(c1.getBlue() - c2.getBlue());
        return a + r + g + b;
    }

    public static Color getNearestColor(Color color, @NotNull Iterable<Color> colorList) {
        Map<Integer, Color> differences = new HashMap<>();
        for (Color color1 : colorList) {
            int i = compareColors(color, color1);
            differences.put(i, color1);
        }

        int lowestDifference = 765;
        for (Integer i : differences.keySet()) {
            if (i < lowestDifference) {
                lowestDifference = i;
            }
        }
        return differences.get(lowestDifference);
    }
}