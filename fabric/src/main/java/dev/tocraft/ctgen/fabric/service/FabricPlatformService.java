package dev.tocraft.ctgen.fabric.service;

import dev.tocraft.ctgen.platform.PlatformService;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class FabricPlatformService implements PlatformService {
    @Override
    public <T> Registry<T> createSimpleRegistry(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.createSimple(key).buildAndRegister();
    }
}
