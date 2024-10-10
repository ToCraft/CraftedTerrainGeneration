package dev.tocraft.crafted.ctgen;

import dev.tocraft.crafted.ctgen.biome.Zone;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class CTerrainGeneration {
    public static final String MODID = "ctgen";
    public static final ResourceKey<Registry<Zone>> MAP_BIOME_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation("worldgen/map_based/biome"));

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
