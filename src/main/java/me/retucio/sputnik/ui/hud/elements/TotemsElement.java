package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.modules.client.Hud;
import me.retucio.sputnik.ui.hud.ImageHudElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;


public class TotemsElement extends ImageHudElement {

    private static ItemStack TOTEM_PREVIEW = null;

    public TotemsElement() {
        super("totems", 363, 521);
        w = 16;
        h = 16;
        reloadTexture();
    }

    @Override
    protected String getImagePath() {
        return "";
    }

    @Override
    public void renderInGame(GuiGraphicsExtractor gui, float delta, Hud hud) {
        if (!isVisible()) return;

        int count = 0;
        if (mc.player != null) {
            for (ItemStack stack : mc.player.getInventory()) {
                if (stack.getItem() == Items.TOTEM_OF_UNDYING)
                    count++;
            }
        } else {
            count = 69;
        }

        ItemStack stack = getTotemPreview(count);
        if (stack != null) {
            drawItem(gui, stack, count);
        }
    }

    @Override
    public void renderInEditor(GuiGraphicsExtractor gui, Hud hud) {
        int count = 0;
        if (mc.player != null) {
            for (ItemStack stack : mc.player.getInventory()) {
                if (stack.getItem() == Items.TOTEM_OF_UNDYING)
                    count++;
            }
        } else {
            count = 69;
        }

        drawEditorBackground(gui);

        ItemStack stack = getTotemPreview(count);
        if (stack != null) {
            drawItem(gui, stack, count);
        }
    }

    @Override
    public List<Component> getTooltip() {
        return List.of(Component.literal("totems disponibles"));
    }

    private void drawItem(GuiGraphicsExtractor gui, ItemStack stack, int count) {
        gui.item(stack, x, y);
        if (count > 1) {
            gui.itemDecorations(mc.font, stack, x, y);
        } else {
            String text = String.valueOf(count);
            int textX = x + 10;
            int textY = y + 9;
            gui.text(
                    mc.font,
                    text,
                    textX,
                    textY,
                    -1,
                    true
            );
        }
    }

    private static ItemStack getTotemPreview(int count) {
        if (TOTEM_PREVIEW == null) {
            try {
                TOTEM_PREVIEW = new ItemStack(Items.TOTEM_OF_UNDYING, 1);
            } catch (Exception e) {
                return null; // registries not ready yet
            }
        }
        return TOTEM_PREVIEW;
    }
}
