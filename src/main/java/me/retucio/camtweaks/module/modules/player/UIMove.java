package me.retucio.camtweaks.module.modules.player;

import me.retucio.camtweaks.mixin.accessor.KeyBindingAccessor;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.ListSetting;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.util.KeyUtil;
import me.retucio.camtweaks.util.Lists;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import java.util.Arrays;
import java.util.List;


public class UIMove extends Module {

    public ListSetting<ScreenHandlerType<?>> screens = addSetting(new ListSetting<>("interfaces", "interfaces en las que te podrás mover",
            Lists.screenList, Lists.allTrue(Lists.screenList), Lists.screenNames));

    private List<KeyBinding> movementKeys;

    private final ScreenHandlerType<?> inventoryHandlerType = ScreenHandlerType.register(
            "player_inventory", GenericContainerScreenHandler::createGeneric9x3);
    private final ScreenHandlerType<?> clickGuiHandlerType = ScreenHandlerType.register(
            "camtweaks_clickgui", GenericContainerScreenHandler::createGeneric9x3);  // 9x3 porque no importa (creo)

    public UIMove() {
        super("moverse en interfaz",
                "te permite seguir usando las teclas de movimiento aún estando en ciertas interfaces",
                Category.PLAYER);

        screens.addOption(inventoryHandlerType, true, "inventario");
        screens.addOption(clickGuiHandlerType, true, "interfaz del mod");

        screens.onUpdate(v -> unpress());
    }

    @Override
    public void onEnable() {
        movementKeys = Arrays.asList(
                mc.options.forwardKey,
                mc.options.backKey,
                mc.options.leftKey,
                mc.options.rightKey,
                mc.options.jumpKey,
                mc.options.sneakKey,
                mc.options.sprintKey);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        unpress();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null || mc.currentScreen == null) return;

        ScreenHandlerType<?> handler = null;
        if (mc.currentScreen == ClickGUI.INSTANCE)
            handler = clickGuiHandlerType;

        if (mc.currentScreen instanceof HandledScreen<?> screen) {
            try {
                handler = screen.getScreenHandler().getType();
            } catch (UnsupportedOperationException e) {
                handler = inventoryHandlerType;
            }
        }

        if (handler == null || !screens.isEnabled(handler)) return;

        for (KeyBinding kb : movementKeys) {
            kb.setPressed(KeyUtil.isKeyDown((
                    (KeyBindingAccessor) kb).getBoundKey().getCode()));
        }
    }

    private void unpress() {
        if (movementKeys == null) return;
        for (KeyBinding kb : movementKeys) {
            kb.setPressed(false);
        }
    }
}
