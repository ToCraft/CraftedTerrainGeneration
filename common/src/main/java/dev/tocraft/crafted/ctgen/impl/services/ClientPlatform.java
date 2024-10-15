package dev.tocraft.crafted.ctgen.impl.services;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ClientPlatform extends PlatformService {
    ClientPlatform INSTANCE = PlatformService.load(ClientPlatform.class);
}
