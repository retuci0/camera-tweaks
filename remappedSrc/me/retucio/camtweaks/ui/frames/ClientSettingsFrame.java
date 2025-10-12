package me.retucio.camtweaks.ui.frames;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.GUISettingsFrameEvent;
import me.retucio.camtweaks.module.modules.GUI;
import me.retucio.camtweaks.ui.ClickGUI;
import me.retucio.camtweaks.ui.buttons.SettingButton;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class ClientSettingsFrame extends SettingsFrame {

    public static final GUI guiSettings = new GUI();
    public boolean extended = false;
    private final String title = "ajustes del mod";

    public ClientSettingsFrame(int x, int y, int w, int h) {
        super(guiSettings, x, y, w, h);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateWidth();
        ctx.fill(x, renderY, x + w, renderY + h, Colors.mainColor.getRGB());

        ctx.drawText(mc.textRenderer, title,
                x + 8,
                renderY + (h / 2) - (mc.textRenderer.fontHeight / 2),
                -1, true);

        ctx.drawText(mc.textRenderer, extended ? "-" : "+",
                x + w - mc.textRenderer.getWidth("+") - 8,
                renderY + (h / 2) - (mc.textRenderer.fontHeight / 2),
                -1, true);

        List<SettingButton> visibleButtons = settingButtons.stream()
                .filter(sb -> sb.getSetting().isVisible() && sb.getSetting().isSearchMatch())
                .toList();

        if (extended) {
            int currentY = renderY + h + 3;
            totalHeight = visibleButtons.size() * h;
            ctx.fill(x, currentY - 2, x + w, currentY + totalHeight, Colors.frameBGColor.getRGB());

            for (SettingButton sb : visibleButtons) {
                sb.setX(x + 4);
                sb.setY(currentY);
                sb.setW(w - 8);
                sb.setH(h - h / 4);
                sb.render(ctx, mouseX, mouseY, delta);
                sb.drawTooltip(ctx, mouseX, mouseY);
                currentY += h;
            }
        } else totalHeight = 0;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            } else if (button == 1) {
                extended = !extended;
                CameraTweaks.EVENT_BUS.post(new GUISettingsFrameEvent.Extend());
            }
        }

        if (!extended) return;  // solo dejar clicar en los módulos si el marco está extendido
        for (SettingButton settingButton : settingButtons)
            settingButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseRelease(double mouseX, double mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
        if (button == 0 && dragging)
            CameraTweaks.EVENT_BUS.post(new GUISettingsFrameEvent.Move());

        super.mouseRelease(mouseX, mouseY, button);
    }
}
