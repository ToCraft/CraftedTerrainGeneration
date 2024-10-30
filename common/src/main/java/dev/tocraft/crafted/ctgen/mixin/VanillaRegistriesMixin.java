package dev.tocraft.crafted.ctgen.mixin;

import dev.tocraft.crafted.ctgen.xtend.CTRegistries;
import dev.tocraft.crafted.ctgen.zone.Zones;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VanillaRegistries.class)
public class VanillaRegistriesMixin {
    @Shadow
    @Final
    private static RegistrySetBuilder BUILDER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onInit(CallbackInfo ci) {
        BUILDER.add(CTRegistries.ZONES_KEY, Zones::bootstrap);
    }
}
