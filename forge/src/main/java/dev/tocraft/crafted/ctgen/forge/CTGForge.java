package dev.tocraft.crafted.ctgen.forge;

import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import net.minecraftforge.common.MinecraftForge;
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
public class CTGForge {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel SYNC_MAP_CHANNEL = NetworkRegistry.newSimpleChannel(SyncMapPacket.PACKET_ID, () -> PROTOCOL_VERSION, ver -> true, ver -> true);

    public CTGForge() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
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

        CTerrainGeneration.initialize();
    }
}
