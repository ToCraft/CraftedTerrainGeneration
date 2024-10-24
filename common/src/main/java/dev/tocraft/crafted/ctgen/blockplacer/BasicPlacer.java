package dev.tocraft.crafted.ctgen.blockplacer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.zone.Codecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.NotNull;

public class BasicPlacer extends BlockPlacer {
    @NotNull
    private final Block value;

    public BasicPlacer(@NotNull Block value) {
        this.value = value;
    }

    public Block get(SimplexNoise noise, double x, double y, double z, String layer) {
        return value;
    }

    @Override
    public Block get(SimplexNoise noise, double x, double z, String layer) {
        return value;
    }

    public @NotNull Block getValue() {
        return value;
    }

    public static final Codec<BasicPlacer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.BLOCK.fieldOf("value").forGetter(o -> o.value)
    ).apply(instance, instance.stable(BasicPlacer::new)));

    public static final ResourceLocation ID = CTerrainGeneration.id("basic");

    @Override
    public Codec<BasicPlacer> codec() {
        return CODEC;
    }
}
