package dev.tocraft.crafted.ctgen.forge;

import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedBiomeChunkGenerator;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedBiomeSource;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

@SuppressWarnings("unused")
@Mod(CTerrainGeneration.MODID)
public class CTGForge {
    public CTGForge() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::event);
        MinecraftForge.EVENT_BUS.register(new CTGForgeEventListener());
    }

    private void event(RegisterEvent event) {
        Registry.register(BuiltInRegistries.BIOME_SOURCE, CTerrainGeneration.id("map_based_biome_source"), MapBasedBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, CTerrainGeneration.id("map_based_chunk_generator"), MapBasedBiomeChunkGenerator.CODEC);
    }
}
