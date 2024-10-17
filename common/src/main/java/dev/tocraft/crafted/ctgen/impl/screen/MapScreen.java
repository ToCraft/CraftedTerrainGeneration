package dev.tocraft.crafted.ctgen.impl.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class MapScreen extends Screen {
    private static final float MAP_SCALE = 0.875f;
    private static final int PLAYER_HEAD_SCALE = 8;
    private static final float ZOOM_FACTOR = 1.1F;

    private final ResourceLocation mapId;

    // received from the server
    private final int pixelOffsetX;
    private final int pixelOffsetY;
    private final double ratio;
    private final int mapWidth;
    private final int mapHeight;

    private int textureWidth = 0;
    private int textureHeight = 0;
    private int textureX = 0;
    private int textureY = 0;

    private int scaledWidth = 0;
    private int scaledHeight = 0;
    private int startX = 0;
    private int startY = 0;

    private double textureOffsetX = 0;
    private double textureOffsetY = 0;
    private double zoom = 1;

    public MapScreen(Minecraft minecraft, ResourceLocation mapId, int xOffset, int yOffset, int mapWidth, int mapHeight) {
        super(Component.literal("CTGen"));
        this.pixelOffsetX = xOffset;
        this.pixelOffsetY = yOffset;
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
        super.render(context, mouseX, mouseY, delta);

        assert minecraft != null && minecraft.player != null;

        renderBackground(context);

        // render map image
        // check if texture with was found
        if (mapIsPresent(minecraft)) {

            // use this.height twice so the aspect ratio will be properly handled
            textureWidth = (int) (ratio * this.height * MAP_SCALE);
            textureHeight = (int) (this.height / ratio * MAP_SCALE);
            textureX = (this.width - textureWidth) / 2;
            textureY = (this.height - textureHeight) / 2;

            // apply zoom and offsets
            scaledWidth = (int) (textureWidth * zoom);
            scaledHeight = (int) (textureHeight * zoom);
            int i = (textureWidth - scaledWidth) / 2;
            textureOffsetX = Mth.clamp(textureOffsetX, i, -i); // clamp x offset
            int i2 = (textureHeight - scaledHeight) / 2;
            textureOffsetY = Mth.clamp(textureOffsetY, i2, -i2); // clamp y offset
            startX = (int) ((double) (this.width - scaledWidth) / 2 + textureOffsetX);
            startY = (int) ((double) (this.height - scaledHeight) / 2  + textureOffsetY);

            // only render the area with the map
            final double scaleFactor = minecraft.getWindow().getGuiScale();
            final int frameX = (int) (Mth.clamp(startX, this.width - this.width * MAP_SCALE, textureX) * scaleFactor);
            final int frameWidth = (int) (Mth.clamp(scaledWidth, textureWidth, this.width * MAP_SCALE * MAP_SCALE) * scaleFactor);
            final int frameY = (int) (textureY * scaleFactor);
            final int frameHeight = (int) (textureHeight * scaleFactor);
            RenderSystem.enableScissor(frameX, frameY, frameWidth, frameHeight);

            context.blit(mapId, startX, startY,  0, 0, scaledWidth, scaledHeight, scaledWidth, scaledHeight);

            // calculate pixel pos for the player
            BlockPos blockPos = minecraft.player.blockPosition();
            int pixelX = (blockPos.getX() >> 2) + pixelOffsetX;
            int pixelY = (blockPos.getZ() >> 2) + pixelOffsetY;
            int playerX = (int) (startX + (double) pixelX / mapWidth * scaledWidth);
            int playerY = (int) (startY + (double) pixelY / mapHeight * scaledHeight);

            // clamp player head inside map texture
            if (playerX < startX + (PLAYER_HEAD_SCALE / 2)) playerX = startX + (PLAYER_HEAD_SCALE / 2);
            if (playerY < startY + (PLAYER_HEAD_SCALE / 2)) playerY = startY + (PLAYER_HEAD_SCALE / 2);
            if (playerX > startX - (PLAYER_HEAD_SCALE / 2) + scaledWidth) playerX = startX - (PLAYER_HEAD_SCALE / 2) + scaledWidth;
            if (playerY > startY - (PLAYER_HEAD_SCALE / 2)+ scaledHeight) playerY = startY - (PLAYER_HEAD_SCALE / 2)+ scaledHeight;

            renderPlayerHead(minecraft.player, context, playerX, playerY);

            // no more cutting, map is rendered
            RenderSystem.disableScissor();

            // render cursor position
            if (mouseX >= textureX && mouseX <= textureX + textureWidth && mouseY >= textureY && mouseY <= textureY + textureHeight) {
                int mousePixelX = (int) ((double) (mouseX - startX) / scaledWidth * mapWidth);
                int mousePixelY = (int) ((double) (mouseY - startY) / scaledHeight * mapHeight);
                Component text = Component.translatable("ctgen.screen.mouse_pos", Component.translatable("ctgen.coordinates", mousePixelX, mousePixelY));
                int textWidth = minecraft.font.width(text);
                PoseStack pose = context.pose();
                pose.pushPose();
                pose.scale(0.75f, 0.75f, 1);
                context.drawString(minecraft.font, text, (int) ((width - textWidth * 0.75f) / 1.5f), (int) ((textureHeight - (float) (height - textureHeight) / 2) / 0.75f), 0xffffff);
                pose.popPose();
            }
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
        context.blit(player.getSkinTextureLocation(), x - (PLAYER_HEAD_SCALE / 2), y - (PLAYER_HEAD_SCALE / 2), PLAYER_HEAD_SCALE, PLAYER_HEAD_SCALE, 8.0f, 8, 8, 8, 64, 64);
        context.blit(player.getSkinTextureLocation(), x - (PLAYER_HEAD_SCALE / 2), y - (PLAYER_HEAD_SCALE / 2), PLAYER_HEAD_SCALE, PLAYER_HEAD_SCALE, 40.0f, 8, 8, 8, 64, 64);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_W || keyCode == GLFW.GLFW_KEY_UP) {
            textureOffsetY += 10; // Move up
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_S || keyCode == GLFW.GLFW_KEY_DOWN) {
            textureOffsetY -= 10; // Move down
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_LEFT) {
            textureOffsetX += 10; // Move left
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_D || keyCode == GLFW.GLFW_KEY_RIGHT) {
            textureOffsetX -= 10; // Move right
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean bl = super.mouseClicked(mouseX, mouseY, button);
        if (bl) return true;
        else {
            if (button == 1 && minecraft != null && minecraft.player != null && mapIsPresent(minecraft)) {
                if (mouseX >= textureX && mouseX <= textureX + textureWidth && mouseY >= textureY && mouseY <= textureY + textureHeight) {
                    // clicked on map
                    int mousePixelX = (int) ((mouseX - startX) / scaledWidth * mapWidth);
                    int mousePixelY = (int) ((mouseY - startY) / scaledHeight * mapHeight);
                    if (minecraft.player.hasPermissions(2)) {
                        minecraft.player.connection.sendCommand("ctgen teleport " + mousePixelX + " " + mousePixelY);
                        onClose();
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        double oZoom = zoom;

        // Zoom in/out with scrolling
        if (amount > 0) {
            zoom *= ZOOM_FACTOR;
        } else if (amount < 0) {
            zoom /= ZOOM_FACTOR;
        }

        zoom = Math.max(1, zoom);

        if (zoom != oZoom) {
            double newZ = zoom / oZoom;
            textureOffsetY *= newZ;
            textureOffsetX *= newZ;
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            textureOffsetX += dragX;
            textureOffsetY += dragY;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean mapIsPresent(Minecraft minecraft) {
        return mapId != null && minecraft.getResourceManager().getResource(mapId).isPresent() && ratio > -1;
    }
}
