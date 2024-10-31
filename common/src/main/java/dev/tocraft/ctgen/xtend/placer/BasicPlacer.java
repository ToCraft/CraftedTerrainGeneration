package dev.tocraft.ctgen.xtend.placer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.util.Codecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.NotNull;

public class BasicPlacer extends BlockPlacer {
    public static final BlockPlacer DEEPSLATE_BLOCK = new BasicPlacer(Blocks.DEEPSLATE);
    public static final BlockPlacer STONE_BLOCK = new BasicPlacer(Blocks.STONE);
    public static final BlockPlacer DIRT_BLOCK = new BasicPlacer(Blocks.DIRT);
    public static final BlockPlacer GRASS_BLOCK = new BasicPlacer(Blocks.GRASS_BLOCK);
    public static final BlockPlacer WATER_BLOCK = new BasicPlacer(Blocks.WATER);
    public static final BlockPlacer AIR = new BasicPlacer(Blocks.AIR);

    @NotNull
    private final Block value;

    public BasicPlacer(@NotNull Block value) {
        this.value = value;
    }

    public @NotNull Block get(SimplexNoise noise, double x, double y, double z, String layer) {
        return value;
    }

    @Override
    public @NotNull Block get(SimplexNoise noise, double x, double z, String layer) {
        return value;
    }

    public @NotNull Block getValue() {
        return value;
    }

    public static final Codec<BasicPlacer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.BLOCK.fieldOf("value").forGetter(o -> o.value)
    ).apply(instance, instance.stable(BasicPlacer::new)));

    public static final ResourceLocation ID = CTerrainGeneration.id("basic_placer");

    @Override
    protected Codec<BasicPlacer> codec() {
        return CODEC;
    }
}
