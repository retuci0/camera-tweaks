package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.ImageHudElement;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.InventoryUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class EchestElement extends ImageHudElement {

    public EchestElement() {
        super("echest", 10, 50);
        reloadTexture();
    }

    @Override
    protected String getImagePath() {
        return "textures/gui/preview-hud.png";
    }

    @Override
    public void renderInGame(GuiGraphicsExtractor gui, float delta, HUD hud) {
        if (!textureLoaded || !isVisible()) return;

        gui.blit(
                RenderPipelines.GUI_TEXTURED,
                textureId,
                x, y,
                0, 0,
                imageWidth, imageHeight,
                imageWidth, imageHeight,
                Colors.PURPLE.getRGB()
        );


        gui.text(
                mc.font,
                "echest",
                x + w / 2 - mc.font.width("echest") / 2,
                y + mc.font.lineHeight - 3,
                Colors.instructionsTextColor.getRGB(),
                false
        );

        renderItems(gui, InventoryUtil.getEchestInv());
    }

    @Override
    public void renderInEditor(GuiGraphicsExtractor gui, HUD hud) {
        if (!textureLoaded) return;

        drawEditorBackground(gui);

        gui.blit(
                RenderPipelines.GUI_TEXTURED,
                textureId,
                x, y,
                0, 0,
                imageWidth, imageHeight,
                imageWidth, imageHeight,
                visible
                        ? Colors.GREEN.getRGB()
                        : Colors.RED.getRGB()
        );

        gui.text(
                mc.font,
                "echest",
                x + w / 2 - mc.font.width("echest") / 2,
                y + mc.font.lineHeight - 3,
                Colors.instructionsTextColor.getRGB(),
                false
        );

        renderItems(gui, InventoryUtil.getEchestInv());
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("echest"));
        tooltip.add(Component.literal("items en tu echest"));
        return tooltip;
    }

    private void renderItems(GuiGraphicsExtractor gui, Container inventory) {
        if (inventory == null) return;

        int drawX = x + 7;
        int drawY = y + 17;
        int startX = drawX;

        int count = 0;
        int rows = 0;

        for (ItemStack item : inventory) {
            if (item.isEmpty()) continue;

            gui.item(item, drawX, drawY);
            gui.itemDecorations(mc.font, item, drawX, drawY);

            drawX += 18;
            count++;

            if (count % 9 == 0) {
                drawX = startX;
                drawY += 18;
                rows++;

                if (rows >= 3) break;
            }
        }

        int itemsDrawn = Math.min(count, 27);
        int columns = Math.min(itemsDrawn, 9);
        int drawnRows = (int) Math.ceil(itemsDrawn / 9.0);

        w = Math.max(columns * 18, w);
        h = Math.max(drawnRows * 18, h);
    }
}