package dev.tocraft.crafted.ctgen.runtime;

import dev.tocraft.crafted.ctgen.util.MapUtils;
import org.jetbrains.annotations.Nullable;

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

@SuppressWarnings("unused")
public class Main {
    private static final String HELP_PAGE = """
            usage: java -jar CTGen.jar -z <arg> -i <arg> -o <arg> [-w <arg>] [-c <arg>]
            Tool for upscaling map images so they can be properly read by the Crafted
            Terrain Generation Mod (CTGen).
             -z,--zones <arg>       directory containing the zones as json files
             -i,--original <arg>    Input map image, no alpha supported!
             -o,--output <arg>      Output map image, must end with ".png"
             -w,--weight <arg>      Default pixel weight, will default to 1
             -c,--corrected <arg>   Optional file to save the input image with corrected colors, must end with ".png"
            Copyright (c) 2024 To_Craft. Licensed under the Crafted License 1.0
            """;

    public static void main(String[] args) {
        List<String> processedArgs = new ArrayList<>();
        StringBuilder input = new StringBuilder();
        StringBuilder zones = new StringBuilder();
        StringBuilder output = new StringBuilder();
        StringBuilder weight = new StringBuilder();
        StringBuilder corrected = new StringBuilder();
        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i];
            switch (arg) {
                case "-i", "--original": {
                    String value = args[i + 1];
                    input.append(value);
                    processedArgs.add(arg);
                    processedArgs.add(value);
                    break;
                }
                case "-z", "--zones": {
                    String value = args[i + 1];
                    zones.append(value);
                    processedArgs.add(arg);
                    processedArgs.add(value);
                    break;
                }
                case "-o", "--output": {
                    String value = args[i + 1];
                    if (!value.endsWith(".png")) {
                        System.err.println("The output file must end with \".png\"");
                        System.exit(0);
                    }
                    output.append(value);
                    processedArgs.add(arg);
                    processedArgs.add(value);
                    break;
                }
                case "-w", "--weight": {
                    String value = args[i + 1];
                    weight.append(value);
                    processedArgs.add(arg);
                    processedArgs.add(value);
                    break;
                }
                case "-c", "--corrected": {
                    String value = args[i + 1];
                    if (!value.endsWith(".png")) {
                        System.err.println("The output file must end with \".png\"");
                        System.exit(0);
                    }
                    corrected.append(value);
                    processedArgs.add(arg);
                    processedArgs.add(value);
                    break;
                }
            }
        }

        // arg is missing or there are too many args
        if (input.isEmpty()  || zones.isEmpty() || output.isEmpty() || new ArrayList<>(List.of(args)).retainAll(processedArgs)) {
            System.out.println(HELP_PAGE);
            System.exit(0);
        }

        try {
            long startTime = System.currentTimeMillis();

            Preparer preparer = new Preparer(input.toString(), zones.toString(), output.toString(), weight.toString(), corrected.toString());
            Runner runner = preparer.run();
            System.out.println("Successfully read all input files. Continuing with processing.");
            runner.run();

            // output duration of the code
            float duration = (float) ((System.currentTimeMillis() - startTime) / 1000.0);
            System.out.println("Program finished within " + duration + " seconds.");
        } catch (IOException e) {
            System.out.println(HELP_PAGE);
            System.err.println("Caught an error while parsing the args.");
            System.err.println(e.getMessage());
            System.exit(0);
        }
    }

    private static class Preparer {
        private final File input;
        private final Path zones;
        private final File output;
        private final double weight;
        @Nullable
        private final File corrected;

        public Preparer(String input, String zones, String output, String weight, String corrected) {
            this.input = new File(input);
            this.zones = Path.of(zones);
            this.output = new File(output);
            this.weight = weight.isBlank() ? 1.0d : Double.parseDouble(weight);
            this.corrected = corrected.isBlank() ? null : new File(corrected);
        }

        // read input files and parse as something readable
        public Runner run() throws IOException {
            BufferedImage inImage = ImageIO.read(input);

            List<JsonZone> zones = new ArrayList<>();

            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(this.zones)) {
                for (Path zoneFile : dirStream) {
                    if (!zoneFile.toString().endsWith("json")) {
                        throw new IOException("Invalid file " + zoneFile + " in zone directory!");
                    }

                    String json = Files.readString(zoneFile);
                    String color = getJsonValue(json, "color");
                    String pixelWeight = getJsonValue(json, "pixel_weight");
                    if (pixelWeight == null) {
                        pixelWeight = String.valueOf(weight);
                    }

                    JsonZone zone = new JsonZone(color, pixelWeight);
                    zones.add(zone);
                }
            }

            return new Runner(inImage, zones, output, corrected);
        }
    }

    private record Runner(BufferedImage original, List<JsonZone> zones, File output, @Nullable File corrected) {
        // actual map generation logic
        public void run() throws IOException {
            List<Color> colors = zones.stream().map(zone -> new Color(zone.color)).toList();
            BufferedImage corrected = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
            int count = MapUtils.approachColors(original, corrected, colors);
            // optionally save the corrected image
            if (this.corrected != null) ImageIO.write(corrected, "PNG", this.corrected);
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
        public JsonZone(String color, String pixelWeight) throws NumberFormatException {
            this(Integer.parseInt(color), Double.parseDouble(pixelWeight));
        }
    }

    @Nullable
    private static String getJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int jsonIndex = json.indexOf(searchKey);
        if (jsonIndex == -1) {
            return null;
        }

        int startIndex = jsonIndex + searchKey.length();
        int endIndex = json.indexOf(",", startIndex);

        if (endIndex == -1) {
            endIndex = json.indexOf("}", startIndex);
        }

        String value = json.substring(startIndex, endIndex).trim();

        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        return value;
    }
}
