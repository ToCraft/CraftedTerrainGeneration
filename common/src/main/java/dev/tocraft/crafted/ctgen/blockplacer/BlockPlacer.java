package dev.tocraft.crafted.ctgen.blockplacer;

import com.mojang.serialization.Codec;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.impl.services.ServerPlatform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.function.Function;

public abstract class BlockPlacer {
    public static final ResourceKey<Registry<Codec<? extends BlockPlacer>>> BLOCK_PLAYER_REGISTRY_KEY = ResourceKey.createRegistryKey(CTerrainGeneration.id("block_placer"));
    public static final Registry<Codec<? extends BlockPlacer>> BLOCK_PLACER_REGISTRY = ServerPlatform.INSTANCE.simpleRegistry(BLOCK_PLAYER_REGISTRY_KEY);

    public static final Codec<BlockPlacer> CODEC = BLOCK_PLACER_REGISTRY.byNameCodec().dispatchStable(BlockPlacer::codec, Function.identity());

    public abstract Block get(SimplexNoise noise, double x, double y, double z);

    public abstract Block get(SimplexNoise noise, double x, double z);

    protected abstract Codec<? extends BlockPlacer> codec();
}
