package dev.tocraft.ctgen.worldgen.noise;

import dev.tocraft.ctgen.CTerrainGeneration;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;

import java.util.Collections;

@SuppressWarnings("unused")
public class CTGenNoiseSettings {
    protected static final NoiseSettings OVERWORLD_NOISE_SETTINGS = NoiseSettings.create(-64, 384, 1, 2);

    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(Registries.NOISE_SETTINGS, CTerrainGeneration.id("overworld"));

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        context.register(OVERWORLD, overworld(context));
    }

    private static NoiseGeneratorSettings overworld(BootstrapContext<?> context) {
        return new NoiseGeneratorSettings(OVERWORLD_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.overworld(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE), false, false), CTGenSurface.overworld(), Collections.emptyList(), 63, false, true, true, true);
    }
}
