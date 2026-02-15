package me.retucio.sputnik.ui.widgets.frames.settings;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.sputnik.GUISettingsFrameEvent;
import me.retucio.sputnik.module.modules.client.GUI;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.ui.widgets.frames.SettingsFrame;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.DrawContext;

public class ClientSettingsFrame extends SettingsFrame {

    public static final GUI guiSettings = new GUI();
    public boolean extended = false;

    public ClientSettingsFrame(int x, int y, int w, int h) {
        super(guiSettings, x, y, w, h);
        title = "ajustes del mod";
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateWidth();

        // botones, fondo, etc.
        if (extended) {
            super.render(ctx, mouseX, mouseY, delta);
        }

        // cabezal
        ctx.fill(x, renderY, x + w, renderY + h, Colors.mainColor.getRGB());

        ctx.drawText(mc.textRenderer, title,
                x + 8,
                renderY + (h / 2) - (mc.textRenderer.fontHeight / 2),
                -1, true);

        ctx.drawText(mc.textRenderer, extended ? "-" : "+",
                x + w - mc.textRenderer.getWidth("+") - 8,
                renderY + (h / 2) - (mc.textRenderer.fontHeight / 2),
                -1, true);

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            } else if (button == 1) {
                extended = !extended;
                Sputnik.EVENT_BUS.post(new GUISettingsFrameEvent.Extend());
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
        ClickGUI.INSTANCE.unselect(this);
        if (button == 0 && dragging)
            Sputnik.EVENT_BUS.post(new GUISettingsFrameEvent.Move());

        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void drawTooltips(DrawContext ctx, int mouseX, int mouseY) {
        if (!extended) return;
        super.drawTooltips(ctx, mouseX, mouseY);
    }
}
