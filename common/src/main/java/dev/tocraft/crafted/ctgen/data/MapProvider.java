package dev.tocraft.crafted.ctgen.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import dev.tocraft.crafted.ctgen.map.MapBiome;
import dev.tocraft.crafted.ctgen.util.MapUtils;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class MapProvider implements DataProvider {
    private final PackOutput packOutput;
    private final BufferedImage original;
    private final ResourceLocation id;
    private final CompletableFuture<List<MapBiome>> mapBiomesFuture;

    public MapProvider(BufferedImage original, ResourceLocation id, CompletableFuture<List<MapBiome>> mapBiomes, PackOutput packOutput) {
        this.packOutput = packOutput;
        this.original = original;
        this.id = id;
        this.mapBiomesFuture = mapBiomes;
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return mapBiomesFuture.thenCompose(biomesList -> CompletableFuture.runAsync(() -> {
            try {
                BufferedImage validOriginal = MapUtils.approachColors(original, biomesList.stream().map(b -> new Color(b.color())).toList());
                BufferedImage biomeMap = MapUtils.generateBiomeMap(validOriginal, biomesList);
                {
                    Path output = packOutput.getOutputFolder().resolve("data/" + id.getNamespace() + "/worldgen/map_based/maps/" + id.getPath() + ".png");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    HashingOutputStream hashStream = new HashingOutputStream(Hashing.sha1(), baos);
                    ImageIO.write(biomeMap, "PNG", hashStream);
                    cache.writeIfNeeded(output, baos.toByteArray(), hashStream.hash());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Override
    public @NotNull String getName() {
        return "Map Image Generation";
    }
}
