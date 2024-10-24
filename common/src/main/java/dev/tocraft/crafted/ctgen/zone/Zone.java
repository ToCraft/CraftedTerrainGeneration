package dev.tocraft.crafted.ctgen.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.blockplacer.BasicPlacer;
import dev.tocraft.crafted.ctgen.blockplacer.BlockPlacer;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public record Zone(Holder<Biome> biome, int color, BlockPlacer deepslateBlock, BlockPlacer stoneBlock,
                   BlockPlacer dirtBlock,
                   BlockPlacer surfaceBlock, int height, double perlinMultiplier, double pixelWeight,
                   Optional<Integer> thresholdModifier) {

    public static final BlockPlacer DEFAULT_DEEPSLATE_BLOCK = new BasicPlacer(Blocks.DEEPSLATE);
    public static final BlockPlacer DEFAULT_STONE_BLOCK = new BasicPlacer(Blocks.STONE);
    public static final BlockPlacer DEFAULT_DIRT_BLOCK = new BasicPlacer(Blocks.DIRT);
    public static final BlockPlacer DEFAULT_SURFACE_BLOCK = new BasicPlacer(Blocks.GRASS_BLOCK);
    public static final int DEFAULT_HEIGHT = 0;
    public static final double DEFAULT_PERLIN_MULTIPLIER = 8;
    public static final double DEFAULT_PIXEL_WEIGHT = 1;

    public static final Codec<Zone> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(Zone::biome),
            Codecs.COLOR.fieldOf("color").forGetter(Zone::color),
            BlockPlacer.CODEC.optionalFieldOf("deepslate_block", DEFAULT_DEEPSLATE_BLOCK).forGetter(Zone::deepslateBlock),
            BlockPlacer.CODEC.optionalFieldOf("stone_block", DEFAULT_STONE_BLOCK).forGetter(Zone::stoneBlock),
            BlockPlacer.CODEC.optionalFieldOf("dirt_block", DEFAULT_DIRT_BLOCK).forGetter(Zone::dirtBlock),
            BlockPlacer.CODEC.optionalFieldOf("surface_block", DEFAULT_SURFACE_BLOCK).forGetter(Zone::surfaceBlock),
            Codec.INT.optionalFieldOf("height", DEFAULT_HEIGHT).forGetter(Zone::height),
            Codec.DOUBLE.optionalFieldOf("perlin_multiplier", DEFAULT_PERLIN_MULTIPLIER).forGetter(Zone::perlinMultiplier),
            Codec.DOUBLE.optionalFieldOf("pixel_weight", DEFAULT_PIXEL_WEIGHT).forGetter(Zone::pixelWeight),
            Codec.INT.optionalFieldOf("threshold_modifier").forGetter(Zone::thresholdModifier)
    ).apply(instance, instance.stable(Zone::new)));

    public static RegistryFileCodec<Zone> CODEC = RegistryFileCodec.create(CTerrainGeneration.MAP_ZONES_REGISTRY, DIRECT_CODEC);
}
