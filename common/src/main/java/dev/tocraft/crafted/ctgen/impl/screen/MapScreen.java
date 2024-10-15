package dev.tocraft.crafted.ctgen.impl.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class MapScreen extends Screen {
    private final ResourceLocation mapId;

    public MapScreen(Minecraft minecraft, ResourceLocation mapId) {
        super(Component.literal("CTGen"));
        this.mapId = mapId;
        super.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        assert minecraft != null;

        renderBackground(context);

        context.drawString(minecraft.font, Component.literal(mapId != null ? "Using map: " + mapId : "Not using CTGen for Chunk Generation!"), width / 3, height / 2, 0xffffff);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        onClose();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
