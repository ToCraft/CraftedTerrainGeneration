package dev.tocraft.crafted.ctgen.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.awt.*;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public record Zone(Holder<Biome> biome, int color, Block deepslateBlock, Block stoneBlock, Block dirtBlock,
                   Block surfaceBlock, int height, double perlinMultiplier, double pixelWeight,
                   Optional<Integer> thresholdModifier) {

    private Zone(Holder<Biome> biome, Color color, ResourceLocation deepslateBlock, ResourceLocation stoneBlock, ResourceLocation dirtBlock, ResourceLocation surfaceBlock, int height, double perlinMultiplier, double pixelWeight, Optional<Integer> thresholdModifier) {
        this(biome, color.getRGB(), BuiltInRegistries.BLOCK.get(deepslateBlock), BuiltInRegistries.BLOCK.get(stoneBlock), BuiltInRegistries.BLOCK.get(dirtBlock), BuiltInRegistries.BLOCK.get(surfaceBlock), height, perlinMultiplier, pixelWeight, thresholdModifier);
    }

    public static final Block DEFAULT_DEEPSLATE_BLOCK = Blocks.DEEPSLATE;
    public static final Block DEFAULT_STONE_BLOCK = Blocks.STONE;
    public static final Block DEFAULT_DIRT_BLOCK = Blocks.DIRT;
    public static final Block DEFAULT_SURFACE_BLOCK = Blocks.GRASS_BLOCK;
    public static final int DEFAULT_HEIGHT = 0;
    public static final double DEFAULT_PERLIN_MULTIPLIER = 8;
    public static final double DEFAULT_PIXEL_WEIGHT = 1;

    private static final Codec<Color> COLOR_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("r").forGetter(Color::getRed),
            Codec.INT.fieldOf("g").forGetter(Color::getGreen),
            Codec.INT.fieldOf("b").forGetter(Color::getBlue)
    ).apply(instance, instance.stable(Color::new)));

    public static final Codec<Zone> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(Zone::biome),
            COLOR_CODEC.fieldOf("color").forGetter(zone -> new Color(zone.color)),
            ResourceLocation.CODEC.optionalFieldOf("deepslate_block", BuiltInRegistries.BLOCK.getKey(DEFAULT_DEEPSLATE_BLOCK)).forGetter(o -> BuiltInRegistries.BLOCK.getKey(o.deepslateBlock)),
            ResourceLocation.CODEC.optionalFieldOf("stone_block", BuiltInRegistries.BLOCK.getKey(DEFAULT_STONE_BLOCK)).forGetter(o -> BuiltInRegistries.BLOCK.getKey(o.stoneBlock)),
            ResourceLocation.CODEC.optionalFieldOf("dirt_block", BuiltInRegistries.BLOCK.getKey(DEFAULT_DIRT_BLOCK)).forGetter(o -> BuiltInRegistries.BLOCK.getKey(o.dirtBlock)),
            ResourceLocation.CODEC.optionalFieldOf("surface_block", BuiltInRegistries.BLOCK.getKey(DEFAULT_SURFACE_BLOCK)).forGetter(o -> BuiltInRegistries.BLOCK.getKey(o.surfaceBlock)),
            Codec.INT.optionalFieldOf("height", DEFAULT_HEIGHT).forGetter(Zone::height),
            Codec.DOUBLE.optionalFieldOf("perlin_multiplier", DEFAULT_PERLIN_MULTIPLIER).forGetter(Zone::perlinMultiplier),
            Codec.DOUBLE.optionalFieldOf("pixel_weight", DEFAULT_PIXEL_WEIGHT).forGetter(Zone::pixelWeight),
            Codec.INT.optionalFieldOf("threshold_modifier").forGetter(Zone::thresholdModifier)
    ).apply(instance, instance.stable(Zone::new)));

    public static RegistryFileCodec<Zone> CODEC = RegistryFileCodec.create(CTerrainGeneration.MAP_ZONES_REGISTRY, DIRECT_CODEC);
}
