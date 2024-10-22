package dev.tocraft.crafted.ctgen.zone;

import dev.tocraft.crafted.ctgen.util.NoiseSelector;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.awt.*;
import java.util.Optional;

@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class ZoneBuilder {
    private Holder<Biome> biome;
    private int color;
    private int height = Zone.DEFAULT_HEIGHT;
    private double perlinMultiplier = Zone.DEFAULT_PERLIN_MULTIPLIER;
    private double pixelWeight = Zone.DEFAULT_PIXEL_WEIGHT;
    private NoiseSelector<Block> deepslateBlock = NoiseSelector.of(Blocks.DEEPSLATE);
    private NoiseSelector<Block> stoneBlock = NoiseSelector.of(Blocks.STONE);
    private NoiseSelector<Block> dirtBlock = NoiseSelector.of(Blocks.DIRT);
    private NoiseSelector<Block> surfaceBlock = NoiseSelector.of(Blocks.GRASS_BLOCK);
    private Optional<Integer> thresholdModifier = Optional.empty();

    public ZoneBuilder setBiome(Holder<Biome> biome) {
        this.biome = biome;
        return this;
    }

    public ZoneBuilder setColor(int color) {
        this.color = color;
        return this;
    }

    public ZoneBuilder setColor(Color color) {
        this.color = color.getRGB();
        return this;
    }

    public ZoneBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public ZoneBuilder setPerlinMultiplier(double perlinMultiplier) {
        this.perlinMultiplier = perlinMultiplier;
        return this;
    }

    public ZoneBuilder setPixelWeight(double pixelWeight) {
        this.pixelWeight = pixelWeight;
        return this;
    }

    public ZoneBuilder setDeepslateBlock(NoiseSelector<Block> deepslateBlock) {
        this.deepslateBlock = deepslateBlock;
        return this;
    }

    public ZoneBuilder setStoneBlock(NoiseSelector<Block> stoneBlock) {
        this.stoneBlock = stoneBlock;
        return this;
    }

    public ZoneBuilder setDirtBlock(NoiseSelector<Block> dirtBlock) {
        this.dirtBlock = dirtBlock;
        return this;
    }

    public ZoneBuilder setSurfaceBlock(NoiseSelector<Block> surfaceBlock) {
        this.surfaceBlock = surfaceBlock;
        return this;
    }

    public ZoneBuilder setDeepslateBlock(Block deepslateBlock) {
        this.deepslateBlock = NoiseSelector.of(deepslateBlock);
        return this;
    }

    public ZoneBuilder setStoneBlock(Block stoneBlock) {
        this.stoneBlock = NoiseSelector.of(stoneBlock);
        return this;
    }

    public ZoneBuilder setDirtBlock(Block dirtBlock) {
        this.dirtBlock = NoiseSelector.of(dirtBlock);
        return this;
    }

    public ZoneBuilder setSurfaceBlock(Block surfaceBlock) {
        this.surfaceBlock = NoiseSelector.of(surfaceBlock);
        return this;
    }

    public ZoneBuilder setThresholdModifier(int thresholdModifier) {
        this.thresholdModifier = Optional.of(thresholdModifier);
        return this;
    }

    public Zone build() {
        return new Zone(biome, color, deepslateBlock, stoneBlock, dirtBlock, surfaceBlock, height, perlinMultiplier, pixelWeight, thresholdModifier);
    }
}