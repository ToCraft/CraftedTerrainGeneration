package dev.tocraft.crafted.ctgen.layer;

import com.mojang.serialization.Codec;
import dev.tocraft.crafted.ctgen.blockplacer.BasicPlacer;
import dev.tocraft.crafted.ctgen.blockplacer.BlockPlacer;
import dev.tocraft.crafted.ctgen.util.CTRegistries;
import dev.tocraft.crafted.ctgen.zone.Zone;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.List;
import java.util.function.Function;

public abstract class BlockLayer {
    public static final Codec<BlockLayer> CODEC = CTRegistries.BLOCK_LAYER.byNameCodec().dispatchStable(BlockLayer::codec, Function.identity());
    private final String name;
    private final boolean hasCaves;
    private final BlockPlacer fallback;

    public BlockLayer(String name, boolean hasCaves, BlockPlacer fallback) {
        this.name = name;
        this.hasCaves = hasCaves;
        this.fallback = fallback;
    }

    public static void register() {
        Registry.register(CTRegistries.BLOCK_LAYER, SeaLayer.ID, SeaLayer.CODEC);
        Registry.register(CTRegistries.BLOCK_LAYER, HeightLayer.ID, HeightLayer.CODEC);
        Registry.register(CTRegistries.BLOCK_LAYER, WeightLayer.ID, WeightLayer.CODEC);
    }

    public static BlockLayer deepslate(int minY) {
        return new HeightLayer(minY, 0, true, "deepslate", BasicPlacer.DEEPSLATE_BLOCK);
    }

    public static BlockLayer stone() {
        return new WeightLayer(0, 0.96, "stone", BasicPlacer.STONE_BLOCK);
    }

    public static BlockLayer dirt() {
        // max is 200% to ensure there's no gap caused by "shifted" blocks
        return new WeightLayer(0.96, 2, "dirt", BasicPlacer.DIRT_BLOCK);
    }

    public static BlockLayer surface() {
        return new SurfaceLayer("surface", BasicPlacer.GRASS_BLOCK);
    }

    public static BlockLayer sea() {
        return new SeaLayer("sea", BasicPlacer.WATER_BLOCK);
    }

    public static List<BlockLayer> defaultLayers(int minY) {
        return List.of(surface(), deepslate(minY), sea(), stone(), dirt());
    }

    public abstract boolean is(SimplexNoise noise, int x, int y, int z, Zone zone, int minY, int seaLevel, double surfaceHeight, int genHeight, int shift);

    public String getName() {
        return this.name;
    }

    public boolean hasCaves() {
        return hasCaves;
    }

    public BlockPlacer getFallback() {
        return fallback;
    }

    protected abstract Codec<? extends BlockLayer> codec();
}
