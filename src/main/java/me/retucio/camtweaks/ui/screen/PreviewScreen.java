package me.retucio.camtweaks.ui.screen;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.module.modules.ShulkerPeek;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PreviewScreen extends Screen {

    private static final Identifier TEXTURE = Identifier.of(CameraTweaks.MOD_ID, "textures/gui/preview.png");

    private int x, y;
    private final int bgWidth = 176;
    private final int bgHeight = 78;

    private final Color color;
    private final Text title;
    private final List<ItemStack> inventory;
    private final Screen parent;
    private final PreviewType type;

    private int focusedSlot;

    public PreviewScreen(ItemStack shulker, Screen parent) {
        super(Text.literal("previsualización del shulker"));
        this.color = ShulkerPeek.SHULKER_COLORS.get(shulker.getItem());
        this.title = shulker.getName();
        this.inventory = shulker.get(DataComponentTypes.CONTAINER).stream().toList();
        this.parent = parent;
        this.type = PreviewType.SHULKER;
    }

    public PreviewScreen(Inventory inventory, Screen parent) {
        super(Text.literal("echest"));
        this.color = Colors.PURPLE;
        this.title = Text.of("echest");
        this.inventory = new ArrayList<>();
        this.parent = parent;
        this.type = PreviewType.ECHEST;

        for (int i = 0; i < inventory.size(); i++)
            this.inventory.add(inventory.getStack(i));
    }

    @Override
    protected void init() {
        this.x = (width - bgWidth) / 2;
        this.y = (height - bgHeight) / 2;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        this.renderBackground(ctx, mouseX, mouseY, delta);
        this.renderItems(ctx, inventory, x + 8, y + 18);

        int selectedSlot = getSlot(mouseX, mouseY);
        if (selectedSlot > -1 && selectedSlot < inventory.size() && !inventory.get(selectedSlot).isOf(Items.AIR))
            renderTooltip(ctx, inventory.get(selectedSlot), mouseX, mouseY);
        focusedSlot = selectedSlot;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, Colors.hudEditorScreenBackgroundColor.getRGB());
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, bgWidth, bgHeight, 256, 256, color.getRGB());
        ctx.drawText(textRenderer, title, x + 8, y + 6, Colors.instructionsTextColor.getRGB(), false);
    }

    private void renderItems(DrawContext context, List<ItemStack> inventory, int x, int y) {
        int baseX = x;
        int count = 0;
        for (ItemStack item : inventory) {
            count++;
            context.drawItem(item, x, y);
            context.drawStackOverlay(textRenderer, item, x, y);
            x += 18;
            if (count % 9 == 0) {
                x = baseX;
                y += 18;
            }
        }
    }

    private int getSlot(int i, int j) {
        int x = this.x + 7;
        int y = this.y + 17;

        int slotX = (i - x) / 18;
        int slotY = (j - y) / 18;

        if (i < x || j < y || i > x + 9 * 18 - 1 || j > y + 3 * 18 - 1)
            return -1;

        return slotX + slotY * 9;
    }

    private void renderTooltip(DrawContext ctx, ItemStack stack, int x, int y) {
        ctx.drawItemTooltip(textRenderer, stack, x, y);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void applyBlur(DrawContext ctx) {
        if (ClientSettingsFrame.guiSettings.blur.isEnabled()) super.applyBlur(ctx);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public PreviewType getType() {
        return this.type;
    }

    public Screen getParent() {
        return parent;
    }

    public int getFocusedSlot() {
        return focusedSlot;
    }

    public List<ItemStack> getInventory() {
        return inventory;
    }

    public enum PreviewType {
        SHULKER,
        ECHEST;
    }
}
