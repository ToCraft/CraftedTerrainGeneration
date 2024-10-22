package dev.tocraft.crafted.ctgen.zone;

import dev.tocraft.crafted.ctgen.blockplacer.NoisePlacer;
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
    private NoisePlacer deepslateBlock = NoisePlacer.of(Blocks.DEEPSLATE);
    private NoisePlacer stoneBlock = NoisePlacer.of(Blocks.STONE);
    private NoisePlacer dirtBlock = NoisePlacer.of(Blocks.DIRT);
    private NoisePlacer surfaceBlock = NoisePlacer.of(Blocks.GRASS_BLOCK);
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

    public ZoneBuilder setDeepslateBlock(NoisePlacer deepslateBlock) {
        this.deepslateBlock = deepslateBlock;
        return this;
    }

    public ZoneBuilder setStoneBlock(NoisePlacer stoneBlock) {
        this.stoneBlock = stoneBlock;
        return this;
    }

    public ZoneBuilder setDirtBlock(NoisePlacer dirtBlock) {
        this.dirtBlock = dirtBlock;
        return this;
    }

    public ZoneBuilder setSurfaceBlock(NoisePlacer surfaceBlock) {
        this.surfaceBlock = surfaceBlock;
        return this;
    }

    public ZoneBuilder setDeepslateBlock(Block deepslateBlock) {
        this.deepslateBlock = NoisePlacer.of(deepslateBlock);
        return this;
    }

    public ZoneBuilder setStoneBlock(Block stoneBlock) {
        this.stoneBlock = NoisePlacer.of(stoneBlock);
        return this;
    }

    public ZoneBuilder setDirtBlock(Block dirtBlock) {
        this.dirtBlock = NoisePlacer.of(dirtBlock);
        return this;
    }

    public ZoneBuilder setSurfaceBlock(Block surfaceBlock) {
        this.surfaceBlock = NoisePlacer.of(surfaceBlock);
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