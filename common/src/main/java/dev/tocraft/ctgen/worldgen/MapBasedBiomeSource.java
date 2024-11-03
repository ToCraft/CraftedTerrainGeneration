package dev.tocraft.ctgen.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class MapBasedBiomeSource extends BiomeSource {
    public static final ResourceLocation ID = CTerrainGeneration.id("map_based_biome_source");
    public static final MapCodec<MapBasedBiomeSource> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            MapSettings.CODEC.fieldOf("settings").forGetter(o -> o.settings)
    ).apply(instance, instance.stable(MapBasedBiomeSource::new)));

    final MapSettings settings;

    public MapBasedBiomeSource(MapSettings settings) {
        this.settings = settings;
    }

    @Override
    protected @NotNull MapCodec<MapBasedBiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.settings.zones.stream().map(holder -> holder.value().biome());
    }

    @Override
    public @NotNull Holder<Biome> getNoiseBiome(int pX, int pY, int pZ, Climate.@NotNull Sampler pSampler) {
        return settings.getZone(pX, pZ).value().biome();
    }
}