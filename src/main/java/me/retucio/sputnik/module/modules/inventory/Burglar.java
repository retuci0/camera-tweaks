package me.retucio.sputnik.module.modules.inventory;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.input.KeyEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.KeySetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.InventoryUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;


/**
 * @author retucio
 */

public class Burglar extends Module {

    private final SettingGroup sgKeys = addSg(new SettingGroup("teclas", true));

    private final KeySetting triggerKey = sgKeys.add(new KeySetting(
            "tecla activadora",
            "tecla a mantener para que las otras teclas funcionen",
            GLFW.GLFW_KEY_LEFT_CONTROL
    ));

    private final KeySetting stealKey = sgKeys.add(new KeySetting(
            "robar",
            "tecla para robar todo",
            GLFW.GLFW_KEY_S
    ));

    private final KeySetting dumpKey = sgKeys.add(new KeySetting(
            "dejar",
            "tecla para dejar todo",
            GLFW.GLFW_KEY_D
    ));


    public final BooleanSetting showStealButton = sgGeneral.add(new BooleanSetting(
            "mostrar botón de robar",
            "mostrar un botón para robar todo en los inventarios de contenedores",
            true
    ));

    public final BooleanSetting showDumpButton = sgGeneral.add(new BooleanSetting(
            "mostrar botón de dejar",
            "mostrar un botón para dejar todo en los inventarios de contenedores",
            true
    ));


    public Burglar() {
        super("moro", "roba cofres con más facilidad", Category.INVENTORY);
    }


    @EventListener
    private void onKey(KeyEvent event) {
        if (!triggerKey.isDown() || event.getAction() != GLFW.GLFW_PRESS
                || !(mc.screen instanceof AbstractContainerScreen<?> container)) return;
        if (event.getKey() == stealKey.getValue()) steal(container);
        if (event.getKey() == dumpKey.getValue()) dump(container);
    }

    public void steal(@NonNull AbstractContainerScreen<?> container) {
        AbstractContainerMenu menu = container.getMenu();
        int stacks = menu.slots.size() - 36;

        for (int i = 0; i < stacks; i++) {
            if (!menu.getSlot(i).hasItem()) continue;
            InventoryUtil.quickMove(i, container);
        }
    }

    public void dump(@NonNull AbstractContainerScreen<?> container) {
        AbstractContainerMenu menu = container.getMenu();
        int stacks = menu.slots.size() - 36;
        if (stacks <= 0) return;

        for (int slot = stacks; slot <= menu.slots.size() - 1; slot++) {
            if (!menu.getSlot(slot).hasItem()) continue;
            InventoryUtil.quickMove(slot, container);
        }
    }
}
