package dev.tocraft.ctgen.zone;

import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.xtend.CTRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public final class Zones {
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


    public static void bootstrap(@NotNull BootstrapContext<Zone> context) {
        // Northern Continent
        context.register(STONY_FLATS, stonyFlats(context).build());
        context.register(SNOWY_FLATS, snowyFlats(context).build());
        context.register(SNOWY_SLOPES, snowySlopes(context).build());
        context.register(SNOWY_MOUNTAINS, snowyMountains(context).build());
        context.register(FROZEN_RIVER, frozenRiver(context).build());
        context.register(FROZEN_LAKE, frozenLake(context).build());
        // Eastern Continent
        context.register(PLAINS, plains(context).build());
        context.register(FOREST, forest(context).build());
        context.register(HILLS, hills(context).build());
        context.register(MOUNTAINS, mountains(context).build());
        context.register(LAKE, lake(context).build());
        // Western Continent
        context.register(DESERT, desert(context).build());
        context.register(BADLANDS, badlands(context).build());
        context.register(BADLANDS_MOUNTAINS, badlandMountains(context).build());
        // General Water Biomes
        context.register(RIVER, river(context).build());
        context.register(OCEAN, ocean(context).build());
        context.register(DEEP_OCEAN, deepOcean(context).build());
    }

    private static ZoneBuilder deepOcean(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.DEEP_OCEAN)).setColor(new Color(0, 35, 85)).setHeight(-60).setTerrainModifier(33);
    }

    private static ZoneBuilder ocean(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.OCEAN)).setColor(new Color(0, 42, 103)).setHeight(-35).setTerrainModifier(16);
    }

    private static ZoneBuilder river(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.RIVER)).setColor(new Color(1, 98, 255)).setHeight(-15).setPixelWeight(2);
    }

    public static ZoneBuilder badlandMountains(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.BADLANDS)).setColor(new Color(70, 71, 53)).setHeight(28).setTerrainModifier(24);
    }

    public static ZoneBuilder badlands(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.BADLANDS)).setColor(new Color(84, 84, 56)).setHeight(18).setTerrainModifier(12);
    }

    public static ZoneBuilder desert(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.DESERT)).setColor(new Color(165, 171, 54)).setHeight(5).setTerrainModifier(4);
    }

    public static ZoneBuilder lake(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.LUKEWARM_OCEAN)).setColor(new Color(0, 83, 217)).setHeight(-20).setPixelWeight(3);
    }

    public static ZoneBuilder mountains(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.STONY_PEAKS)).setColor(new Color(130, 130, 130)).setHeight(40).setTerrainModifier(50);
    }

    public static ZoneBuilder hills(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.WINDSWEPT_GRAVELLY_HILLS)).setColor(new Color(151, 151, 151)).setHeight(18).setTerrainModifier(18);
    }

    public static ZoneBuilder forest(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.FOREST)).setColor(new Color(43, 70, 43)).setHeight(12).setTerrainModifier(10);
    }

    public static ZoneBuilder plains(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.PLAINS)).setColor(new Color(57, 95, 57)).setHeight(5);
    }

    public static ZoneBuilder frozenLake(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.FROZEN_OCEAN)).setColor(new Color(78, 126, 204)).setHeight(-20).setPixelWeight(3);
    }

    public static ZoneBuilder frozenRiver(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.FROZEN_RIVER)).setColor(new Color(87, 145, 240)).setHeight(-15).setPixelWeight(2);
    }

    public static ZoneBuilder snowyMountains(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.JAGGED_PEAKS)).setColor(new Color(168, 168, 168)).setHeight(40).setTerrainModifier(50);
    }

    public static ZoneBuilder snowySlopes(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.SNOWY_SLOPES)).setColor(new Color(192, 192, 192)).setHeight(18).setTerrainModifier(20).setPixelWeight(1.5);
    }

    public static ZoneBuilder snowyFlats(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.SNOWY_SLOPES)).setColor(new Color(217, 217, 217)).setHeight(5);
    }

    public static ZoneBuilder stonyFlats(@NotNull BootstrapContext<Zone> context) {
        return new ZoneBuilder().setBiome(getBiome(context, Biomes.STONY_SHORE)).setColor(new Color(130, 140, 130)).setHeight(12);
    }

    public static @NotNull Holder<Biome> getBiome(@NotNull BootstrapContext<?> context, ResourceKey<Biome> biome) {
        return context.lookup(Registries.BIOME).getOrThrow(biome);
    }

    private static @NotNull ResourceKey<Zone> getKey(String name) {
        return ResourceKey.create(CTRegistries.ZONES_KEY, CTerrainGeneration.id(name));
    }
}
