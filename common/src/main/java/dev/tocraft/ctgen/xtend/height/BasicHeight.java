package dev.tocraft.ctgen.xtend.height;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.worldgen.MapSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class BasicHeight extends TerrainHeight {
    public static final BasicHeight DEFAULT = new BasicHeight();

    public static final Codec<BasicHeight> CODEC = RecordCodecBuilder.create(instance -> instance.stable(new BasicHeight()));
    public static final ResourceLocation ID = CTerrainGeneration.id("basic_height");

    @Override
    public double getHeight(MapSettings settings, SimplexNoise noise, int x, int z, double terrainModifier) {
        return 0;
    }

    @Override
    protected Codec<BasicHeight> codec() {
        return CODEC;
    }
}