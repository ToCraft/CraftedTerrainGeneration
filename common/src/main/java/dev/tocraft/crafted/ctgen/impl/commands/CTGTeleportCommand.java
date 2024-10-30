package dev.tocraft.crafted.ctgen.impl.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tocraft.crafted.ctgen.impl.CTGCommand;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.crafted.ctgen.worldgen.MapSettings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class CTGTeleportCommand {
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.teleport.invalidPosition"));

    public static void register(LiteralCommandNode<CommandSourceStack> rootNode) {
        LiteralCommandNode<CommandSourceStack> teleportNode =
                Commands.literal("teleport")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                        .then(
                                Commands.argument("pixel", Vec2Argument.vec2())
                                        .executes(
                                                commandContext -> teleportToPos(
                                                        commandContext.getSource(),
                                                        Collections.singleton(commandContext.getSource().getEntityOrException()),
                                                        commandContext.getSource().getLevel(),
                                                        Vec2Argument.getVec2(commandContext, "pixel")
                                                )
                                        )
                        )
                        .then(
                                Commands.argument("targets", EntityArgument.entities())
                                        .then(
                                                Commands.argument("pixel", Vec2Argument.vec2())
                                                        .executes(
                                                                commandContext -> teleportToPos(
                                                                        commandContext.getSource(),
                                                                        EntityArgument.getEntities(commandContext, "targets"),
                                                                        commandContext.getSource().getLevel(),
                                                                        Vec2Argument.getVec2(commandContext, "pixel")
                                                                )
                                                        )
                                        )
                        ).build();

        rootNode.addChild(teleportNode);
    }

    private static int teleportToPos(CommandSourceStack source, Collection<? extends Entity> targets, ServerLevel level, Vec2 dest) throws CommandSyntaxException {
        if (level.getChunkSource().getGenerator() instanceof MapBasedChunkGenerator generator) {
            MapSettings settings = generator.getSettings();

            int i = settings.isPixelsAreChunks() ? 4 : 2;

            int x = ((int) dest.x) - settings.xOffset(0) << i;
            int z = ((int) dest.y) - settings.yOffset(0) << i;
            int y = level.getChunk(x >> 4, z >> 4).getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) + 1;
            BlockPos pos = new BlockPos(x, y, z);

            for (Entity entity : targets) {
                performTeleport(entity, level, pos);
            }

            Component coords = Component.translatable("ctgen.coordinates", ((int) dest.x), ((int) dest.y));
            if (targets.size() == 1) {
                source.sendSuccess(
                        () -> Component.translatable(
                                "ctgen.commands.teleport.success.location.single",
                                targets.iterator().next().getDisplayName(),
                                coords
                        ),
                        true
                );
            } else {
                source.sendSuccess(
                        () -> Component.translatable(
                                "ctgen.commands.teleport.success.location.multiple",
                                targets.size(),
                                coords
                        ),
                        true
                );
            }
        } else {
            throw CTGCommand.INVALID_CHUNK_GENERATOR.create();
        }

        return targets.size();
    }

    private static void performTeleport(
            Entity entity,
            ServerLevel level,
            BlockPos pos

    ) throws CommandSyntaxException {
        if (!Level.isInSpawnableBounds(pos)) {
            throw INVALID_POSITION.create();
        } else {
            if (entity.teleportTo(level, pos.getX(), pos.getY(), pos.getZ(), new HashSet<>(), entity.getYRot(), entity.getXRot())) {
                if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.isFallFlying()) {
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                    entity.setOnGround(true);
                }

                if (entity instanceof PathfinderMob pathfinderMob) {
                    pathfinderMob.getNavigation().stop();
                }
            }
        }
    }
}
