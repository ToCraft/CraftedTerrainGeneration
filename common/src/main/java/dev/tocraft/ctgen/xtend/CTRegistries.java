package dev.tocraft.ctgen.xtend;

import com.mojang.serialization.MapCodec;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.platform.PlatformService;
import dev.tocraft.ctgen.xtend.carver.Carver;
import dev.tocraft.ctgen.xtend.height.TerrainHeight;
import dev.tocraft.ctgen.xtend.layer.BlockLayer;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class CTRegistries {
    public static final ResourceKey<Registry<Zone>> ZONES_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("worldgen/map_based/zones"));
    public static final ResourceKey<Registry<MapCodec<? extends BlockPlacer>>> BLOCK_PLACER_KEY = ResourceKey.createRegistryKey(CTerrainGeneration.id("block_placer"));
    public static final ResourceKey<Registry<MapCodec<? extends BlockLayer>>> BLOCK_LAYER_KEY = ResourceKey.createRegistryKey(CTerrainGeneration.id("block_layer"));
    public static final ResourceKey<Registry<MapCodec<? extends TerrainHeight>>> TERRAIN_KEY = ResourceKey.createRegistryKey(CTerrainGeneration.id("terrain"));
    public static final ResourceKey<Registry<MapCodec<? extends Carver>>> CARVER_KEY = ResourceKey.createRegistryKey(CTerrainGeneration.id("carver"));

    public static final Registry<MapCodec<? extends BlockPlacer>> BLOCK_PLACER = PlatformService.INSTANCE.createSimpleRegistry(BLOCK_PLACER_KEY);
    public static final Registry<MapCodec<? extends BlockLayer>> BLOCK_LAYER = PlatformService.INSTANCE.createSimpleRegistry(BLOCK_LAYER_KEY);
    public static final Registry<MapCodec<? extends TerrainHeight>> TERRAIN = PlatformService.INSTANCE.createSimpleRegistry(TERRAIN_KEY);
    public static final Registry<MapCodec<? extends Carver>> CARVER = PlatformService.INSTANCE.createSimpleRegistry(CARVER_KEY);
}
