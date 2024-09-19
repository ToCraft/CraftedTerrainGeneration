package dev.tocraft.crafted.ctgen.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class MapBasedBiomeSource extends BiomeSource {
    public static final Codec<MapBasedBiomeSource> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            MapSettings.CODEC.fieldOf("settings").forGetter(o -> o.settings)
    ).apply(instance, instance.stable(MapBasedBiomeSource::new)));

    private final MapSettings settings;

    public MapBasedBiomeSource(MapSettings settings) {
        this.settings = settings;
    }

    @Override
    protected @NotNull Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.settings.biomeData().stream().map(holder -> holder.value().biome());
    }

    @Override
    public @NotNull Holder<Biome> getNoiseBiome(int pX, int pY, int pZ, Climate.@NotNull Sampler pSampler) {
        return settings.getMapBiome(pX, pZ).value().biome();
    }

    public MapSettings getSettings() {
        return settings;
    }
}