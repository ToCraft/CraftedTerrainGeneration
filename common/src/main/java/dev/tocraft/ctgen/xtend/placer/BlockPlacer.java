package dev.tocraft.ctgen.xtend.placer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.tocraft.ctgen.util.Codecs;
import dev.tocraft.ctgen.xtend.CTRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class BlockPlacer {
    public static final Codec<BlockPlacer> DIRECT_CODEC = CTRegistries.BLOCK_PLACER.byNameCodec().dispatchStable(BlockPlacer::codec, Function.identity());
    public static final Codec<BlockPlacer> CODEC = Codec.either(Codecs.BLOCK, DIRECT_CODEC).xmap(either -> {
        Optional<Block> left = either.left();
        if (left.isPresent()) {
            return new BasicPlacer(left.get());
        } else {
            return either.right().orElseThrow();
        }
    }, placer -> {
        if (placer instanceof BasicPlacer basic) {
            return Either.left(basic.getValue());
        } else {
            return Either.right(placer);
        }
    });

    @ApiStatus.Internal
    public static void register(@NotNull BiConsumer<ResourceLocation, MapCodec<? extends BlockPlacer>> registerFunc) {
        registerFunc.accept(BasicPlacer.ID, BasicPlacer.CODEC);
        registerFunc.accept(NoisePlacer.ID, NoisePlacer.CODEC);
    }

    @NotNull
    public abstract Block get(SimplexNoise noise, double x, double y, double z, double surfaceHeight, String layer);

    protected abstract MapCodec<? extends BlockPlacer> codec();
}
