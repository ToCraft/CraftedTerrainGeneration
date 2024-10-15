package dev.tocraft.crafted.ctgen.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class CTGClient {
    public static final AtomicReference<ResourceLocation> CURRENT_MAP = new AtomicReference<>(null);
}
