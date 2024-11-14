package dev.tocraft.ctgen.xtend.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.tocraft.ctgen.xtend.CTRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class Carver {
    public static final Codec<Carver> CODEC = CTRegistries.CARVER.byNameCodec().dispatchStable(Carver::codec, Function.identity());

    @ApiStatus.Internal
    public static void register(@NotNull BiConsumer<ResourceLocation, MapCodec<? extends Carver>> registerFunc) {
        registerFunc.accept(NoiseCarver.ID, NoiseCarver.CODEC);
    }

    public abstract boolean canSetBlock(SimplexNoise noise, @NotNull BlockPos pos, double surfaceHeight, int minHeight, double carverModifier);

    protected abstract MapCodec<? extends Carver> codec();
}
