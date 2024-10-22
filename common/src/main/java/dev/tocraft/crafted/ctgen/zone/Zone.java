package dev.tocraft.crafted.ctgen.zone;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.util.NoiseSelector;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.awt.*;
import java.util.Optional;

public record Zone(Holder<Biome> biome, int color, NoiseSelector<Block> deepslateBlock, NoiseSelector<Block> stoneBlock, NoiseSelector<Block> dirtBlock,
                   NoiseSelector<Block> surfaceBlock, int height, double perlinMultiplier, double pixelWeight,
                   Optional<Integer> thresholdModifier) {

    public static final NoiseSelector<Block> DEFAULT_DEEPSLATE_BLOCK = NoiseSelector.of(Blocks.DEEPSLATE);
    public static final NoiseSelector<Block> DEFAULT_STONE_BLOCK = NoiseSelector.of(Blocks.STONE);
    public static final NoiseSelector<Block> DEFAULT_DIRT_BLOCK = NoiseSelector.of(Blocks.DIRT);
    public static final NoiseSelector<Block> DEFAULT_SURFACE_BLOCK = NoiseSelector.of(Blocks.GRASS_BLOCK);
    public static final int DEFAULT_HEIGHT = 0;
    public static final double DEFAULT_PERLIN_MULTIPLIER = 8;
    public static final double DEFAULT_PIXEL_WEIGHT = 1;

    public static final Codec<Color> COLOR_DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("r").forGetter(Color::getRed),
            Codec.INT.fieldOf("g").forGetter(Color::getGreen),
            Codec.INT.fieldOf("b").forGetter(Color::getBlue)
    ).apply(instance, instance.stable(Color::new)));

    public static final Codec<Integer> COLOR_CODEC = Codec.either(COLOR_DIRECT_CODEC, Codec.INT)
            .xmap(either -> either.right().orElseGet(() -> either.left().orElseThrow().getRGB()), rgb -> {
                Color color = new Color(rgb);
                if (color.getAlpha() < 255) {
                    return Either.right(rgb);
                } else {
                    return Either.left(color);
                }
            });

    public static final Codec<Block> BLOCK_CODEC = ResourceLocation.CODEC.comapFlatMap(id -> BuiltInRegistries.BLOCK.containsKey(id) ? DataResult.success(BuiltInRegistries.BLOCK.get(id)) : DataResult.error(() -> String.format("Block %s not found!", id)), BuiltInRegistries.BLOCK::getKey).stable();

    private static final Codec<NoiseSelector<Block>> BLOCK_SELECTOR = NoiseSelector.codec(BLOCK_CODEC);

    public static final Codec<Zone> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(Zone::biome),
            COLOR_CODEC.fieldOf("color").forGetter(Zone::color),
            BLOCK_SELECTOR.optionalFieldOf("deepslate_block", DEFAULT_DEEPSLATE_BLOCK).forGetter(Zone::deepslateBlock),
            BLOCK_SELECTOR.optionalFieldOf("stone_block", DEFAULT_STONE_BLOCK).forGetter(Zone::stoneBlock),
            BLOCK_SELECTOR.optionalFieldOf("dirt_block", DEFAULT_DIRT_BLOCK).forGetter(Zone::dirtBlock),
            BLOCK_SELECTOR.optionalFieldOf("surface_block", DEFAULT_SURFACE_BLOCK).forGetter(Zone::surfaceBlock),
            Codec.INT.optionalFieldOf("height", DEFAULT_HEIGHT).forGetter(Zone::height),
            Codec.DOUBLE.optionalFieldOf("perlin_multiplier", DEFAULT_PERLIN_MULTIPLIER).forGetter(Zone::perlinMultiplier),
            Codec.DOUBLE.optionalFieldOf("pixel_weight", DEFAULT_PIXEL_WEIGHT).forGetter(Zone::pixelWeight),
            Codec.INT.optionalFieldOf("threshold_modifier").forGetter(Zone::thresholdModifier)
    ).apply(instance, instance.stable(Zone::new)));

    public static RegistryFileCodec<Zone> CODEC = RegistryFileCodec.create(CTerrainGeneration.MAP_ZONES_REGISTRY, DIRECT_CODEC);
}
