package dev.tocraft.ctgen.xtend.carver;

import com.mojang.serialization.Codec;
import dev.tocraft.ctgen.xtend.CTRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class Carver {
    public static final Codec<Carver> CODEC = CTRegistries.CARVER.byNameCodec().dispatchStable(Carver::codec, Function.identity());

    public static void register() {
        CTRegistries.CARVER.register(NoiseCarver.ID, NoiseCarver.CODEC);
    }


    public abstract boolean canSetBlock(SimplexNoise noise, @NotNull BlockPos pos, double surfaceHeight, int minHeight, double carverModifier);

    protected abstract Codec<? extends Carver> codec();
}
