package me.retucio.sputnik.module.modules.inventory;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.util.KeyUtil;
import me.retucio.sputnik.util.Lists;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.Arrays;
import java.util.List;


public class UIMove extends Module {

    public ListSetting<MenuType<?>> screens = sgGeneral.add(new ListSetting<>("interfaces", "interfaces en las que te podrás mover",
            Lists.screenList, Lists.allTrue(Lists.screenList), Lists.screenNames));

    private List<KeyMapping> movementKeys;

    private final MenuType<?> inventoryHandlerType = MenuType.register(
            "player_inventory", ChestMenu::threeRows);
    private final MenuType<?> clickGuiHandlerType = MenuType.register(
            "sputnik_clickgui", ChestMenu::threeRows);  // 9x3 porque no importa (creo)

    public UIMove() {
        super("moverse en interfaz",
                "te permite seguir usando las teclas de movimiento aún estando en ciertas interfaces",
                Category.INVENTORY);

        screens.addOption(inventoryHandlerType, true, "inventario");
        screens.addOption(clickGuiHandlerType, true, "interfaz del mod");

        screens.onUpdate(v -> unpress());
    }

    @Override
    public void onEnable() {
        movementKeys = Arrays.asList(
                mc.options.keyUp,
                mc.options.keyDown,
                mc.options.keyLeft,
                mc.options.keyRight,
                mc.options.keyJump,
                mc.options.keyShift,
                mc.options.keySprint);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        unpress();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.screen == null) return;

        MenuType<?> handler = null;
        if (mc.screen == ClickGUI.INSTANCE)
            handler = clickGuiHandlerType;

        if (mc.screen instanceof AbstractContainerScreen<?> screen) {
            try {
                handler = screen.getMenu().getType();
            } catch (UnsupportedOperationException e) {
                handler = inventoryHandlerType;
            }
        }

        if (handler == null || !screens.isEnabled(handler)) return;

        for (KeyMapping kb : movementKeys) {
            kb.setDown(KeyUtil.isKeyDown(KeyUtil.getKey(kb)));
        }
    }

    private void unpress() {
        if (movementKeys == null) return;
        for (KeyMapping kb : movementKeys) {
            kb.setDown(false);
        }
    }
}
