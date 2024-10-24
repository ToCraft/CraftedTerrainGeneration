package dev.tocraft.crafted.ctgen.zone;

import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.blockplacer.NoisePlacer;
import dev.tocraft.crafted.ctgen.util.Noise;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class Zones {
    // Northern Continent
    public static final ResourceKey<Zone> STONY_FLATS = getKey("stony_flats");
    public static final ResourceKey<Zone> SNOWY_FLATS = getKey("snowy_flats");
    public static final ResourceKey<Zone> SNOWY_SLOPES = getKey("snowy_slopes");
    public static final ResourceKey<Zone> SNOWY_MOUNTAINS = getKey("snowy_mountains");
    public static final ResourceKey<Zone> FROZEN_LAKE = getKey("frozen_lake");
    public static final ResourceKey<Zone> FROZEN_RIVER = getKey("frozen_river");
    // Eastern Continent
    public static final ResourceKey<Zone> PLAINS = getKey("plains");
    public static final ResourceKey<Zone> FOREST = getKey("forest");
    public static final ResourceKey<Zone> HILLS = getKey("hills");
    public static final ResourceKey<Zone> MOUNTAINS = getKey("mountains");
    public static final ResourceKey<Zone> LAKE = getKey("lake");
    // Western Continent
    public static final ResourceKey<Zone> DESERT = getKey("desert");
    public static final ResourceKey<Zone> BADLANDS = getKey("badlands");
    public static final ResourceKey<Zone> BADLANDS_MOUNTAINS = getKey("badlands_mountains");
    // General Water Biomes
    public static final ResourceKey<Zone> RIVER = getKey("river");
    public static final ResourceKey<Zone> OCEAN = getKey("ocean");
    public static final ResourceKey<Zone> DEEP_OCEAN = getKey("deep_ocean");


    public static void bootstrap(BootstapContext<Zone> context) {
        // Northern Continent
        context.register(STONY_FLATS, new ZoneBuilder().setBiome(getBiome(context, Biomes.STONY_SHORE)).setColor(new Color(130, 140, 130)).setDirtBlock(Blocks.STONE).setSurfaceBlock(Blocks.STONE).setHeight(12).build());
        context.register(SNOWY_FLATS, new ZoneBuilder().setBiome(getBiome(context, Biomes.SNOWY_SLOPES)).setColor(new Color(217, 217, 217)).setHeight(11).build());
        context.register(SNOWY_SLOPES, new ZoneBuilder().setBiome(getBiome(context, Biomes.SNOWY_SLOPES)).setColor(new Color(192, 192, 192)).setDirtBlock(Blocks.SNOW_BLOCK).setSurfaceBlock(Blocks.SNOW_BLOCK).setHeight(24).setPerlinMultiplier(20).setPixelWeight(1.5).build());
        context.register(SNOWY_MOUNTAINS, new ZoneBuilder().setBiome(getBiome(context, Biomes.JAGGED_PEAKS)).setColor(new Color(168, 168, 168)).setHeight(60).setPerlinMultiplier(28).setSurfaceBlock(Blocks.SNOW_BLOCK).build());
        context.register(FROZEN_RIVER, new ZoneBuilder().setBiome(getBiome(context, Biomes.FROZEN_RIVER)).setColor(new Color(87, 145, 240)).setDirtBlock(Blocks.SAND).setSurfaceBlock(Blocks.SAND).setHeight(-15).setPixelWeight(2).setThresholdModifier(26).build());
        context.register(FROZEN_LAKE, new ZoneBuilder().setBiome(getBiome(context, Biomes.FROZEN_OCEAN)).setColor(new Color(78, 126, 204)).setSurfaceBlock(Blocks.SAND).setHeight(-20).setPixelWeight(3).setThresholdModifier(26).build());
        // Eastern Continent
        context.register(PLAINS, new ZoneBuilder().setBiome(getBiome(context, Biomes.PLAINS)).setColor(new Color(57, 95, 57)).setHeight(6).setPerlinMultiplier(4).build());
        context.register(FOREST, new ZoneBuilder().setBiome(getBiome(context, Biomes.FOREST)).setColor(new Color(43, 70, 43)).setHeight(8).setPerlinMultiplier(6).build());
        context.register(HILLS, new ZoneBuilder().setBiome(getBiome(context, Biomes.WINDSWEPT_GRAVELLY_HILLS)).setColor(new Color(151, 151, 151)).setHeight(25).setPerlinMultiplier(18).build());
        context.register(MOUNTAINS, new ZoneBuilder().setBiome(getBiome(context, Biomes.STONY_PEAKS)).setColor(new Color(130, 130, 130)).setDirtBlock(Blocks.STONE).setSurfaceBlock(Blocks.STONE).setHeight(50).setPerlinMultiplier(28).build());
        context.register(LAKE, new ZoneBuilder().setBiome(getBiome(context, Biomes.LUKEWARM_OCEAN)).setColor(new Color(0, 83, 217)).setSurfaceBlock(Blocks.SAND).setHeight(-20).setPixelWeight(3).build());
        // Western Continent
        context.register(DESERT, new ZoneBuilder().setBiome(getBiome(context, Biomes.DESERT)).setColor(new Color(165, 171, 54)).setDirtBlock(Blocks.SANDSTONE).setSurfaceBlock(Blocks.SAND).setHeight(5).setPerlinMultiplier(4).build());
        context.register(BADLANDS, new ZoneBuilder().setBiome(getBiome(context, Biomes.BADLANDS)).setColor(new Color(84, 84, 56)).setDirtBlock(Blocks.RED_CONCRETE).setSurfaceBlock(Blocks.BROWN_CONCRETE).setHeight(18).setPerlinMultiplier(12).build());
        context.register(BADLANDS_MOUNTAINS, new ZoneBuilder().setBiome(getBiome(context, Biomes.BADLANDS)).setColor(new Color(70, 71, 53)).setDirtBlock(Blocks.RED_CONCRETE).putLayer("surface", NoisePlacer.of(new Noise(List.of(1f), 1, 1), new HashMap<>() {
            {
                put(-0.5d, Blocks.ORANGE_CONCRETE);
                put(0d, Blocks.RED_CONCRETE);
            }
        }, Blocks.BROWN_CONCRETE)).setHeight(28).setPerlinMultiplier(24).build());
        // General Water Biomes
        context.register(RIVER, new ZoneBuilder().setBiome(getBiome(context, Biomes.RIVER)).setColor(new Color(1, 98, 255)).setDirtBlock(Blocks.SAND).setSurfaceBlock(Blocks.SAND).setHeight(-15).setPixelWeight(2).setThresholdModifier(26).build());
        context.register(OCEAN, new ZoneBuilder().setBiome(getBiome(context, Biomes.OCEAN)).setColor(new Color(0, 42, 103)).setSurfaceBlock(Blocks.SAND).setHeight(-35).setPerlinMultiplier(16).setThresholdModifier(26).build());
        context.register(DEEP_OCEAN, new ZoneBuilder().setBiome(getBiome(context, Biomes.DEEP_OCEAN)).setColor(new Color(0, 35, 85)).setHeight(-60).setPerlinMultiplier(33).setThresholdModifier(26).build());
    }

    private static Holder<Biome> getBiome(BootstapContext<?> context, ResourceKey<Biome> biome) {
        return context.lookup(Registries.BIOME).getOrThrow(biome);
    }

    private static ResourceKey<Zone> getKey(String name) {
        return ResourceKey.create(CTerrainGeneration.MAP_ZONES_REGISTRY, CTerrainGeneration.id(name));
    }
}
