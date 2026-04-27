package me.retucio.sputnik.module.modules.inventory;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.input.KeyEvent;
import me.retucio.sputnik.mixin.accessors.AbstractContainerScreenAccessor;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.ui.screen.PreviewScreen;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.InventoryUtil;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.HashMap;


public class ShulkerPeek extends Module {

    public final KeySetting previewKey = sgGeneral.add(new KeySetting("tecla de previsualización", "tecla a mantener para previsualizar", GLFW.GLFW_KEY_LEFT_ALT));
    public final BooleanSetting showTooltips = sgGeneral.add(new BooleanSetting("mostrar tooltips", "añadir texto a los tooltips (cajas de texto) de los shulkers", true));

    public static final HashMap<Item, Color> SHULKER_COLORS = new HashMap<>();

    public ShulkerPeek() {
        super("prev. de shulkers",
                "te permite previsualizar el contenido de shulkers desde el inventario",
                Category.INVENTORY);

        // colores y tal (https://github.com/kgriff0n/shulker-preview)
        SHULKER_COLORS.put(Items.SHULKER_BOX, Colors.LAVENDER);
        SHULKER_COLORS.put(Items.WHITE_SHULKER_BOX, Colors.WHITE);
        SHULKER_COLORS.put(Items.LIGHT_GRAY_SHULKER_BOX, Colors.SILVER);
        SHULKER_COLORS.put(Items.GRAY_SHULKER_BOX, Colors.GRAY);
        SHULKER_COLORS.put(Items.BLACK_SHULKER_BOX, Colors.BLACK);
        SHULKER_COLORS.put(Items.BROWN_SHULKER_BOX, Colors.BROWN);
        SHULKER_COLORS.put(Items.RED_SHULKER_BOX, Colors.RED);
        SHULKER_COLORS.put(Items.ORANGE_SHULKER_BOX, Colors.ORANGE);
        SHULKER_COLORS.put(Items.YELLOW_SHULKER_BOX, Colors.YELLOW);
        SHULKER_COLORS.put(Items.LIME_SHULKER_BOX, Colors.LIME);
        SHULKER_COLORS.put(Items.GREEN_SHULKER_BOX, Colors.GREEN);
        SHULKER_COLORS.put(Items.CYAN_SHULKER_BOX, Colors.CYAN);
        SHULKER_COLORS.put(Items.LIGHT_BLUE_SHULKER_BOX, Colors.CELESTE);
        SHULKER_COLORS.put(Items.BLUE_SHULKER_BOX, Colors.BLUE);
        SHULKER_COLORS.put(Items.PURPLE_SHULKER_BOX, Colors.PURPLE);
        SHULKER_COLORS.put(Items.MAGENTA_SHULKER_BOX, Colors.MAGENTA);
        SHULKER_COLORS.put(Items.PINK_SHULKER_BOX, Colors.PINK);
    }

    @EventListener
    private void onKey(KeyEvent event) {
        if (mc.player == null) return;
        if (event.getKey() != previewKey.getValue()) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            openPreviewScreen();
            return;
        }

        if (event.getAction() == GLFW.GLFW_REPEAT) {
            if (mc.screen instanceof PreviewScreen preview) {
                if (preview.getType() != PreviewScreen.PreviewType.SHULKER)
                    openPreviewScreen();
            } else openPreviewScreen();
            return;
        }

        if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (mc.screen instanceof PreviewScreen preview)
                preview.onClose();
        }
    }

    private void openPreviewScreen() {
        ItemStack stack;

        if (mc.screen instanceof PreviewScreen previewScreen) {
            int focusedSlot = previewScreen.getFocusedSlot();
            if (focusedSlot == -1) return;
            stack = previewScreen.getInventory().get(focusedSlot);
        } else if (mc.screen instanceof AbstractContainerScreen<?> handledScreen) {
            stack = ((AbstractContainerScreenAccessor) handledScreen).getHoveredSlot().getItem();
        } else return;

        if (stack.getItem() == Items.ENDER_CHEST) {
            if (InventoryUtil.getEchestInv() == null) {
                ChatUtil.warn("abre un enderchest primero");
                return;
            }
            mc.setScreen(new PreviewScreen(InventoryUtil.getEchestInv(), mc.screen));
            return;
        }

        else if (isShulkerEmpty(stack)) return;
        mc.setScreen(new PreviewScreen(stack, mc.screen));
    }

    public static boolean isShulkerEmpty(ItemStack stack) {
        if (stack == null) return true;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return true;
        if (!(blockItem.getBlock() instanceof ShulkerBoxBlock)) return true;

        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container == null) return true;

        // .stream()?
        return container.allItemsCopyStream().allMatch(ItemStack::isEmpty);
    }
}
