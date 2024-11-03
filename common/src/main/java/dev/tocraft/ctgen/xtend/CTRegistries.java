package dev.tocraft.ctgen.xtend;

import com.mojang.serialization.MapCodec;
import dev.tocraft.ctgen.util.Registrar;
import dev.tocraft.ctgen.xtend.carver.Carver;
import dev.tocraft.ctgen.xtend.height.TerrainHeight;
import dev.tocraft.ctgen.xtend.layer.BlockLayer;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import dev.tocraft.ctgen.zone.Zone;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class CTRegistries {
    public static final ResourceKey<Registry<Zone>> ZONES_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("worldgen/map_based/zones"));

    public static final Registrar<MapCodec<? extends BlockPlacer>> BLOCK_PLACER = new Registrar<>();
    public static final Registrar<MapCodec<? extends BlockLayer>> BLOCK_LAYER = new Registrar<>();
    public static final Registrar<MapCodec<? extends TerrainHeight>> TERRAIN = new Registrar<>();
    public static final Registrar<MapCodec<? extends Carver>> CARVER = new Registrar<>();
}
