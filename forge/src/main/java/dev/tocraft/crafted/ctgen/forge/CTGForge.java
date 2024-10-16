package dev.tocraft.crafted.ctgen.forge;

import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedBiomeSource;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
@ApiStatus.Internal
@Mod(CTerrainGeneration.MODID)
public class CTGForge {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel SYNC_MAP_CHANNEL = NetworkRegistry.newSimpleChannel(SyncMapPacket.PACKET_ID, () -> PROTOCOL_VERSION, ver -> true, ver -> true);

    public CTGForge() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::event);
        MinecraftForge.EVENT_BUS.register(new CTGForgeEventListener());

        SYNC_MAP_CHANNEL.messageBuilder(SyncMapPacket.class, 0)
                .decoder(SyncMapPacket::decode)
                .encoder(SyncMapPacket::encode)
                .consumerMainThread((packet, context) -> {
                    packet.handle();
                })
                .add();

        if (FMLEnvironment.dist.isClient()) {
            new CTGForgeClient();
        }
    }

    private void event(RegisterEvent event) {
        Registry.register(BuiltInRegistries.BIOME_SOURCE, CTerrainGeneration.id("map_based_biome_source"), MapBasedBiomeSource.CODEC);
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, CTerrainGeneration.id("map_based_chunk_generator"), MapBasedChunkGenerator.CODEC);
    }
}
