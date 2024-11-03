package dev.tocraft.ctgen.xtend.height;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.tocraft.ctgen.worldgen.MapSettings;
import dev.tocraft.ctgen.xtend.CTRegistries;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.function.Function;

public abstract class TerrainHeight {
    public static final Codec<TerrainHeight> CODEC = CTRegistries.TERRAIN.byNameCodec().dispatchStable(TerrainHeight::codec, Function.identity());

    public static void register() {
        CTRegistries.TERRAIN.register(BasicHeight.ID, BasicHeight.CODEC);
        CTRegistries.TERRAIN.register(NoiseHeight.ID, NoiseHeight.CODEC);
    }

    /**
     * @param settings the map settings used by the chunk generator
     * @param noise    the SimplexNoise to be used as perlin
     * @param x        this should be a block position x
     * @param z        this should be a block position z
     * @return the relative height
     */
    public abstract double getHeight(MapSettings settings, SimplexNoise noise, int x, int z, double terrainModifier);

    protected abstract MapCodec<? extends TerrainHeight> codec();
}
