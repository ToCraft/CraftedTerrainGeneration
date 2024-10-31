package dev.tocraft.ctgen.forge;

import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.ctgen.xtend.carver.Carver;
import dev.tocraft.ctgen.xtend.height.TerrainHeight;
import dev.tocraft.ctgen.xtend.layer.BlockLayer;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
@ApiStatus.Internal
@Mod(CTerrainGeneration.MODID)
public final class CTGForge {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel SYNC_MAP_CHANNEL = NetworkRegistry.newSimpleChannel(SyncMapPacket.PACKET_ID, () -> PROTOCOL_VERSION, ver -> true, ver -> true);

    public CTGForge() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CTGForgeEventListener.initialize(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            CTGForgeClientEventListener.initialize(modEventBus);
        }

        SYNC_MAP_CHANNEL.messageBuilder(SyncMapPacket.class, 0)
                .decoder(SyncMapPacket::decode)
                .encoder(SyncMapPacket::encode)
                .consumerMainThread((packet, context) -> {
                    packet.handle();
                })
                .add();

        // values for the built-in registries
        BlockPlacer.register();
        BlockLayer.register();
        TerrainHeight.register();
        Carver.register();
    }
}
