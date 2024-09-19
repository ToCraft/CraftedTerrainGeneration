package dev.tocraft.crafted.ctgen.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class Main {
    private static final Option INPUT_OPTION = Option.builder().option("i").longOpt("input").hasArg().desc("Input map image").build();
    private static final Option BIOMES_OPTION = Option.builder().option("b").longOpt("biomes").hasArg().desc("Biomes directory").build();
    private static final Option OUTPUT_OPTION = Option.builder().option("o").longOpt("output").hasArg().desc("Output map image").build();
    private static final Options OPTIONS = new Options().addOption(INPUT_OPTION).addOption(BIOMES_OPTION).addOption(OUTPUT_OPTION);

    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

    public static void main(String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(OPTIONS, args);

            if (!cmd.hasOption(INPUT_OPTION) || !cmd.hasOption(BIOMES_OPTION) || !cmd.hasOption(OUTPUT_OPTION)) {
                HelpFormatter help = new HelpFormatter();
                help.printHelp("java -jar CTGen.jar", "Tool for upscaling map images so they can be properly read by the Crafted Terrain Generation Mod (CTGen.", OPTIONS, "Copyright (c) 2024 To_Craft. Licensed under the Crafted License 1.0", true);
                System.exit(-1);
            }

            String input = cmd.getOptionValue(INPUT_OPTION);
            String biomes = cmd.getOptionValue(BIOMES_OPTION);
            String output = cmd.getOptionValue(OUTPUT_OPTION);

            MapProcessor processor = new MapProcessor(input, biomes, output);
            processor.run();
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class MapProcessor {
        private final Path input;
        private final Path biomes;
        private final Path output;

        public MapProcessor(String input, String biomes, String output) {
            this.input = Path.of(input);
            this.biomes = Path.of(biomes);
            this.output = Path.of(output);
        }

        public void run() throws IOException {

        }
    }
}
