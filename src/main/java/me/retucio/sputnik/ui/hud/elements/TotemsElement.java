package me.retucio.sputnik.ui.hud.elements;

import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.hud.ImageHudElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;

public class TotemsElement extends ImageHudElement {

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
    public void renderInGame(DrawContext ctx, float delta, HUD hud) {
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

        drawItem(ctx, new ItemStack(Items.TOTEM_OF_UNDYING, Math.max(1, count)), count);
    }

    @Override
    public void renderInEditor(DrawContext ctx, HUD hud) {
        int count = 0;
        if (mc.player != null) {
            for (ItemStack stack : mc.player.getInventory()) {
                if (stack.getItem() == Items.TOTEM_OF_UNDYING)
                    count++;
            }
        } else {
            count = 69;
        }

        drawEditorBackground(ctx);
        drawItem(ctx, new ItemStack(Items.TOTEM_OF_UNDYING, Math.max(1, count)), count);
    }

    @Override
    public List<Text> getTooltip() {
        return List.of(Text.of("totems disponibles"));
    }

    private void drawItem(DrawContext ctx, ItemStack stack, int count) {
        ctx.drawItem(stack, x, y);
        if (count > 1) {
            ctx.drawStackOverlay(mc.textRenderer, stack, x, y);
        } else {
            String text = String.valueOf(count);
            int textX = x + 10;
            int textY = y + 9;
            ctx.drawText(
                    mc.textRenderer,
                    text,
                    textX,
                    textY,
                    -1,
                    true
            );
        }
    }
}
