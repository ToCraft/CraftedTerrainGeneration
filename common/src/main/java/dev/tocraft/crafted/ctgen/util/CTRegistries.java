package dev.tocraft.crafted.ctgen.util;

import com.mojang.serialization.Codec;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.blockplacer.BlockPlacer;
import dev.tocraft.crafted.ctgen.impl.services.ServerPlatform;
import dev.tocraft.crafted.ctgen.layer.BlockLayer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class CTRegistries {
    public static final ResourceKey<Registry<Codec<? extends BlockPlacer>>> BLOCK_PLAYER_KEY = ResourceKey.createRegistryKey(CTerrainGeneration.id("block_placer"));
    public static final Registry<Codec<? extends BlockPlacer>> BLOCK_PLACER = ServerPlatform.INSTANCE.simpleRegistry(BLOCK_PLAYER_KEY);

    public static final ResourceKey<Registry<Codec<? extends BlockLayer>>> BLOCK_LAYER_KEY = ResourceKey.createRegistryKey(CTerrainGeneration.id("block_layer"));
    public static final Registry<Codec<? extends BlockLayer>> BLOCK_LAYER = ServerPlatform.INSTANCE.simpleRegistry(BLOCK_LAYER_KEY);
}
