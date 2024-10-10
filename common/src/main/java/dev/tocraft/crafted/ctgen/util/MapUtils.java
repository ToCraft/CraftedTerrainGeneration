package dev.tocraft.crafted.ctgen.util;

import dev.tocraft.crafted.ctgen.biome.Zone;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
public class MapUtils {
    public static BufferedImage approachColors(final BufferedImage inImage, final Iterable<Color> colors) {
        BufferedImage output = new BufferedImage(inImage.getWidth(), inImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < output.getWidth(); x++) {
            for (int y = 0; y < output.getHeight(); y++) {
                Color in = new Color(inImage.getRGB(x, y));
                Color out = ColorUtils.getNearestColor(in, colors);
                if (out != null) {
                    output.setRGB(x, y, out.getRGB());
                }
            }
        }

        return output;
    }

    private static final int[][] diagonal = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
    private static final int[][] orthogonal = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};

    public static BufferedImage generateBiomeMap(final BufferedImage original, Iterable<Zone> biomes) {
        final BufferedImage greatMap = new BufferedImage(original.getWidth() * 2, original.getHeight() * 2, original.getType());
        // seed is 0 so the map is always the same
        final Random random = new Random(0);

        // set each forth pixel
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                greatMap.setRGB(x * 2, y * 2, original.getRGB(x, y));
            }
        }

        // define missing pixels diagonally
        for (int x = 1; x < greatMap.getWidth(); x += 2) {
            for (int y = 1; y < greatMap.getHeight(); y += 2) {
                ArrayList<Integer> nearColors = new ArrayList<>();
                for (int[] direction : diagonal) {
                    int x1 = x + direction[0];
                    int y1 = y + direction[1];

                    if (isInBound(x1, y1, greatMap.getWidth(), greatMap.getHeight())) {
                        nearColors.add(greatMap.getRGB(x1, y1));
                    }
                }
                int out = getMostWeightColor(biomes, nearColors, random);
                greatMap.setRGB(x, y, out);
            }
        }

        // define missing pixels orthogonally
        for (int x = 0; x < greatMap.getWidth(); x++) {
            // x % 2 is always 1 or 0, so we can define this as an offset
            for (int y = 1 - (x % 2); y < greatMap.getHeight(); y += 2) {
                ArrayList<Integer> nearColors = new ArrayList<>();
                for (int[] direction : orthogonal) {
                    int x1 = x + direction[0];
                    int y1 = y + direction[1];

                    if (isInBound(x1, y1, greatMap.getWidth(), greatMap.getHeight())) {
                        nearColors.add(greatMap.getRGB(x1, y1));
                    }
                }

                int out = getMostWeightColor(biomes, nearColors, random);
                greatMap.setRGB(x, y, out);
            }
        }
        return greatMap;
    }

    private static boolean isInBound(int x, int y, int width, int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private static int getMostWeightColor(Iterable<Zone> biomes, List<Integer> list, Random random) {
        double totalWeight = 0;
        for (int color : list) {
            totalWeight += getByColor(biomes, color).pixelWeight();
        }
        double randomWeight = random.nextDouble(totalWeight);
        double currentWeight = 0;
        for (int color : list) {
            currentWeight += getByColor(biomes, color).pixelWeight();
            if (currentWeight >= randomWeight) {
                return color;
            }
        }

        throw new RuntimeException("no valid color could be found!");
    }

    private static Zone getByColor(Iterable<Zone> biomes, int color) {
        for (Zone biome : biomes) {
            if (biome.color() == color) {
                return biome;
            }
        }
        throw new IllegalArgumentException("No biome for color " + color + " was found.");
    }
}
