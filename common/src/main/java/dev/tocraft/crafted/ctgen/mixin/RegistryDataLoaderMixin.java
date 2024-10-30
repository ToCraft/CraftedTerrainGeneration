package dev.tocraft.crafted.ctgen.mixin;

import dev.tocraft.crafted.ctgen.xtend.CTRegistries;
import dev.tocraft.crafted.ctgen.zone.Zone;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
    @Shadow
    @Final
    @Mutable
    public static List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onInit(CallbackInfo ci) {
        List<RegistryDataLoader.RegistryData<?>> worldGenRegistries = new ArrayList<>(WORLDGEN_REGISTRIES);

        int biomeIndex = -1;
        for (int i = 0; i < worldGenRegistries.size(); i++) {
            if (worldGenRegistries.get(i).key() == Registries.BIOME) {
                biomeIndex = i;
                break;
            }
        }
        if (biomeIndex != -1) {
            RegistryDataLoader.RegistryData<?> zoneRegistryData = new RegistryDataLoader.RegistryData<>(CTRegistries.ZONES_KEY, Zone.DIRECT_CODEC);
            worldGenRegistries.add(biomeIndex + 1, zoneRegistryData);
            WORLDGEN_REGISTRIES = worldGenRegistries;
        }
    }
}
