package dev.tocraft.ctgen.runtime;

import dev.tocraft.cli.CmdLineBuilder;
import dev.tocraft.cli.CommandLine;
import dev.tocraft.cli.Option;
import dev.tocraft.cli.OptionBuilder;
import dev.tocraft.cli.json.JsonParser;
import dev.tocraft.cli.json.elements.JsonElement;
import dev.tocraft.cli.json.elements.JsonString;
import dev.tocraft.ctgen.util.MapUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class Main {
    private static final String JAR_FILE_NAME = Path.of(Main.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .getPath()).getFileName().toString();

    private static final String CMD_BASE = "java -jar " + JAR_FILE_NAME;
    private static final String DESCRIPTION = "Tool for upscaling map images so they can be properly read by\nthe Crafted Terrain Generation Mod (CTGen).\nEvery image file path must end with \".png\"";
    private static final String COPYRIGHT = "Copyright (c) 2024 To_Craft. Licensed under the Crafted License 1.0";
    private static final Option INPUT = new OptionBuilder().setAbbreviation("-i").addAlias("--input").setDescription("Input map image, no alpha supported!").create();
    private static final Option ZONES = new OptionBuilder().setAbbreviation("-z").addAlias("--zones").setDescription("directory containing the zones as json files").create();
    private static final Option OUTPUT = new OptionBuilder().setAbbreviation("-o").addAlias("--output").setDescription("Output map image").create();
    private static final Option CORRECTED = new OptionBuilder().setAbbreviation("-c").addAlias("--corrected").setDescription("Optional file to save an image with only the corrected colors").setRequired(false).create();
    private static final Option ONLY_CHANGED = new OptionBuilder().setAbbreviation("-oc").addAlias("--only-changed").setDescription("Works only in addition to --corrected, the corrected image will only show the changed pixel.").setTakesInput(false).setRequired(false).create();
    private static final CommandLine CMDLINE = new CmdLineBuilder().setCmdBase(CMD_BASE).setHeader(DESCRIPTION).setFooter(COPYRIGHT).addOptions(INPUT, ZONES, OUTPUT, CORRECTED, ONLY_CHANGED).create();

    public static void main(String[] args) {
        Map<Option, String> input = CMDLINE.parseArgs(args);

        try {
            if (input == null || (input.containsKey(ONLY_CHANGED) && !input.containsKey(CORRECTED))) {
                throw new IOException();
            }

            long startTime = System.currentTimeMillis();

            Preparer preparer = new Preparer(input.get(INPUT), input.get(ZONES), input.get(OUTPUT), input.get(CORRECTED), input.containsKey(ONLY_CHANGED));
            Runner runner = preparer.run();
            System.out.println("Successfully read all input files. Continuing with processing.");
            runner.run();

            // output duration of the code
            float duration = (float) ((System.currentTimeMillis() - startTime) / 1000.0);
            System.out.println("Program finished within " + duration + " seconds.");
        } catch (IOException e) {
            System.out.println(CMDLINE.getHelpPage());
            System.err.println("Caught an error while parsing the args.");
            String msg = e.getMessage();
            if (msg != null && !msg.isBlank()) {
                System.err.println(msg);
            }
            System.exit(0);
        }
    }

    private static class Preparer {
        private final File input;
        private final Path zones;
        private final File output;
        private final File corrected;
        private final boolean onlyChanged;

        public Preparer(String input, String zones, String output, String corrected, boolean onlyChanged) {
            this.input = new File(input);
            this.zones = Path.of(zones);
            this.output = new File(output);
            this.corrected = corrected == null ? null : new File(corrected);
            this.onlyChanged = onlyChanged;
        }

        // read input files and parse as something readable
        @Contract(" -> new")
        public @NotNull Runner run() throws IOException {
            BufferedImage inImage = ImageIO.read(input);

            List<JsonZone> zones = new ArrayList<>();

            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(this.zones)) {
                for (Path zoneFile : dirStream) {
                    if (!zoneFile.toString().endsWith("json")) {
                        throw new IOException("Invalid file " + zoneFile + " in zone directory!");
                    }

                    String json = Files.readString(zoneFile);
                    Color color;
                    {
                        JsonElement colorElement = JsonParser.parseJson(json).asObject().get("color");
                        if (colorElement instanceof JsonString s) {
                            color = new Color(((0xFF) << 24) | Integer.decode(s.get()));
                        } else {
                            Map<String, JsonElement> colorMap = colorElement.asObject();
                            int r = colorMap.get("red").asInt().get();
                            int g = colorMap.get("green").asInt().get();
                            int b = colorMap.get("blue").asInt().get();
                            color = new Color(r, g, b);
                        }
                    }
                    JsonElement pixelWeight = JsonParser.parseJson(json).asObject().get("pixel_weight");
                    JsonZone zone = new JsonZone(color.getRGB(), pixelWeight != null ? pixelWeight.asDouble().get() : 1.0);
                    zones.add(zone);
                }
            }

            return new Runner(inImage, zones, output, corrected, onlyChanged);
        }
    }

    private record Runner(BufferedImage original, List<JsonZone> zones, File output, File corrected,
                          boolean onlyChanged) {
        // actual map generation logic
        public void run() throws IOException {
            List<Color> colors = zones.stream().map(zone -> new Color(zone.color)).toList();
            BufferedImage corrected = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
            corrected.setData(original.copyData(null));
            int count = MapUtils.approachColors(original, corrected, colors);
            // optionally save the corrected image
            if (this.corrected != null) {
                BufferedImage outCorr;
                if (onlyChanged) {
                    outCorr = new BufferedImage(corrected.getWidth(), corrected.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    for (int x = 0; x < corrected.getWidth(); x++) {
                        for (int y = 0; y < corrected.getHeight(); y++) {
                            int c = corrected.getRGB(x, y);
                            int o = original.getRGB(x, y);
                            if (c != o) {
                                outCorr.setRGB(x, y, c);
                            }
                        }
                    }
                } else {
                    outCorr = corrected;
                }
                ImageIO.write(outCorr, "PNG", this.corrected);
            }
            System.out.println("Corrected the color for " + count + " pixels.");

            BufferedImage outMap = MapUtils.generateDetailedMap(corrected, color -> {
                for (JsonZone zone : zones) {
                    if (zone.color() == color) {
                        return zone.pixelWeight();
                    }
                }

                throw new IllegalArgumentException("No zone for color " + color + " was found.");
            });
            System.out.println("Generated Map Image with estimated " + (outMap.getWidth() * outMap.getHeight() * 3 / 1048576) + "MB.");
            ImageIO.write(outMap, "PNG", output);
        }
    }

    private record JsonZone(int color, double pixelWeight) {
    }
}
