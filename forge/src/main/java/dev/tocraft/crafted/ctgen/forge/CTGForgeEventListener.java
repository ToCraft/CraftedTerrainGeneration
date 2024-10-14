package dev.tocraft.crafted.ctgen.forge;

import dev.tocraft.crafted.ctgen.data.MapImageRegistry;
import dev.tocraft.crafted.ctgen.impl.CTGCommand;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unused")
public class CTGForgeEventListener {
    private static final List<PreparableReloadListener> RELOAD_LISTENERS = new CopyOnWriteArrayList<>() {
        {
            add(new MapImageRegistry());
        }
    };

    @SubscribeEvent
    public void addReloadListenerEvent(AddReloadListenerEvent event) {
        for (PreparableReloadListener reloadListener : RELOAD_LISTENERS) {
            event.addListener(reloadListener);
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CTGCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }
}
