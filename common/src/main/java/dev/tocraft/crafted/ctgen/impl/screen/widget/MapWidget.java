package dev.tocraft.crafted.ctgen.impl.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
    private float minZoom;
    private final float zoomFactor;
    private double zoom = 1;
    private boolean showCursorPos;
    private boolean showPlayer;

    // view frame
    private int textureX;
    private int textureY;
    private int textureWidth;
    private int textureHeight;

    @Nullable
    public static MapWidget ofPacket(Minecraft minecraft, int x, int y, int width, int height, @NotNull SyncMapPacket packet) {
        ResourceLocation mapId = packet.getMapId();
        int xOffset = packet.getXOffset();
        int yOffset = packet.getYOffset();
        int mapWidth = packet.getMapWidth();
        int mapHeight = packet.getMapHeight();
        if (mapId != null) {
            return new MapWidget(minecraft, x, y, width, height, new ResourceLocation(mapId.getNamespace(), "textures/gui/" + mapId.getPath() + ".png"), xOffset, yOffset, mapWidth, mapHeight);
        }
        return null;
    }

    /**
     * @see #ofPacket(Minecraft, int, int, int, int, SyncMapPacket)
     */
    public MapWidget(Minecraft minecraft, int x, int y, int width, int height, ResourceLocation mapId, int xOffset, int yOffset, int mapWidth, int mapHeight) {
        this(minecraft, x, y, width, height, mapId, xOffset, yOffset, mapWidth, mapHeight, 1.1F, 1.0F, 8, true, true);
    }

    /**
     * @see #ofPacket(Minecraft, int, int, int, int, SyncMapPacket)
     */
    @ApiStatus.Internal
    public MapWidget(Minecraft minecraft, int x, int y, int width, int height, ResourceLocation mapId, int xOffset, int yOffset, int mapWidth, int mapHeight, float zoomFactor, float minZoom, int headScale, boolean showCursorPos, boolean showPlayer) {
        super(x, y, width, height, Component.literal("Map Widget"));
        this.minecraft = minecraft;
        this.pixelOffsetX = xOffset;
        this.pixelOffsetY = yOffset;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.ratio = (double) mapWidth / mapHeight;
        this.mapId = mapId;
        this.playerHeadScale = headScale;
        this.minZoom = minZoom;
        this.zoomFactor = zoomFactor;
        this.showCursorPos = showCursorPos;
        this.showPlayer = showPlayer;
        this.textureWidth = width;
        this.textureHeight = height;
        updateMapWidth();
        updateMapHeight();
        this.textureX = x;
        this.textureY = y;
    }

    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
    }

    /**
     * Sets the frame height and the texture height to a new value
     */
    public void setHeight(int height) {
        this.height = height;
        setTextureHeight(height);
    }

    /**
     * Sets the frame width and the texture width to a new value
     */
    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        setTextureWidth(width);
    }

    /**
     * Sets the frame x and the texture x to a new value
     */
    @Override
    public void setX(int x) {
        super.setX(x);
        this.textureX = x;
    }

    /**
     * Sets the frame y and the texture y to a new value
     */
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
        return (int) (textureHeight * zoom);
    }

    /**
     * @return the width of the map texture while rendering (zoom applied)
     */
    public int getScaledMapWidth() {
        return (int) (textureWidth * zoom);
    }

    public void setZoom(double zoom) {
        this.zoom = Math.max(minZoom, zoom);
        updateMapHeight();
        updateMapWidth();
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

    public void setTexturePos(int x, int y) {
        this.textureX = x;
        this.textureY = y;
    }

    public void setTextureSize(int width, int height) {
        if ((double) width / height != ratio) {
            throw new IllegalArgumentException("texture width and height are in the wrong aspect ratio!");
        }
        setTextureWidth(width);
        setTextureHeight(height);
    }

    public void setFramePos(int x, int y) {
        super.setX(x);
        super.setY(y);
    }

    public void setFrameSize(int width, int height) {
        super.setWidth(width);
        this.height = height;
        updateMapHeight();
        updateMapWidth();
    }

    @ApiStatus.Experimental
    public void setTextureWidth(int width) {
        this.textureWidth = width;
        updateMapWidth();
    }

    @ApiStatus.Experimental
    public void setTextureHeight(int height) {
        this.textureHeight = height;
        updateMapHeight();
    }

    private void updateMapWidth() {
        scaledMapWidth = (int) (textureWidth * zoom);
        double d = (double) (width - scaledMapWidth) / 2;
        textureOffsetX = Mth.clamp(textureOffsetX, d, -d); // clamp x offset
        startX = textureX + (int) ((double) (textureWidth - scaledMapWidth) / 2 + textureOffsetX);
    }

    private void updateMapHeight() {
        scaledMapHeight = (int) (textureHeight * zoom);
        double d = (double) (height - scaledMapHeight) / 2;
        textureOffsetY = Mth.clamp(textureOffsetY, d, -d); // clamp y offset
        startY = textureY + (int) ((double) (textureHeight - scaledMapHeight) / 2  + textureOffsetY);
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

        // cut widget
        final double scaleFactor = minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int) (getX() * scaleFactor), (int) (getY() * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));

        // render actual map
        context.blit(mapId, startX, startY,  0, 0, scaledMapWidth, scaledMapHeight, scaledMapWidth, scaledMapHeight);

        if (showPlayer) {
            // calculate pixel pos for the player
            BlockPos blockPos = minecraft.player.blockPosition();
            int pixelX = (blockPos.getX() >> 2) + pixelOffsetX;
            int pixelY = (blockPos.getZ() >> 2) + pixelOffsetY;
            int playerX = (int) (startX + (double) pixelX / mapWidth * scaledMapWidth);
            int playerY = (int) (startY + (double) pixelY / mapHeight * scaledMapHeight);

            int halfHead = playerHeadScale / 2;

            // clamp player head inside map texture
            if (playerX < startX + halfHead) playerX = startX + halfHead;
            if (playerY < startY + halfHead) playerY = startY + halfHead;
            if (playerX > startX - halfHead + scaledMapWidth) playerX = startX - halfHead + scaledMapWidth;
            if (playerY > startY - halfHead + scaledMapHeight) playerY = startY - halfHead + scaledMapHeight;

            // render player head
            ResourceLocation skin = minecraft.player.getSkinTextureLocation();
            context.blit(skin, playerX - halfHead, playerY - halfHead, playerHeadScale, playerHeadScale, 8.0f, 8, 8, 8, 64, 64);
            context.blit(skin, playerX - halfHead, playerY - halfHead, playerHeadScale, playerHeadScale, 40.0f, 8, 8, 8, 64, 64);
        }

        // render cursor position
        if (isHovered && showCursorPos) {
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
            setZoom(zoom * zoomFactor);
        } else if (amount < 0) {
            setZoom(zoom / zoomFactor);
        }

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
