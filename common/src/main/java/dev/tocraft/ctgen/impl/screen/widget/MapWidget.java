package dev.tocraft.ctgen.impl.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MapWidget extends AbstractWidget {
    private static final float ZOOM_FACTOR = 1.1F;
    private static final int MOVE_SPEED = 10;

    // generic
    private final Minecraft minecraft;

    // texture to be used as map
    private final ResourceLocation mapId;
    private final boolean pixelsAreChunks;

    // received from the server
    private final int pixelOffsetX;
    private final int pixelOffsetY;
    private final int mapWidth;
    private final int mapHeight;
    private final double ratio;

    // rendered map
    private int zoomedWidth = 0;
    private int zoomedHeight = 0;

    // zoom and offsets
    private double textureOffsetX = 0;
    private double textureOffsetY = 0;
    private float minZoom;
    private double zoom;

    // misc
    private boolean showCursorPos;
    private boolean showPlayer;

    @Nullable
    public static MapWidget ofPacket(Minecraft minecraft, int x, int y, int width, int height, @NotNull SyncMapPacket packet) {
        ResourceLocation mapId = packet.getMapId();
        boolean pixelsAreChunks = packet.isPixelsAreChunks();
        int xOffset = packet.getXOffset();
        int yOffset = packet.getYOffset();
        int mapWidth = packet.getMapWidth();
        int mapHeight = packet.getMapHeight();
        if (mapId != null) {
            return new MapWidget(minecraft, x, y, width, height, ResourceLocation.fromNamespaceAndPath(mapId.getNamespace(), "textures/gui/" + mapId.getPath() + ".png"), pixelsAreChunks, xOffset, yOffset, mapWidth, mapHeight);
        }
        return null;
    }

    /**
     * @see #ofPacket(Minecraft, int, int, int, int, SyncMapPacket)
     */
    public MapWidget(Minecraft minecraft, int x, int y, int width, int height, ResourceLocation mapId, boolean pixelsAreChunks, int xOffset, int yOffset, int mapWidth, int mapHeight) {
        this(minecraft, x, y, width, height, mapId, pixelsAreChunks, xOffset, yOffset, mapWidth, mapHeight, defaultZoom(width, height, mapWidth, mapHeight), true, true);
    }

    /**
     * @see #ofPacket(Minecraft, int, int, int, int, SyncMapPacket)
     */
    @ApiStatus.Internal
    public MapWidget(Minecraft minecraft, int x, int y, int width, int height, ResourceLocation mapId, boolean pixelsAreChunks, int xOffset, int yOffset, int mapWidth, int mapHeight, float minZoom, boolean showCursorPos, boolean showPlayer) {
        super(x, y, width, height, Component.literal("Map Widget"));
        this.minecraft = minecraft;
        this.pixelOffsetX = xOffset;
        this.pixelOffsetY = yOffset;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.ratio = (double) mapWidth / mapHeight;
        this.mapId = mapId;
        this.pixelsAreChunks = pixelsAreChunks;
        this.minZoom = minZoom;
        this.zoom = minZoom;
        this.showCursorPos = showCursorPos;
        this.showPlayer = showPlayer;

        updateZoomedWidth();
        updateZoomedHeight();
        resetTextureOffsets();
    }

    public float defaultZoom() {
        return defaultZoom(width, height, mapWidth, mapHeight);
    }

    private static float defaultZoom(int width, int height, int mapWidth, int mapHeight) {
        return Math.max((float) width / mapWidth, (float) height / mapHeight);
    }

    /**
     * @return the width of the actual map for generation
     */
    public int getMapWidth() {
        return mapWidth;
    }

    /**
     * @return the height of the actual map for generation
     */
    public int getMapHeight() {
        return mapHeight;
    }

    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
        if (zoom < minZoom) {
            zoom = minZoom;
        }
    }

    /**
     * Sets the frame height and the texture height to a new value
     */
    public void setHeight(int height) {
        this.height = height;
        updateZoomedHeight();
    }

    /**
     * Sets the frame width and the texture width to a new value
     */
    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        updateZoomedWidth();
    }

    /**
     * Sets the frame x and the texture x to a new value
     */
    @Override
    public void setX(int x) {
        super.setX(x);
    }

    /**
     * Sets the frame y and the texture y to a new value
     */
    @Override
    public void setY(int y) {
        super.setY(y);
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

    public void resetTextureOffsets() {
        // calculate pixel pos for the player
        int playerX;
        int playerY;
        if (minecraft.player != null) {
            // calculate pixel pos for the player
            BlockPos blockPos = minecraft.player.blockPosition();
            int i = pixelsAreChunks ? 4 : 2;
            int pixelX = (blockPos.getX() >> i) + pixelOffsetX;
            int pixelY = (blockPos.getZ() >> i) + pixelOffsetY;
            playerX = (int) ((double) pixelX / mapWidth * zoomedWidth);
            playerY = (int) ((double) pixelY / mapHeight * zoomedHeight);
        } else {
            playerX = zoomedWidth / 2;
            playerY = zoomedHeight / 2;
        }

        // calculate offset for player pos on texture
        int tX = playerX - width / 2;
        int tY = playerY - height / 2;

        setTextureOffsetX(tX);
        setTextureOffsetY(tY);
    }

    public void setTextureOffsetX(double textureOffsetX) {
        this.textureOffsetX = textureOffsetX;
        updateZoomedWidth();
    }

    public void setTextureOffsetY(double textureOffsetY) {
        this.textureOffsetY = textureOffsetY;
        updateZoomedHeight();
    }

    /**
     * @return the virtual y point, where the map texture starts
     */
    public int getTextureY() {
        return (int) (getY() - textureOffsetY);
    }

    /**
     * @return the virtual x point, where the map texture starts
     */
    public int getTextureX() {
        return (int) (getX() - textureOffsetX);
    }

    /**
     * @return the height of the map texture while rendering (zoom applied)
     */
    public int getZoomedHeight() {
        return zoomedHeight;
    }

    /**
     * @return the width of the map texture while rendering (zoom applied)
     */
    public int getZoomedWidth() {
        return zoomedWidth;
    }

    public void setZoom(double zoom) {
        this.zoom = Math.max(minZoom, zoom);
    }

    public double getZoom() {
        return zoom;
    }

    private void updateZoomedWidth() {
        zoomedWidth = (int) (mapWidth * zoom);
        if (minZoom >= defaultZoom()) {
            textureOffsetX = Mth.clamp(textureOffsetX, 0, Math.max(0, zoomedWidth - width));
        }
    }

    private void updateZoomedHeight() {
        zoomedHeight = (int) (mapHeight * zoom);
        if (minZoom >= defaultZoom()) {
            textureOffsetY = Mth.clamp(textureOffsetY, 0, Math.max(0, zoomedHeight - height));
        }
    }

    public void setShowCursorPos(boolean showCursorPos) {
        this.showCursorPos = showCursorPos;
    }

    public void setShowPlayer(boolean showPlayer) {
        this.showPlayer = showPlayer;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        assert minecraft != null && minecraft.player != null;

        updateZoomedWidth();
        updateZoomedHeight();

        // cut widget
        context.flush();
        final double scaleFactor = minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int) (getX() * scaleFactor), (int) (getY() * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));

        // render actual map
        context.blit(RenderType::guiTextured, mapId, getTextureX(), getTextureY(), 0, 0, zoomedWidth, zoomedHeight, zoomedWidth, zoomedHeight);

        if (showPlayer) {
            // calculate pixel pos for the player
            BlockPos blockPos = minecraft.player.blockPosition();
            int i = pixelsAreChunks ? 4 : 2;
            int pixelX = (blockPos.getX() >> i) + pixelOffsetX;
            int pixelY = (blockPos.getZ() >> i) + pixelOffsetY;
            int playerX = (int) (getTextureX() + (double) pixelX / mapWidth * zoomedWidth);
            int playerY = (int) (getTextureY() + (double) pixelY / mapHeight * zoomedHeight);

            // clamp player head inside map texture
            if (playerX < getTextureX() + 4) playerX = getTextureX() + 4;
            if (playerY < getTextureY() + 4) playerY = getTextureY() + 4;
            if (playerX > getTextureX() - 4 + zoomedWidth) playerX = getTextureX() - 4 + zoomedWidth;
            if (playerY > getTextureY() - 4 + zoomedHeight) playerY = getTextureY() - 4 + zoomedHeight;

            // render player head
            ResourceLocation skin = minecraft.player.getSkin().texture();
            context.blit(RenderType::guiTextured, skin, playerX - 4, playerY - 4, 8.0f, 8, 8, 8, 8, 8, 64, 64);
            context.blit(RenderType::guiTextured, skin, playerX - 4, playerY - 4, 40.0f, 8, 8, 8, 8, 8, 64, 64);
        }

        // render cursor position
        if (isHovered && showCursorPos) {
            int mousePixelX = mousePixelX(mouseX);
            int mousePixelY = mousePixelY(mouseY);
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean bl = super.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == GLFW.GLFW_KEY_W || keyCode == GLFW.GLFW_KEY_UP) {
            textureOffsetY -= MOVE_SPEED; // Move up
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_S || keyCode == GLFW.GLFW_KEY_DOWN) {
            textureOffsetY += MOVE_SPEED; // Move down
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_LEFT) {
            textureOffsetX -= MOVE_SPEED; // Move left
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_D || keyCode == GLFW.GLFW_KEY_RIGHT) {
            textureOffsetX += MOVE_SPEED; // Move right
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_BRACKET || keyCode == GLFW.GLFW_KEY_KP_ADD) {
            zoom(ZOOM_FACTOR, (double) width / 2, (double) height / 2);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_SLASH || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT) {
            zoom(1 / ZOOM_FACTOR, (double) width / 2, (double) height / 2);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (active && visible && button == 1 && minecraft != null && minecraft.player != null && clicked(mouseX, mouseY)) {
            if (isHovered) {
                // clicked on map
                int mousePixelX = mousePixelX(mouseX);
                int mousePixelY = mousePixelY(mouseY);
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

    public int mousePixelX(double mouseX) {
        return (int) ((mouseX - getTextureX()) / zoomedWidth * mapWidth);
    }

    public int mousePixelY(double mouseY) {
        return (int) ((mouseY - getTextureY()) / zoomedHeight * mapHeight);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        double value;

        // Zoom in/out with scrolling
        if (deltaY > 0) {
            value = ZOOM_FACTOR;
        } else if (deltaY < 0) {
            value = 1 / ZOOM_FACTOR;
        } else {
            value = 1;
        }
        if (isHovered) {
            zoom(value, mouseX - getX(), mouseY - getY());
        } else {
            zoom(value, (double) width / 2, (double) height / 2);
        }

        return true;
    }

    private void zoom(double fac, double relX, double relY) {
        double oZoom = zoom;

        setZoom(zoom * fac);

        if (zoom != oZoom) {
            double newZ = zoom / oZoom;

            // Apply zoom, and adjust the texture offset to ensure zoom focuses on the mouse cursor
            textureOffsetX = (textureOffsetX + relX) * newZ - relX;
            textureOffsetY = (textureOffsetY + relY) * newZ - relY;

            updateZoomedHeight();
            updateZoomedWidth();
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        textureOffsetX -= dragX;
        textureOffsetY -= dragY;
        updateZoomedWidth();
        updateZoomedHeight();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
