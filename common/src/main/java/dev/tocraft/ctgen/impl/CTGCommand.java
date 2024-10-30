package dev.tocraft.ctgen.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tocraft.ctgen.CTerrainGeneration;
import dev.tocraft.ctgen.impl.commands.CTGLocateCommand;
import dev.tocraft.ctgen.impl.commands.CTGTeleportCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CTGCommand {
    public static final SimpleCommandExceptionType INVALID_CHUNK_GENERATOR = new SimpleCommandExceptionType(Component.translatable("ctgen.commands.invalidChunkGenerator"));

    public static void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralCommandNode<CommandSourceStack> rootNode = Commands.literal(CTerrainGeneration.MODID).requires(source -> source.hasPermission(2)).build();
        CTGLocateCommand.register(rootNode, context);
        CTGTeleportCommand.register(rootNode);
        dispatcher.getRoot().addChild(rootNode);
    }
}
