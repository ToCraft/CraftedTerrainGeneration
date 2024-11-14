package dev.tocraft.ctgen.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.ServiceLoader;

@ApiStatus.Internal
public interface PlatformService {
    PlatformService INSTANCE = ServiceLoader.load(PlatformService.class).findFirst().orElseThrow(() -> new RuntimeException("Couldn't initialize CTGen Platform Service!"));

    <T> Registry<T> createSimpleRegistry(ResourceKey<Registry<T>> key);
}
