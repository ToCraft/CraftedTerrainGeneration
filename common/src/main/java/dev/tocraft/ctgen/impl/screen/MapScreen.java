package dev.tocraft.ctgen.impl.screen;

import dev.tocraft.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.ctgen.impl.screen.widget.MapWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MapScreen extends Screen {
    private static final float MAP_SCALE = 0.875f;
    @Nullable
    private final MapWidget mapWidget;

    public MapScreen(Minecraft minecraft, @NotNull SyncMapPacket packet) {
        super(Component.literal("Map Menu"));
        super.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());

        int ratio = packet.getMapWidth() / packet.getMapHeight();

        int width = (int) (this.width * MAP_SCALE);
        int height = (int) ((float) this.height * MAP_SCALE);
        int x = (this.width - width) / 2;
        int y = (this.height - height) / 2;
        this.mapWidget = MapWidget.ofPacket(minecraft, x, y, width, height, packet);
    }

    /**
     * updates the properties for the map widget in case the screen size changed
     */
    private void setSpecs(@NotNull MapWidget widget) {
        int width = (int) (this.width * MAP_SCALE);
        int height = (int) (this.height * MAP_SCALE);
        int x = (this.width - width) / 2;
        int y = (this.height - height) / 2;

        widget.setX(x);
        widget.setY(y);
        widget.setHeight(height);
        widget.setWidth(width);

        widget.setMinZoom(widget.defaultZoom());
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        if (mapWidget != null) {
            setSpecs(mapWidget);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        assert minecraft != null && minecraft.player != null;

        // transparent background
        renderBackground(context, mouseX, mouseY, delta);

        // check if texture with was found
        if (mapWidget != null && mapWidget.getMapTexId() != null && minecraft.getResourceManager().getResource(mapWidget.getMapTexId()).isPresent()) {
            // widget is disabled -> close screen
            if (!mapWidget.isActive()) {
                onClose();
                return;
            }

            // render map
            mapWidget.render(context, mouseX, mouseY, delta);
        } else {
            // print info why there is no map
            Component text = mapWidget != null ? Component.translatable("ctgen.screen.no_texture", mapWidget.getMapTexId()) : Component.translatable("ctgen.screen.wrong_generation");
            // get text specs
            int textWidth = minecraft.font.width(text.getVisualOrderText());
            int textHeight = minecraft.font.lineHeight;
            context.drawString(minecraft.font, text, (width - textWidth) / 2, (height - textHeight) / 2, 0xffffff);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (mapWidget != null) {
            return mapWidget.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        } else {
            return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (mapWidget != null) {
            return mapWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mapWidget != null) {
            return mapWidget.mouseClicked(mouseX, mouseY, button);
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean bl = super.keyPressed(keyCode, scanCode, modifiers);
        if (bl) {
            return true;
        } else {
            return mapWidget != null && mapWidget.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
