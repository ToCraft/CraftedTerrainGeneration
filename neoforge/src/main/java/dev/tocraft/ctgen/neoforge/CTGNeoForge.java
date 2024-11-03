package dev.tocraft.ctgen.neoforge;

import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.xtend.carver.Carver;
import dev.tocraft.ctgen.xtend.height.TerrainHeight;
import dev.tocraft.ctgen.xtend.layer.BlockLayer;
import dev.tocraft.ctgen.xtend.placer.BlockPlacer;
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

        // values for the built-in registries
        BlockPlacer.register();
        BlockLayer.register();
        TerrainHeight.register();
        Carver.register();
    }
}
