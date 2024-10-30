package dev.tocraft.crafted.ctgen.xtend.terrain;

import com.mojang.serialization.Codec;
import dev.tocraft.crafted.ctgen.worldgen.MapSettings;
import dev.tocraft.crafted.ctgen.xtend.CTRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.function.Function;

public abstract class TerrainHeight {
    public static final Codec<TerrainHeight> CODEC = CTRegistries.TERRAIN.byNameCodec().dispatchStable(TerrainHeight::codec, Function.identity());

    public static void register() {
        Registry.register(CTRegistries.TERRAIN, BasicSurface.ID, BasicSurface.CODEC);
    }

    /**
     * @param settings the map settings used by the chunk generator
     * @param noise    the SimplexNoise to be used as perlin
     * @param x        this should be a block position x
     * @param z        this should be a block position z
     * @return the relative height
     */
    public abstract double getHeight(MapSettings settings, SimplexNoise noise, int x, int z, double terrainModifier);

    protected abstract Codec<? extends TerrainHeight> codec();
}
