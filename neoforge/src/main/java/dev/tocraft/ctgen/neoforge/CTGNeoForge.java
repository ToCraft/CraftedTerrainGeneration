package dev.tocraft.ctgen.neoforge;

import dev.tocraft.ctgen.CTerrainGeneration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

@SuppressWarnings("unused")
@ApiStatus.Internal
@Mod(CTerrainGeneration.MODID)
public final class CTGNeoForge {
    public CTGNeoForge() {
        IEventBus modEventBus = Objects.requireNonNull(ModList.get().getModContainerById(CTerrainGeneration.MODID).orElseThrow().getEventBus());

        // register event handlers
        CTGNeoForgeEventListener.initialize(modEventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CTGNeoForgeClientEventListener.initialize(modEventBus);
        }
    }
}
