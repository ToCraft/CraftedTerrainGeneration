package dev.tocraft.ctgen;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@ApiStatus.Internal
public final class CTerrainGeneration {
    public static final String MODID = "ctgen";

    @Contract("_ -> new")
    @ApiStatus.Internal
    public static @NotNull ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
