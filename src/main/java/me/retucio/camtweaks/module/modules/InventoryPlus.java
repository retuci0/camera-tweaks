package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.Event;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.KeyEvent;
import me.retucio.camtweaks.event.events.MouseClickEvent;
import me.retucio.camtweaks.mixin.accessor.HandledScreenAccessor;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.KeySetting;
import me.retucio.camtweaks.util.InventoryUtil;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;

public class InventoryPlus extends Module {

    public KeySetting row1key = addSetting(new KeySetting("fila 1", "tecla para mover items a la primera fila", GLFW.GLFW_KEY_A));
    public KeySetting row2key = addSetting(new KeySetting("fila 2", "tecla para mover items a la segunda fila", GLFW.GLFW_KEY_S));
    public KeySetting row3key = addSetting(new KeySetting("fila 3", "tecla para mover items a la tercera fila", GLFW.GLFW_KEY_D));

    public InventoryPlus() {
        super("inventario plus", "mueve ítems del inventario usando solo el teclado");
    }

    @SubscribeEvent
    public void onKey(KeyEvent event) {
        if (event.getAction() == GLFW.GLFW_RELEASE) return;
        processInput(event.getKey(), event);
    }

    @SubscribeEvent
    public void onMouseButton(MouseClickEvent event) {
        if (event.getAction() == GLFW.GLFW_RELEASE) return;
        processInput(event.getButton(), event);
    }

    private void processInput(int inputCode, Event event) {
        if (!(mc.currentScreen instanceof HandledScreen<?> screen) || mc.player == null) return;

        Slot focusedSlot = ((HandledScreenAccessor) screen).getFocusedSlot();
        if (focusedSlot == null) return;

        int containerSlot = focusedSlot.id;
        int hotbarKeyIndex = InventoryUtil.getSlotNumberFromKey(inputCode);
        if (hotbarKeyIndex == -1) return;

        int rowOffset = InventoryUtil.getRowOffset(screen);

        if (KeyUtil.isKeyDown(row1key.getKey())) {
            event.cancel();
            swapWithRow(screen, containerSlot, hotbarKeyIndex, rowOffset);
        }
        else if (KeyUtil.isKeyDown(row2key.getKey())) {
            event.cancel();
            swapWithRow(screen, containerSlot, hotbarKeyIndex, rowOffset + 9);
        }
        else if (KeyUtil.isKeyDown(row3key.getKey())) {
            event.cancel();
            swapWithRow(screen, containerSlot, hotbarKeyIndex, rowOffset + 18);
        }
    }

    private void swapWithRow(HandledScreen<?> screen, int containerSlot, int column, int rowOffset) {
        int targetContainerSlot = InventoryUtil.calculateTargetSlot(screen, containerSlot, column, rowOffset);
        if (targetContainerSlot != -1) {
            InventoryUtil.swapSlots(containerSlot, targetContainerSlot, screen);
        }
    }
}