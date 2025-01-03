package dev.tocraft.ctgen.fabric;

import dev.tocraft.ctgen.xtend.CTRegistries;
import dev.tocraft.ctgen.zone.Zones;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.RegistryPatchGenerator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public final class CTGDataGen implements DataGeneratorEntrypoint {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder().add(CTRegistries.ZONES_KEY, Zones::bootstrap);

    @Override
    public void onInitializeDataGenerator(@NotNull FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        // generate zones
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<DataProvider>) (output, registries) ->
                new RegistriesDatapackGenerator(output,
                        RegistryPatchGenerator.createLookup(registries, BUILDER).thenComposeAsync(patchedRegistries ->
                                CompletableFuture.completedFuture(patchedRegistries.patches()))
                ));
    }
}
