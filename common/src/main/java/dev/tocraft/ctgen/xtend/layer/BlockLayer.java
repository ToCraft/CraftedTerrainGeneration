package dev.tocraft.ctgen.xtend.layer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.tocraft.ctgen.xtend.CTRegistries;
import dev.tocraft.ctgen.xtend.placer.BasicPlacer;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class BlockLayer {
    public static final Codec<BlockLayer> CODEC = CTRegistries.BLOCK_LAYER.byNameCodec().dispatchStable(BlockLayer::codec, Function.identity());
    private final String name;
    private final BlockPlacer fallback;

    public BlockLayer(String name, BlockPlacer fallback) {
        this.name = name;
        this.fallback = fallback;
    }

    @ApiStatus.Internal
    public static void register(@NotNull BiConsumer<ResourceLocation, MapCodec<? extends BlockLayer>> registerFunc) {
        registerFunc.accept(SeaLayer.ID, SeaLayer.CODEC);
        registerFunc.accept(HeightLayer.ID, HeightLayer.CODEC);
        registerFunc.accept(WeightLayer.ID, WeightLayer.CODEC);
    }

    @Contract("_ -> new")
    public static @NotNull BlockLayer deepslate(int minY) {
        return new HeightLayer(minY, 0, true, "deepslate", BasicPlacer.DEEPSLATE_BLOCK);
    }

    @Contract(" -> new")
    public static @NotNull BlockLayer stone() {
        return new WeightLayer(0, 0.96, "stone", BasicPlacer.STONE_BLOCK);
    }

    @Contract(" -> new")
    public static @NotNull BlockLayer dirt() {
        // max is 200% to ensure there's no gap caused by "shifted" blocks
        return new WeightLayer(0.96, 2, "dirt", BasicPlacer.DIRT_BLOCK);
    }

    @Contract(" -> new")
    public static @NotNull BlockLayer surface() {
        return new SurfaceLayer("surface", BasicPlacer.GRASS_BLOCK);
    }

    @Contract(" -> new")
    public static @NotNull BlockLayer sea() {
        return new SeaLayer("sea", BasicPlacer.WATER_BLOCK);
    }

    @Contract("_ -> new")
    public static @Unmodifiable List<BlockLayer> defaultLayers(int minY) {
        return List.of(surface(), deepslate(minY), sea(), stone(), dirt());
    }

    public abstract boolean is(SimplexNoise noise, int x, int y, int z, Zone zone, int minY, int seaLevel, double surfaceHeight, int genHeight, int shift);

    public String getName() {
        return this.name;
    }

    public BlockPlacer getFallback() {
        return fallback;
    }

    protected abstract MapCodec<? extends BlockLayer> codec();
}
