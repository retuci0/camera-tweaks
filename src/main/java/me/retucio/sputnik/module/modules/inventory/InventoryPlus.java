package me.retucio.sputnik.module.modules.inventory;

import com.github.retucio.neutrino.Event;
import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.input.KeyEvent;
import me.retucio.sputnik.event.input.MouseClickEvent;
import me.retucio.sputnik.mixin.accessors.HandledScreenAccessor;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.util.InventoryUtil;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;

public class InventoryPlus extends Module {

    private final KeySetting row1key = sgGeneral.add(new KeySetting("fila 1", "tecla para mover items a la primera fila", GLFW.GLFW_KEY_A));
    private final KeySetting row2key = sgGeneral.add(new KeySetting("fila 2", "tecla para mover items a la segunda fila", GLFW.GLFW_KEY_S));
    private final KeySetting row3key = sgGeneral.add(new KeySetting("fila 3", "tecla para mover items a la tercera fila", GLFW.GLFW_KEY_D));

    public InventoryPlus() {
        super("inventario plus",
                "mueve ítems del inventario usando solo el teclado",
                Category.INVENTORY);
    }

    @EventListener
    private void onKey(KeyEvent event) {
        if (event.getAction() == GLFW.GLFW_RELEASE) return;
        processInput(event.getKey(), event);
    }

    @EventListener
    private void onMouseButton(MouseClickEvent event) {
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

        if (KeyUtil.isKeyDown(row1key.getValue())) {
            event.cancel();
            swapWithRow(screen, containerSlot, hotbarKeyIndex, rowOffset);
        }
        else if (KeyUtil.isKeyDown(row2key.getValue())) {
            event.cancel();
            swapWithRow(screen, containerSlot, hotbarKeyIndex, rowOffset + 9);
        }
        else if (KeyUtil.isKeyDown(row3key.getValue())) {
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