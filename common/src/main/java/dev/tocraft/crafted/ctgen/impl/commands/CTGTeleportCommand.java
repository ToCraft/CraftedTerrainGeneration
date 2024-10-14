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
                                Commands.argument("pos", Vec2Argument.vec2())
                                        .executes(
                                                commandContext -> teleportToPos(
                                                        commandContext.getSource(),
                                                        Collections.singleton(commandContext.getSource().getEntityOrException()),
                                                        commandContext.getSource().getLevel(),
                                                        Vec2Argument.getVec2(commandContext, "pos")
                                                )
                                        )
                        )
                        .then(
                                Commands.argument("targets", EntityArgument.entities())
                                        .then(
                                                Commands.argument("pos", Vec2Argument.vec2())
                                                        .executes(
                                                                commandContext -> teleportToPos(
                                                                        commandContext.getSource(),
                                                                        EntityArgument.getEntities(commandContext, "targets"),
                                                                        commandContext.getSource().getLevel(),
                                                                        Vec2Argument.getVec2(commandContext, "pos")
                                                                )
                                                        )
                                        )
                        ).build();

        LiteralCommandNode<CommandSourceStack> tpNode = Commands.literal("tp").redirect(teleportNode).build();
        rootNode.addChild(teleportNode);
        rootNode.addChild(tpNode);
    }

    private static int teleportToPos(CommandSourceStack source, Collection<? extends Entity> targets, ServerLevel level, Vec2 dest) throws CommandSyntaxException {
        if (level.getChunkSource().getGenerator() instanceof MapBasedChunkGenerator generator) {
            MapSettings settings = generator.getSettings();
            int x = ((int) dest.x) - settings.xOffset(0) << 2;
            int z = ((int) dest.y) - settings.yOffset(0) << 2;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            for (Entity entity : targets) {
                performTeleport(entity, level, pos);
            }

            if (targets.size() == 1) {
                source.sendSuccess(
                        () -> Component.translatable(
                                "commands.teleport.success.location.single",
                                targets.iterator().next().getDisplayName(),
                                pos.getX(),
                                pos.getY(),
                                pos.getZ()
                        ),
                        true
                );
            } else {
                source.sendSuccess(
                        () -> Component.translatable(
                                "commands.teleport.success.location.multiple",
                                targets.size(),
                                pos.getX(),
                                pos.getY(),
                                pos.getZ()
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