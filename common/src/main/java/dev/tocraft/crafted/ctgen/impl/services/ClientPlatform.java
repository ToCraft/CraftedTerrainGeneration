package dev.tocraft.crafted.ctgen.impl.services;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public interface ClientPlatform extends PlatformService {
    ClientPlatform INSTANCE = PlatformService.load(ClientPlatform.class);
}
