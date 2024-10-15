package dev.tocraft.crafted.ctgen.impl.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class MapScreen extends Screen {
    private static final float MAP_SCALE = 0.875f;
    private static final int PLAYER_HEAD_SCALE = 8;
    private final ResourceLocation mapId;
    private final int xOffset;
    private final int yOffset;
    private final double ratio;
    private final int mapWidth;
    private final int mapHeight;

    public MapScreen(Minecraft minecraft, ResourceLocation mapId, int xOffset, int yOffset, int mapWidth, int mapHeight) {
        super(Component.literal("CTGen"));
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.ratio = (double) mapWidth / mapHeight;
        if (mapId != null) {
            this.mapId = new ResourceLocation(mapId.getNamespace(), "textures/gui/"  + mapId.getPath() + ".png");
        } else {
            this.mapId = null;
        }
        super.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        assert minecraft != null && minecraft.player != null;

        renderBackground(context);

        // render map image
        // check if texture with was found
        if (mapId != null && minecraft.getResourceManager().getResource(mapId).isPresent() && ratio > -1) {

            // use this.height twice so the aspect ratio will be properly handled
            int textureWidth = (int) (ratio * this.height * MAP_SCALE);
            int textureHeight = (int) (this.height / ratio * MAP_SCALE);
            int mapX = (this.width - textureWidth) / 2;
            int mapY = (this.height - textureHeight) / 2;

            // only render the area with the map
            double scaleFactor = minecraft.getWindow().getGuiScale();
            RenderSystem.enableScissor((int) (mapX * scaleFactor), (int) (mapY * scaleFactor), (int) (textureWidth * scaleFactor), (int) (textureHeight * scaleFactor));

            context.blit(mapId, mapX, mapY, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

            // calculate pixel pos for the player
            BlockPos blockPos = minecraft.player.blockPosition();
            int pixelX = (blockPos.getX() >> 2) + xOffset;
            int pixelY = (blockPos.getZ() >> 2) + yOffset;
            int playerX = (int) (mapX + ((double) pixelX / mapWidth * textureWidth));
            int playerY = (int) (mapY + ((double) pixelY / mapHeight * textureHeight));
            renderPlayerHead(minecraft.player, context, playerX, playerY);

            // no more cutting, map is rendered
            RenderSystem.disableScissor();
        } else {
            // print info why there is no map
            Component text = mapId != null ? Component.translatable("ctgen.screen.no_texture", mapId) : Component.translatable("ctgen.screen.wrong_generation");
            // get text specs
            int textWidth = minecraft.font.width(text.getVisualOrderText());
            int textHeight = minecraft.font.lineHeight;
            context.drawString(minecraft.font, text, (width - textWidth) / 2, (height - textHeight) / 2, 0xffffff);
        }
    }

    private static void renderPlayerHead(@NotNull AbstractClientPlayer player, @NotNull GuiGraphics context, int x, int y) {
        context.blit(player.getSkinTextureLocation(), x, y, PLAYER_HEAD_SCALE, PLAYER_HEAD_SCALE, 8.0f, 8, 8, 8, 64, 64);
        context.blit(player.getSkinTextureLocation(), x, y, PLAYER_HEAD_SCALE, PLAYER_HEAD_SCALE, 40.0f, 8, 8, 8, 64, 64);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        onClose();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
