package dev.tocraft.crafted.ctgen.impl.commands;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import dev.tocraft.crafted.ctgen.CTerrainGeneration;
import dev.tocraft.crafted.ctgen.impl.CTGCommand;
import dev.tocraft.crafted.ctgen.worldgen.MapBasedChunkGenerator;
import dev.tocraft.crafted.ctgen.worldgen.MapSettings;
import dev.tocraft.crafted.ctgen.zone.Zone;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class CTGLocateCommand {
    public static void register(LiteralCommandNode<CommandSourceStack> rootNode, CommandBuildContext context) {
        LiteralCommandNode<CommandSourceStack> locateNode =
                Commands.literal("locate")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                        .then(
                                Commands.literal("zone")
                                        .then(
                                                Commands.argument("dest", new ResourceLocationArgument()).suggests((context1, builder) -> SharedSuggestionProvider.suggest(context.holderLookup(CTerrainGeneration.MAP_ZONES_REGISTRY).listElements().map(h -> h.key().location().toString()), builder))
                                                        .executes(
                                                                commandContext -> locate(
                                                                        commandContext.getSource(),
                                                                        commandContext.getSource().getLevel(),
                                                                        ResourceLocationArgument.getId(commandContext, "dest")
                                                                )
                                                        )
                                        )
                        ).build();

        rootNode.addChild(locateNode);
    }

    private static int locate(CommandSourceStack source, ServerLevel level, ResourceLocation zoneId) throws CommandSyntaxException {
        if (level.getChunkSource().getGenerator() instanceof MapBasedChunkGenerator generator) {
            Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);

            Zone zone = level.registryAccess().registryOrThrow(CTerrainGeneration.MAP_ZONES_REGISTRY).getOptional(zoneId).orElseThrow();

            MapSettings settings = generator.getSettings();
            BufferedImage image = generator.getSettings().getMapImage();
            int targetColor = zone.color();

            Vec3 pos = source.getPosition();
            int x = settings.xOffset(((int) pos.x) >> 2);
            int z = settings.yOffset(((int) pos.y) >> 2);
            // still search in image, even if player is outside of it
            if (x < 0 || x > image.getWidth()) x = 0;
            if (z < 0 || z > image.getHeight()) z = 0;
            Point start = new Point(x, z);

            Point located = locateColor(image, start, targetColor);

            if (located != null) {
                showResult(source, located, zoneId.toString(), stopwatch.elapsed());
            } else {
                source.sendFailure(Component.translatable("ctgen.commands.locate.failure", zoneId));
            }
        } else {
            throw CTGCommand.INVALID_CHUNK_GENERATOR.create();
        }

        return 1;
    }

    private static final Point[] DIRECTIONS = {
            new Point(2, 0),   // Right
            new Point(0, 2),   // Down
            new Point(-2, 0),  // Left
            new Point(0, -2)   // Up
    };

    public static Point locateColor(BufferedImage image, Point startPoint, int targetColor) {
        int width = image.getWidth();
        int height = image.getHeight();

        Queue<Point> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();

        queue.add(startPoint);

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (!visited.contains(current)) {
                visited.add(current);

                if (current.x >= 0 && current.x < width && current.y >= 0 && current.y < height) {
                    int currentColor = image.getRGB(current.x, current.y);

                    if (currentColor == targetColor) {
                        return current;
                    }

                    for (Point dir : DIRECTIONS) {
                        int newX = current.x + dir.x;
                        int newY = current.y + dir.y;

                        queue.add(new Point(newX, newY));
                    }
                }
            }
        }

        return null;
    }

    private static void showResult(
            CommandSourceStack source,
            Point pixelPos,
            String elementName,
            Duration duration
    ) {
        Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable("ctgen.coordinates", pixelPos.x, pixelPos.y))
                .withStyle(
                        style -> style.withColor(ChatFormatting.GREEN)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ctgen teleport @s " + pixelPos.x + " " + pixelPos.y))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
                );
        source.sendSuccess(() -> Component.translatable("ctgen.commands.locate.success", elementName, component), false);
        LogUtils.getLogger().info("Locating element {} took {} ms", elementName, duration.toMillis());
    }
}
