package dev.tocraft.crafted.ctgen.impl.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MapWidget extends AbstractWidget {
    // texture to be used as map
    private final ResourceLocation mapId;
    private final Minecraft minecraft;
    private final int playerHeadScale;

    // received from the server
    private final int pixelOffsetX;
    private final int pixelOffsetY;
    private final int mapWidth;
    private final int mapHeight;
    private final double ratio;

    // rendered map
    private int startX = 0;
    private int startY = 0;
    private int scaledMapWidth = 0;
    private int scaledMapHeight = 0;

    // zoom and offsets
    private double textureOffsetX = 0;
    private double textureOffsetY = 0;
    private final float zoomFactor;
    private double zoom = 1;

    // view frame
    private int textureX;
    private int textureY;
    private int textureWidth;
    private int textureHeight;

    /**
     * @see MapWidgetBuilder
     */
    @ApiStatus.Internal
    public MapWidget(Minecraft minecraft, int x, int y, int width, int height, ResourceLocation mapId, int xOffset, int yOffset, int mapWidth, int mapHeight, float zoomFactor, int headScale) {
        super(x, y, width, height, Component.literal("Map Widget"));
        this.minecraft = minecraft;
        this.pixelOffsetX = xOffset;
        this.pixelOffsetY = yOffset;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.ratio = (double) mapWidth / mapHeight;
        this.mapId = mapId;
        this.playerHeadScale = headScale;
        this.zoomFactor = zoomFactor;
        this.textureWidth = width;
        this.textureHeight = height;
        this.textureX = x;
        this.textureY = y;
    }

    public void setHeight(int height) {
        this.height = height;
        this.textureHeight = height;
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        this.textureWidth = width;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.textureX = x;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.textureY = y;
    }

    /**
     * @return the texture to be used as map
     */
    public ResourceLocation getMapId() {
        return mapId;
    }

    /**
     * @return the aspect ratio for the original map
     */
    public double getRatio() {
        return ratio;
    }

    /**
     * @return the virtual y point, where the map texture starts
     */
    public int getStartY() {
        return startY;
    }

    /**
     * @return the virtual x point, where the map texture starts
     */
    public int getStartX() {
        return startX;
    }

    /**
     * @return the height of the map texture while rendering (zoom applied)
     */
    public int getScaledMapHeight() {
        return scaledMapHeight;
    }

    /**
     * @return the width of the map texture while rendering (zoom applied)
     */
    public int getScaledMapWidth() {
        return scaledMapWidth;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public double getZoom() {
        return zoom;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureY() {
        return textureY;
    }

    public int getTextureX() {
        return textureX;
    }

    public void updateTexture(int x, int y, int width, int height) {
        this.textureX = x;
        this.textureY = y;
        this.textureWidth = width;
        this.textureHeight = height;
    }

    public void updateFrame(int x, int y, int width, int height) {
        super.setX(x);
        super.setY(y);
        super.setWidth(width);
        this.height = height;
    }

    public void updateVars() {
        // apply zoom
        scaledMapWidth = (int) (textureWidth * zoom);
        scaledMapHeight = (int) (textureHeight * zoom);

        // handle offsets
        double d = (double) (width - scaledMapWidth) / 2;
        textureOffsetX = Mth.clamp(textureOffsetX, d, -d); // clamp x offset
        double d2 = (double) (height - scaledMapHeight) / 2;
        textureOffsetY = Mth.clamp(textureOffsetY, d2, -d2); // clamp y offset
        startX = textureX + (int) ((double) (textureWidth - scaledMapWidth) / 2 + textureOffsetX);
        startY = textureY + (int) ((double) (textureHeight - scaledMapHeight) / 2  + textureOffsetY);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        assert minecraft != null && minecraft.player != null;

        updateVars();

        // cut widget
        final double scaleFactor = minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int) (getX() * scaleFactor), (int) (getY() * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));

        // render actual map
        context.blit(mapId, startX, startY,  0, 0, scaledMapWidth, scaledMapHeight, scaledMapWidth, scaledMapHeight);

        // calculate pixel pos for the player
        BlockPos blockPos = minecraft.player.blockPosition();
        int pixelX = (blockPos.getX() >> 2) + pixelOffsetX;
        int pixelY = (blockPos.getZ() >> 2) + pixelOffsetY;
        int playerX = (int) (startX + (double) pixelX / mapWidth * scaledMapWidth);
        int playerY = (int) (startY + (double) pixelY / mapHeight * scaledMapHeight);

        // clamp player head inside map texture
        if (playerX < startX + (playerHeadScale / 2)) playerX = startX + (playerHeadScale / 2);
        if (playerY < startY + (playerHeadScale / 2)) playerY = startY + (playerHeadScale / 2);
        if (playerX > startX - (playerHeadScale / 2) + scaledMapWidth) playerX = startX - (playerHeadScale / 2) + scaledMapWidth;
        if (playerY > startY - (playerHeadScale / 2) + scaledMapHeight) playerY = startY - (playerHeadScale / 2) + scaledMapHeight;

        renderPlayerHead(minecraft.player, context, playerX, playerY);

        // render cursor position
        if (isHovered) {
            int mousePixelX = (int) ((double) (mouseX - startX) / scaledMapWidth * mapWidth);
            int mousePixelY = (int) ((double) (mouseY - startY) / scaledMapHeight * mapHeight);
            Component text = Component.translatable("ctgen.screen.mouse_pos", Component.translatable("ctgen.coordinates", mousePixelX, mousePixelY));

            // resize text
            int textWidth = minecraft.font.width(text);
            PoseStack pose = context.pose();
            pose.pushPose();
            pose.scale(0.75f, 0.75f, 1);
            // render text centered
            context.drawString(minecraft.font, text, (int) (getX() / 0.75f + width / 1.5f - (float) textWidth / 2), (int) ((getY() + (height - (float) height / 8)) / 0.75f), 0xffffff);
            pose.popPose();
        }

        // widget is rendered - no need for the scissors anymore
        RenderSystem.disableScissor();
    }

    private void renderPlayerHead(@NotNull AbstractClientPlayer player, @NotNull GuiGraphics context, int x, int y) {
        context.blit(player.getSkinTextureLocation(), x - (playerHeadScale / 2), y - (playerHeadScale / 2), playerHeadScale, playerHeadScale, 8.0f, 8, 8, 8, 64, 64);
        context.blit(player.getSkinTextureLocation(), x - (playerHeadScale / 2), y - (playerHeadScale / 2), playerHeadScale, playerHeadScale, 40.0f, 8, 8, 8, 64, 64);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean bl = super.keyPressed(keyCode, scanCode, modifiers);
        if (bl) {
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_W || keyCode == GLFW.GLFW_KEY_UP) {
            textureOffsetY += 10; // Move up
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_S || keyCode == GLFW.GLFW_KEY_DOWN) {
            textureOffsetY -= 10; // Move down
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_LEFT) {
            textureOffsetX += 10; // Move left
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_D || keyCode == GLFW.GLFW_KEY_RIGHT) {
            textureOffsetX -= 10; // Move right
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (active && visible && button == 1 && minecraft != null && minecraft.player != null && clicked(mouseX, mouseY)) {
            if (isHovered) {
                // clicked on map
                int mousePixelX = (int) ((mouseX - startX) / scaledMapWidth * mapWidth);
                int mousePixelY = (int) ((mouseY - startY) / scaledMapHeight * mapHeight);
                if (minecraft.player.hasPermissions(2)) {
                    minecraft.player.connection.sendCommand("ctgen teleport " + mousePixelX + " " + mousePixelY);
                    // disable widget
                    this.playDownSound(minecraft.getSoundManager());
                    active = false;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        double oZoom = zoom;

        // Zoom in/out with scrolling
        if (amount > 0) {
            zoom *= zoomFactor;
        } else if (amount < 0) {
            zoom /= zoomFactor;
        }

        zoom = Math.max(1, zoom);

        if (zoom != oZoom) {
            double newZ = zoom / oZoom;
            // apply zoom to offset
            textureOffsetY *= newZ;
            textureOffsetX *= newZ;
            return true;
        }

        return false;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        textureOffsetX += dragX;
        textureOffsetY += dragY;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.POSITION, createNarrationMessage());
    }
}
