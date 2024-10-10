package dev.tocraft.crafted.ctgen.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import dev.tocraft.crafted.ctgen.zone.Zone;
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
    private final CompletableFuture<List<Zone>> zonesFuture;

    public MapProvider(BufferedImage original, ResourceLocation id, CompletableFuture<List<Zone>> zones, PackOutput packOutput) {
        this.packOutput = packOutput;
        this.original = original;
        this.id = id;
        this.zonesFuture = zones;
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return zonesFuture.thenCompose(biomesList -> CompletableFuture.runAsync(() -> {
            try {
                BufferedImage validOriginal = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
                int count = MapUtils.approachColors(original, validOriginal, biomesList.stream().map(b -> new Color(b.color())).toList());
                LogUtils.getLogger().info("Corrected the color for {} pixels.", count);
                BufferedImage detailedMap = MapUtils.generateDetailedMap(validOriginal, biomesList);
                LogUtils.getLogger().info("Generated Map Image with estimated {}MB.", detailedMap.getWidth() * detailedMap.getHeight() * 3 / 1048576);
                {
                    Path output = packOutput.getOutputFolder().resolve("data/" + id.getNamespace() + "/worldgen/map_based/maps/" + id.getPath() + ".png");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    HashingOutputStream hashStream = new HashingOutputStream(Hashing.sha1(), baos);
                    ImageIO.write(detailedMap, "PNG", hashStream);
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
