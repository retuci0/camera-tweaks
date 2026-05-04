package me.retucio.sputnik.ui.widgets.panels.settings;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.sputnik.ClientSettingsPanelEvent;
import me.retucio.sputnik.module.modules.client.ClientSettingsModule;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.ui.widgets.panels.SettingsPanel;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ClientSettingsPanel extends SettingsPanel {

    public static final ClientSettingsModule clientSettings = new ClientSettingsModule();
    public boolean extended = false;

    public ClientSettingsPanel(int x, int y, int w, int h) {
        super(clientSettings, x, y, w, h);
        title = "ajustes del mod";
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        updateWidth();

        // botones, fondo, etc.
        if (extended) {
            super.render(gui, mouseX, mouseY, delta);
        }

        // cabezal
        gui.fill(x, renderY, x + w, renderY + h, Colors.mainColor.getRGB());

        gui.text(mc.font, title,
                x + 8,
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                -1, true);

        gui.text(mc.font, extended ? "-" : "+",
                x + w - mc.font.width("+") - 8,
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                -1, true);

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this)) {
            if (button == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            } else if (button == 1) {
                extended = !extended;
                Sputnik.EVENT_BUS.post(new ClientSettingsPanelEvent.Extend());
            }
        }

        // solo dejar clicar en los ajustes si el marco está extendido
        if (!extended) return;

        for (int i = 0; i < settingGroups.size(); i++) {
            SettingGroup group = settingGroups.get(i);
            if (hasVisibleSettingsInGroup(group) && isGroupHeaderHovered(mouseX, mouseY, i)) {
                group.setExtended(!group.isExtended());
                updateVisibleButtonsForGroup(group);
                return;
            }
        }

        for (SettingButton<?> sb : buttons) {
            sb.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        ClickGui.INSTANCE.unselect(this);
        if (button == 0 && dragging)
            Sputnik.EVENT_BUS.post(new ClientSettingsPanelEvent.Move());

        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void drawTooltips(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        if (!extended) return;
        super.drawTooltips(gui, mouseX, mouseY);
    }
}
