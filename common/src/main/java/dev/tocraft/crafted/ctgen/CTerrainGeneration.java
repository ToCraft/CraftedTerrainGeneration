package dev.tocraft.crafted.ctgen;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unused")
public class CTerrainGeneration {
    public static final String MODID = "ctgen";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
