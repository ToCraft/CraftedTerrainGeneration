package dev.tocraft.ctgen.xtend;

import com.mojang.serialization.Codec;
import dev.tocraft.ctgen.util.Registrar;
import dev.tocraft.ctgen.xtend.height.TerrainHeight;
import dev.tocraft.ctgen.xtend.layer.BlockLayer;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class CTRegistries {
    public static final ResourceKey<Registry<Zone>> ZONES_KEY = ResourceKey.createRegistryKey(new ResourceLocation("worldgen/map_based/zones"));

    public static final Registrar<Codec<? extends BlockPlacer>> BLOCK_PLACER = new Registrar<>();
    public static final Registrar<Codec<? extends BlockLayer>> BLOCK_LAYER = new Registrar<>();
    public static final Registrar<Codec<? extends TerrainHeight>> TERRAIN = new Registrar<>();
}
