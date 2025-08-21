package me.retucio.camtweaks.ui.settings;

import me.retucio.camtweaks.module.modules.GUI;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.gui.DrawContext;

public class ClickGUISettingsFrame extends SettingsFrame {

    public static final GUI guiSettings = new GUI();
    private boolean extended = false;
    private String title = "ajustes de la interfaz -";

    public ClickGUISettingsFrame(int x, int y, int w, int h) {
        super(guiSettings, x, y, w, h);
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
            }
        }

        if (!extended) return;  // solo dejar clicar en los módulos si el marco está extendido
        for (SettingButton settingButton : settingButtons)
            settingButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateWidth();
        ctx.fill(x, y, x + w, y + h, Colors.frameHeadColor);

        ctx.drawText(mc.textRenderer, title,
                x + (w / 2) - (mc.textRenderer.getWidth(title) / 2),
                y + (h / 2) - (mc.textRenderer.fontHeight / 2),
                -1, false);

        if (extended) {
            int totalHeight = settingButtons.size() * h + 3;
            ctx.fill(x, y + h + 1, x + w, y + h + totalHeight, Colors.frameBGColor);

            for (SettingButton button : settingButtons)
                button.render(ctx, mouseX, mouseY, delta);
        }
    }
}