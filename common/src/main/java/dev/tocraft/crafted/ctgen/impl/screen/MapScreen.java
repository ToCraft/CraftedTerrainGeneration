package dev.tocraft.crafted.ctgen.impl.screen;

import dev.tocraft.crafted.ctgen.impl.network.SyncMapPacket;
import dev.tocraft.crafted.ctgen.impl.screen.widget.MapWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MapScreen extends Screen {
    private static final float MAP_SCALE = 0.875f;
    @Nullable
    private final MapWidget mapWidget;

    public MapScreen(Minecraft minecraft, @Nullable SyncMapPacket packet) {
        super(Component.literal("Map Menu"));
        super.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());

        MapWidget widget = packet != null ? MapWidget.ofPacket(minecraft, 0, 0, 0, 0, packet) : null;
        if (widget != null) {
            setSpecs(widget);
            this.mapWidget = widget;
        } else {
            this.mapWidget = null;
        }
    }


    private void setSpecs(@NotNull MapWidget widget) {
        // update widget specs
        int width = (int) (this.height * widget.getRatio() * MAP_SCALE);
        int height = (int) (this.height / widget.getRatio() * MAP_SCALE);
        int x = (this.width - width) / 2;
        int y = (this.height - height) / 2;

        // adapt texture size in case screen size changed
        widget.setTexturePos(x, y);
        widget.setTextureSize(width, height);


        final int frameWidth = (int) (Mth.clamp(widget.getScaledMapWidth(), widget.getTextureWidth(), this.width * MAP_SCALE * MAP_SCALE));
        final int frameHeight = widget.getTextureHeight();

        widget.setFrameSize(frameWidth, frameHeight);

        // calculate frame pos now - requires widget.getStartX, which depends on the frame width
        final int frameX = (int) (Mth.clamp(widget.getStartX(), this.width - this.width * MAP_SCALE, widget.getTextureX()));
        final int frameY = widget.getTextureY();

        widget.setFramePos(frameX, frameY);
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        assert minecraft != null && minecraft.player != null;

        // transparent background
        renderBackground(context);

        // check if texture with was found
        if (mapWidget != null && mapWidget.getMapId() != null && minecraft.getResourceManager().getResource(mapWidget.getMapId()).isPresent()) {
            // widget is disabled -> close screen
            if (!mapWidget.isActive()) {
                onClose();
                return;
            }

            // adjust frame
            setSpecs(mapWidget);

            // render map
            mapWidget.render(context, mouseX, mouseY, delta);
        } else {
            // print info why there is no map
            Component text = mapWidget != null && mapWidget.getMapId() != null ? Component.translatable("ctgen.screen.no_texture", mapWidget.getMapId()) : Component.translatable("ctgen.screen.wrong_generation");
            // get text specs
            int textWidth = minecraft.font.width(text.getVisualOrderText());
            int textHeight = minecraft.font.lineHeight;
            context.drawString(minecraft.font, text, (width - textWidth) / 2, (height - textHeight) / 2, 0xffffff);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mapWidget != null) {
            return mapWidget.mouseScrolled(mouseX, mouseY, delta);
        } else {
            return super.mouseScrolled(mouseX, mouseY, delta);
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
}
