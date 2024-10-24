package dev.tocraft.crafted.ctgen.blockplacer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.tocraft.crafted.ctgen.util.CTRegistries;
import dev.tocraft.crafted.ctgen.zone.Codecs;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.Optional;
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

    public static void register() {
        Registry.register(CTRegistries.BLOCK_PLACER, BasicPlacer.ID, BasicPlacer.CODEC);
        Registry.register(CTRegistries.BLOCK_PLACER, NoisePlacer.ID, NoisePlacer.CODEC);
    }

    public abstract Block get(SimplexNoise noise, double x, double y, double z, String layer);

    public abstract Block get(SimplexNoise noise, double x, double z, String layer);

    protected abstract Codec<? extends BlockPlacer> codec();
}
