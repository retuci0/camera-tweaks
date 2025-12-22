package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.KeyEvent;
import me.retucio.camtweaks.mixin.accessor.HandledScreenAccessor;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.KeySetting;
import me.retucio.camtweaks.ui.screen.PreviewScreen;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.Colors;

import me.retucio.camtweaks.util.MiscUtil;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.HashMap;


public class ShulkerPeek extends Module {

    public KeySetting previewKey = addSetting(new KeySetting("tecla de previsualización", "tecla a mantener para previsualizar", GLFW.GLFW_KEY_LEFT_ALT));
    public BooleanSetting showTooltips = addSetting(new BooleanSetting("mostrar tooltips", "añadir texto a los tooltips (cajas de texto) de los shulkers", true));

    public static final HashMap<Item, Color> SHULKER_COLORS = new HashMap<>();

    public ShulkerPeek() {
        super("prev. de shulkers", "te permite previsualizar el contenido de shulkers desde el inventario");

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

    @SubscribeEvent
    public void onKey(KeyEvent event) {
        if (mc.player == null) return;
        if (event.getKey() != previewKey.getKey()) return;

        if (event.getAction() == GLFW.GLFW_PRESS) {
            openPreviewScreen();
            return;
        }

        if (event.getAction() == GLFW.GLFW_REPEAT) {
            if (mc.currentScreen instanceof PreviewScreen preview) {
                if (preview.getType() != PreviewScreen.PreviewType.SHULKER)
                    openPreviewScreen();
            } else openPreviewScreen();
            return;
        }

        if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (mc.currentScreen instanceof PreviewScreen preview)
                preview.close();
        }
    }

    private void openPreviewScreen() {
        ItemStack stack;

        if (mc.currentScreen instanceof PreviewScreen previewScreen) {
            int focusedSlot = previewScreen.getFocusedSlot();
            if (focusedSlot == -1) return;
            stack = previewScreen.getInventory().get(focusedSlot);
        } else if (mc.currentScreen instanceof HandledScreen<?> handledScreen) {
            stack = ((HandledScreenAccessor) handledScreen).getFocusedSlot().getStack();
        } else return;

        if (stack.getItem() == Items.ENDER_CHEST) {
            if (MiscUtil.getEchestInv() == null) {
                ChatUtil.warn("abre un enderchest primero");
                return;
            }
            mc.setScreen(new PreviewScreen(MiscUtil.getEchestInv(), mc.currentScreen));
            return;
        }

        else if (isShulkerEmpty(stack)) return;
        mc.setScreen(new PreviewScreen(stack, mc.currentScreen));
    }

    public static boolean isShulkerEmpty(ItemStack stack) {
        if (stack == null) return true;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return true;
        if (!(blockItem.getBlock() instanceof ShulkerBoxBlock)) return true;

        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container == null) return true;

        return container.stream().allMatch(ItemStack::isEmpty);
    }
}
