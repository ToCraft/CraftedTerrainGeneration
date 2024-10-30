package dev.tocraft.ctgen.xtend;

import com.mojang.serialization.Codec;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.impl.services.ServerPlatform;
import dev.tocraft.ctgen.xtend.layer.BlockLayer;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.ctgen.xtend.terrain.TerrainHeight;
import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class CTRegistries {
    public static final ResourceKey<Registry<Zone>> ZONES_KEY = ResourceKey.createRegistryKey(new ResourceLocation("worldgen/map_based/zones"));

    public static final ResourceKey<Registry<Codec<? extends BlockPlacer>>> BLOCK_PLAYER_KEY = ResourceKey.createRegistryKey(CTerrainGeneration.id("block_placer"));
    public static final Registry<Codec<? extends BlockPlacer>> BLOCK_PLACER = ServerPlatform.INSTANCE.simpleRegistry(BLOCK_PLAYER_KEY);

    public static final ResourceKey<Registry<Codec<? extends BlockLayer>>> BLOCK_LAYER_KEY = ResourceKey.createRegistryKey(CTerrainGeneration.id("block_layer"));
    public static final Registry<Codec<? extends BlockLayer>> BLOCK_LAYER = ServerPlatform.INSTANCE.simpleRegistry(BLOCK_LAYER_KEY);

    public static final ResourceKey<Registry<Codec<? extends TerrainHeight>>> TERRAIN_KEY = ResourceKey.createRegistryKey(CTerrainGeneration.id("terrain"));
    public static final Registry<Codec<? extends TerrainHeight>> TERRAIN = ServerPlatform.INSTANCE.simpleRegistry(TERRAIN_KEY);
}
