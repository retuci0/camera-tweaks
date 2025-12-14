package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.KeyEvent;
import me.retucio.camtweaks.mixin.accessor.HandledScreenAccessor;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.KeySetting;
import me.retucio.camtweaks.ui.screen.ShulkerPreviewScreen;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;

import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;


/**
 *
 */

public class ShulkerPeek extends Module {

    public KeySetting previewKey = addSetting(new KeySetting("tecla de previsualización", "tecla a mantener para previsualizar", GLFW.GLFW_KEY_LEFT_ALT));
    public BooleanSetting showTooltips = addSetting(new BooleanSetting("mostrar tooltips", "añadir texto a los tooltips (cajas de texto) de los shulkers", true));

    public static final HashMap<Item, Color> SHULKER_COLORS = new HashMap<>();

    public ShulkerPeek() {
        super("prev. de shulkers", "te permite previsualizar el contenido de shulkers desde el inventario");

        // colores y tal (https://github.com/kgriff0n/shulker-preview)
        SHULKER_COLORS.put(Items.SHULKER_BOX, new Color(142, 108, 142));
        SHULKER_COLORS.put(Items.WHITE_SHULKER_BOX, new Color(225, 230, 230));
        SHULKER_COLORS.put(Items.LIGHT_GRAY_SHULKER_BOX, new Color(137, 137, 128));
        SHULKER_COLORS.put(Items.GRAY_SHULKER_BOX, new Color(60, 65, 68));
        SHULKER_COLORS.put(Items.BLACK_SHULKER_BOX, new Color(31, 31, 35));
        SHULKER_COLORS.put(Items.BROWN_SHULKER_BOX, new Color(113, 70, 39));
        SHULKER_COLORS.put(Items.RED_SHULKER_BOX, new Color(152, 36, 34));
        SHULKER_COLORS.put(Items.ORANGE_SHULKER_BOX, new Color(241, 114, 15));
        SHULKER_COLORS.put(Items.YELLOW_SHULKER_BOX, new Color(249, 196, 35));
        SHULKER_COLORS.put(Items.LIME_SHULKER_BOX, new Color(110, 185, 24));
        SHULKER_COLORS.put(Items.GREEN_SHULKER_BOX, new Color(83, 107, 29));
        SHULKER_COLORS.put(Items.CYAN_SHULKER_BOX, new Color(22, 133, 144));
        SHULKER_COLORS.put(Items.LIGHT_BLUE_SHULKER_BOX, new Color(57, 177, 215));
        SHULKER_COLORS.put(Items.BLUE_SHULKER_BOX, new Color(49, 52, 152));
        SHULKER_COLORS.put(Items.PURPLE_SHULKER_BOX, new Color(113, 37, 166));
        SHULKER_COLORS.put(Items.MAGENTA_SHULKER_BOX, new Color(183, 61, 172));
        SHULKER_COLORS.put(Items.PINK_SHULKER_BOX, new Color(239, 135, 166));
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
            if (!(mc.currentScreen instanceof ShulkerPreviewScreen)) {
                openPreviewScreen();
                return;
            }
        }

        if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (mc.currentScreen instanceof ShulkerPreviewScreen preview)
                preview.close();
        }
    }

    private void openPreviewScreen() {
        if (!(mc.currentScreen instanceof HandledScreen<?> prevScreen)) return;

        Slot focusedSlot = ((HandledScreenAccessor) prevScreen).getFocusedSlot();
        if (focusedSlot == null) return;

        ItemStack stack = focusedSlot.getStack();
        if (isShulkerEmpty(stack)) return;

        mc.setScreen(new ShulkerPreviewScreen(stack, prevScreen));
    }

    public static boolean isShulkerEmpty(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return true;
        if (!(blockItem.getBlock() instanceof ShulkerBoxBlock)) return true;

        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container == null) return true;

        return container.stream().allMatch(ItemStack::isEmpty);
    }
}
