package me.retucio.sputnik.ui.widgets.panels.settings;

import me.retucio.sputnik.config.ConfigManager;
import me.retucio.sputnik.friend.Friend;
import me.retucio.sputnik.friend.FriendManager;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.Friends;
import me.retucio.sputnik.module.setting.settings.StringSetting;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.buttons.settings.TextButton;
import me.retucio.sputnik.ui.widgets.panels.SettingsPanel;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;


public class FriendSettingsPanel extends SettingsPanel {

    private final Friend friend;
    private final StringSetting nickname;
    public final Module dummyModule;

    private int removeButtonY;

    public FriendSettingsPanel(Friend friend, int x, int y, int w, int h) {
        super(ModuleManager.INSTANCE.getModuleByClass(Friends.class), x, y, w, h);

        this.friend = friend;

        dummyModule = new Module(friend.getName(), "gestiona a " + friend.getName(), Category.CLIENT) {
            @Override public void onEnable() {}
            @Override public void onDisable() {}
        };

        dummyModule.getSettings().forEach(s -> s.visibility(false));
        dummyModule.shouldSaveSettings(false);

        setModule(dummyModule);

        nickname = new StringSetting("nombre", "con qué nombre guardar al amigo", friend.getName(), 40);
        dummyModule.getSgGeneral().add(nickname);
        nickname.onUpdate(name -> {
            friend.setName(name);
            ConfigManager.getConfig().friends.put(friend.getUuid().toString(), name);
            ConfigManager.save();
        });

        buttons.clear();
        addButton(new TextButton(nickname, this, h));

        setSettingGroups(dummyModule.getSgs());
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mx, int my, float delta) {
        super.render(gui, mx, my, delta);

        removeButtonY = renderY + h + totalHeight + PADDING;
        int buttonH = h - h / 4;

        gui.fill(x, removeButtonY, x + w, removeButtonY + buttonH + PADDING, Colors.panelBgColor.getRGB());

        int color = isRemoveButtonHovered(mx, my)
                ? Colors.disabledToggleButtonColor.brighter().getRGB()
                : Colors.disabledToggleButtonColor.getRGB();

        gui.fill(x + PADDING, removeButtonY, x + w - PADDING, removeButtonY + buttonH, color);

        String label = "eliminar amigo";
        gui.text(mc.font, label,
                x + w / 2 - mc.font.width(label) / 2,
                removeButtonY + (buttonH - mc.font.lineHeight) / 2,
                -1, true);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (button == 0 && isRemoveButtonHovered(mouseX, mouseY)) {
            FriendManager.INSTANCE.remove(friend);
            ClickGui.INSTANCE.closeSettingsPanel(dummyModule);
        }
    }

    private boolean isRemoveButtonHovered(int mouseX, int mouseY) {
        int buttonH = h - h / 4;
        return mouseX > x + PADDING && mouseX < x + w - PADDING
                && mouseY > removeButtonY && mouseY < removeButtonY + buttonH;
    }

    public Friend getFriend() {
        return friend;
    }
}