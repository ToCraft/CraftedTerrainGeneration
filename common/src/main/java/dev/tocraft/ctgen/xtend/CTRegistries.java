package dev.tocraft.ctgen.xtend;

import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class CTRegistries {
    public static final ResourceKey<Registry<Zone>> ZONES_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("worldgen/map_based/zones"));
}
