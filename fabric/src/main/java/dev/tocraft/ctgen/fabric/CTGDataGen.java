package dev.tocraft.ctgen.fabric;

import dev.tocraft.ctgen.xtend.CTRegistries;
import dev.tocraft.ctgen.zone.Zones;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CTGDataGen implements DataGeneratorEntrypoint {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder().add(CTRegistries.ZONES_KEY, Zones::bootstrap);

    @Override
    public void onInitializeDataGenerator(@NotNull FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<DataProvider>) (output, registriesFuture) ->
                new RegistriesDatapackGenerator(output, CompletableFuture.supplyAsync(() -> {
                    RegistryAccess.Frozen frozen = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
                    return BUILDER.buildPatch(frozen, registriesFuture.join());
                })));
    }
}
