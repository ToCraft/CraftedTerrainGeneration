package dev.tocraft.crafted.ctgen.worldgen;

import dev.tocraft.crafted.ctgen.biome.CarverSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Function;

public class CaveCarver {
    private final CarverSetting setting;
    private final long seed;
    private final SimplexNoise noise;
    private final Function<BlockPos, Block> caveAirGetter;
    private final Function<BlockPos, Integer> surfaceHeightGetter;

    public CaveCarver(CarverSetting setting, long seed, SimplexNoise noise, Function<BlockPos, Block> caveAirGetter, Function<BlockPos, Integer> surfaceHeightGetter) {
        this.setting = setting;
        this.seed = seed;
        this.noise = noise;
        this.caveAirGetter = caveAirGetter;
        this.surfaceHeightGetter = surfaceHeightGetter;
    }

    public void carveCaves(ChunkAccess chunk, ChunkSource chunkSource) {
        Random rand = new Random(chunk.getPos().toLong() ^ seed);

        int fullWorms = (int) setting.occurrences();
        float partialWormChance = setting.occurrences() - fullWorms;

        for (int i = 0; i < fullWorms; i++) {
            carveWorm(chunk, chunkSource, rand);
        }

        if (rand.nextFloat() < partialWormChance) {
            carveWorm(chunk, chunkSource, rand);
        }
    }

    private void carveWorm(ChunkAccess chunk, ChunkSource chunkSource, Random rand) {
        int startX = rand.nextInt(16);
        int startZ = rand.nextInt(16);
        int startY = rand.nextInt(surfaceHeightGetter.apply(chunk.getPos().getBlockAt(startX, chunk.getHeight(), startZ)));

        double x = startX + (chunk.getPos().x << 4);
        double y = startY;
        double z = startZ + (chunk.getPos().z << 4);

        int length = rand.nextInt(setting.minLength(), setting.maxLength());
        for (int i = 0; i < length; i++) {
            double[] direction = getNoiseDirection(x, y, z, noise);

            x += direction[0];
            y += direction[1];
            z += direction[2];

            carveSphere(chunk, chunkSource, (int) x, (int) y, (int) z);
        }
    }

    private double[] getNoiseDirection(double x, double y, double z, SimplexNoise noise) {
        double dx = noise.getValue(x * 0.05, y * 0.05, z * 0.05);
        double dy = noise.getValue(x * 0.05, y * 0.05 + 100, z * 0.05);
        double dz = noise.getValue(x * 0.05, y * 0.05 + 200, z * 0.05);

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length != 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }

        return new double[]{dx, dy, dz};
    }

    private void carveSphere(ChunkAccess chunk, ChunkSource chunkSource, int x, int y, int z) {
        int radius = setting.radius();
        int sqRadius = radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= sqRadius) {
                        int carveX = x + dx;
                        int carveY = y + dy;
                        int carveZ = z + dz;

                        BlockPos pos = new BlockPos(carveX, carveY, carveZ);
                        if (carveY < surfaceHeightGetter.apply(pos)) {

                            if (chunk.getPos().x << 4 <= carveX && carveX < (chunk.getPos().x << 4) + 16 &&
                                    chunk.getPos().z << 4 <= carveZ && carveZ < (chunk.getPos().z << 4) + 16) {
                                chunk.setBlockState(pos, caveAirGetter.apply(pos).defaultBlockState(), false);
                            } else {
                                ChunkAccess outChunk = getChunkAt(x, z, chunkSource);
                                if (outChunk != null) {
                                    outChunk.setBlockState(pos, caveAirGetter.apply(pos).defaultBlockState(), false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private ChunkAccess getChunkAt(double x, double z, ChunkSource chunkSource) {
        int chunkX = (int) Math.floor(x) >> 4;
        int chunkZ = (int) Math.floor(z) >> 4;
        return chunkSource.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, true);
    }
}
