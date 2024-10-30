package dev.tocraft.ctgen.forge;

import dev.tocraft.ctgen.data.MapImageRegistry;
import dev.tocraft.ctgen.impl.CTGCommand;
import dev.tocraft.ctgen.worldgen.MapBasedBiomeSource;
import dev.tocraft.ctgen.worldgen.MapBasedChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ApiStatus.Internal
public final class CTGForgeEventListener {
    public static void initialize(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.addListener(CTGForgeEventListener::addReloadListenerEvent);
        MinecraftForge.EVENT_BUS.addListener(CTGForgeEventListener::registerCommands);
        modEventBus.addListener(CTGForgeEventListener::register);
    }

    private static final List<PreparableReloadListener> RELOAD_LISTENERS = new CopyOnWriteArrayList<>() {
        {
            add(new MapImageRegistry());
        }
    };

    private static void addReloadListenerEvent(AddReloadListenerEvent event) {
        for (PreparableReloadListener reloadListener : RELOAD_LISTENERS) {
            event.addListener(reloadListener);
        }
    }

    private static void registerCommands(@NotNull RegisterCommandsEvent event) {
        CTGCommand.register(event.getDispatcher(), event.getBuildContext());
    }

    private static void register(RegisterEvent event) {
        // generic stuff
        event.register(Registries.BIOME_SOURCE, helper -> helper.register(MapBasedBiomeSource.ID, MapBasedBiomeSource.CODEC));
        event.register(Registries.CHUNK_GENERATOR, helper -> helper.register(MapBasedChunkGenerator.ID, MapBasedChunkGenerator.CODEC));
    }
}
