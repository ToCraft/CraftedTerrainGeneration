package dev.tocraft.crafted.ctgen;

import dev.tocraft.crafted.ctgen.zone.Zone;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class CTerrainGeneration {
    public static final String MODID = "ctgen";
    public static final ResourceKey<Registry<Zone>> MAP_ZONES_REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation("worldgen/map_based/zones"));

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
