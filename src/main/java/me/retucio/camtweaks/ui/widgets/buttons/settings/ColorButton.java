package me.retucio.camtweaks.ui.widgets.buttons.settings;

import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.ui.widgets.buttons.SettingButton;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.widgets.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import me.retucio.camtweaks.util.render.DrawUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ColorButton extends SettingButton<ColorSetting> {

    public ColorButton(ColorSetting setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // fondo
        Color bgColor = isHovered(mouseX, mouseY)
                ? Colors.buttonColor.brighter()
                : Colors.buttonColor;
        ctx.fill(x, y, x + w, y + h, bgColor.getRGB());

        // prev. del color
        int previewSize = h - 6;
        int previewX = x + 5;
        int previewY = y + 3;

        DrawUtil.drawBorder(ctx, previewX - 1, previewY - 1, previewSize + 2, previewSize + 2, setting.isRainbow() ? Colors.mainColor.getRGB() : -1);
        ctx.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, setting.getColor().getRGB());

        // texto
        String label = setting.getName();
        int textX = previewX + previewSize + 5;
        ctx.drawText(mc.textRenderer, label, textX, y + 3, -1, true);

        // valor actual
        String valueText = setting.isRainbow() ? "gay." : Colors.ARGBtoHex(setting.getA(), setting.getR(), setting.getG(), setting.getB());
        int valueWidth = mc.textRenderer.getWidth(valueText);
        ctx.drawText(mc.textRenderer, valueText, x + w - valueWidth - 5, y + 3, -1, true);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) {
                // clic izq.: abrir selector de colores
                ClickGUI.INSTANCE.openColorPickerFrame(
                        setting.getModule(), setting,
                        parent.getX() + parent.getW() + 120,
                        parent.getRenderY() + offset);
            } else if (button == 1 && KeyUtil.isShiftDown()) {
                // shift + clic dcho.: restablecer color
                setting.reset();
            }
        }
    }

    @Override
    public void drawTooltip(DrawContext ctx, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) {
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(Text.of(setting.getDescription()));
            tooltip.add(setting.getTooltipText());

            ctx.drawTooltip(mc.textRenderer, tooltip, mouseX, mouseY + 20);
        }
    }
}
