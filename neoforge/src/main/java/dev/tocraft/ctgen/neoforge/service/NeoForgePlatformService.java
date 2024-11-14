package dev.tocraft.ctgen.neoforge.service;

import dev.tocraft.ctgen.platform.PlatformService;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class NeoForgePlatformService implements PlatformService {
    @Override
    public <T> Registry<T> createSimpleRegistry(ResourceKey<Registry<T>> key) {
        return new RegistryBuilder<>(key).create();
    }
}
