package dev.tocraft.crafted.ctgen;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class CTerrainGeneration {
    public static final String MODID = "ctgen";

    @Contract("_ -> new")
    public static @NotNull ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
