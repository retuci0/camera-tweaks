package me.retucio.camtweaks.ui.frames;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.GUISettingsFrameEvent;
import me.retucio.camtweaks.module.modules.GUI;
import me.retucio.camtweaks.ui.buttons.SettingButton;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class ClickGUISettingsFrame extends SettingsFrame {

    public static final GUI guiSettings = new GUI();
    public boolean extended = false;
    private String title = "ajustes de la interfaz -";

    public ClickGUISettingsFrame(int x, int y, int w, int h) {
        super(guiSettings, x, y, w, h);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateWidth();
        ctx.fill(x, y, x + w, y + h, Colors.frameHeadColor);

        ctx.drawText(mc.textRenderer, title,
                x + (w / 2) - (mc.textRenderer.getWidth(title) / 2),
                y + (h / 2) - (mc.textRenderer.fontHeight / 2),
                -1, false);

        List<SettingButton> visibleButtons = settingButtons.stream()
                .filter(sb -> sb.getSetting().isVisible())
                .toList();

        if (extended) {
            int currentY = y + h + 3;
            ctx.fill(x, currentY - 2, x + w, currentY + visibleButtons.size() * h, Colors.frameBGColor);

            for (SettingButton sb : visibleButtons) {
                sb.setX(x + 4);
                sb.setY(currentY);
                sb.setW(w - 8);
                sb.setH(h);
                sb.render(ctx, mouseX, mouseY, delta);
                currentY += h;
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            } else if (button == 1) {
                extended = !extended;
                title = extended ? "ajustes de la interfaz -" : "ajustes de la interfaz +";
                CameraTweaks.EVENT_BUS.post(new GUISettingsFrameEvent.Extend());
            }
        }

        if (!extended) return;  // solo dejar clicar en los módulos si el marco está extendido
        for (SettingButton settingButton : settingButtons)
            settingButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseRelease(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            CameraTweaks.EVENT_BUS.post(new GUISettingsFrameEvent.Move());
        }
        super.mouseRelease(mouseX, mouseY, button);
    }
}
