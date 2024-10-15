package dev.tocraft.crafted.ctgen.impl.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import dev.tocraft.crafted.ctgen.impl.CTGClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class MapScreen extends Screen {
    private static final float MAP_SCALE = 0.875f;
    private final ResourceLocation mapId;
    private double ratio = -1;

    public MapScreen(Minecraft minecraft, ResourceLocation mapId) {
        super(Component.literal("CTGen"));
        if (mapId != null) {
            this.mapId = new ResourceLocation(mapId.getNamespace(), "textures/gui/"  + mapId.getPath() + ".png");
            // calculate map image aspect ratio
            Optional<Resource> optionalResource = minecraft.getResourceManager().getResource(this.mapId);
            if (optionalResource.isPresent()) {
                Resource resource = optionalResource.get();
                try (NativeImage image = NativeImage.read(resource.open())) {
                    this.ratio = (double) image.getWidth() / image.getHeight();
                } catch (IOException e) {
                    LogUtils.getLogger().warn("Couldn't load map image at {}", this.mapId);
                }
            }
        } else {
            this.mapId = null;
        }
        super.init(minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        assert minecraft != null;

        renderBackground(context);

        // render map image
        // check if texture with was found
        if (mapId != null && minecraft.getResourceManager().getResource(mapId).isPresent() && ratio > -1) {
            // use this.height twice so the aspect ratio will be properly handled
            int textureWidth = (int) (ratio * this.height * MAP_SCALE);
            int textureHeight = (int) (this.height / ratio * MAP_SCALE);
            int x = (this.width - textureWidth) / 2;
            int y = (this.height - textureHeight) / 2;
            context.blit(mapId, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        } else {
            // print info why there is no map
            Component text = mapId != null ? Component.translatable("ctgen.screen.no_texture", mapId) : Component.translatable("ctgen.screen.wrong_generation");
            // get text specs
            int textWidth = minecraft.font.width(text.getVisualOrderText());
            int textHeight = minecraft.font.lineHeight;
            context.drawString(minecraft.font, text, (width - textWidth) / 2, (height - textHeight) / 2, 0xffffff);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        onClose();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
