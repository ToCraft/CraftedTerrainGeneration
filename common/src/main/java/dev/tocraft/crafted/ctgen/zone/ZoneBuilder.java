package dev.tocraft.crafted.ctgen.zone;

import dev.tocraft.crafted.ctgen.blockplacer.BasicPlacer;
import dev.tocraft.crafted.ctgen.blockplacer.BlockPlacer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.util.Optional;

@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
public class ZoneBuilder {
    private Holder<Biome> biome;
    private int color;
    private int height = Zone.DEFAULT_HEIGHT;
    private double perlinMultiplier = Zone.DEFAULT_PERLIN_MULTIPLIER;
    private double pixelWeight = Zone.DEFAULT_PIXEL_WEIGHT;
    private BlockPlacer deepslateBlock = Zone.DEFAULT_DEEPSLATE_BLOCK;
    private BlockPlacer stoneBlock = Zone.DEFAULT_STONE_BLOCK;
    private BlockPlacer dirtBlock = Zone.DEFAULT_DIRT_BLOCK;
    private BlockPlacer surfaceBlock = Zone.DEFAULT_SURFACE_BLOCK;
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

    public ZoneBuilder setDeepslateBlockPlacer(BlockPlacer deepslateBlock) {
        this.deepslateBlock = deepslateBlock;
        return this;
    }

    public ZoneBuilder setStoneBlockPlacer(BlockPlacer stoneBlock) {
        this.stoneBlock = stoneBlock;
        return this;
    }

    public ZoneBuilder setDirtBlockPlacer(BlockPlacer dirtBlock) {
        this.dirtBlock = dirtBlock;
        return this;
    }

    public ZoneBuilder setSurfaceBlockPlacer(BlockPlacer surfaceBlock) {
        this.surfaceBlock = surfaceBlock;
        return this;
    }

    public ZoneBuilder setDeepslateBlock(Block deepslateBlock) {
        this.deepslateBlock = new BasicPlacer(deepslateBlock);
        return this;
    }

    public ZoneBuilder setStoneBlock(Block stoneBlock) {
        this.stoneBlock = new BasicPlacer(stoneBlock);
        return this;
    }

    public ZoneBuilder setDirtBlock(Block dirtBlock) {
        this.dirtBlock = new BasicPlacer(dirtBlock);
        return this;
    }

    public ZoneBuilder setSurfaceBlock(Block surfaceBlock) {
        this.surfaceBlock = new BasicPlacer(surfaceBlock);
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