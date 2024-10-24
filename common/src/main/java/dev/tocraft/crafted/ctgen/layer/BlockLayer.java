package dev.tocraft.crafted.ctgen.layer;

import com.mojang.serialization.Codec;
import dev.tocraft.crafted.ctgen.util.CTRegistries;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.function.Function;

public abstract class BlockLayer {
    public static final Codec<BlockLayer> CODEC = CTRegistries.BLOCK_LAYER.byNameCodec().dispatchStable(BlockLayer::codec, Function.identity());

    protected final String name;

    public BlockLayer(String name) {
        this.name = name;
    }

    public abstract boolean is(SimplexNoise noise, double x, double y, double z);

    public String getName() {
        return this.name;
    }

    ;

    protected abstract Codec<? extends BlockLayer> codec();
}
