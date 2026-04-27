package me.retucio.sputnik.ui.widgets.buttons.settings;

import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.widgets.frames.SettingsFrame;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.KeyUtil;
import me.retucio.sputnik.util.render.DrawUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ColorButton extends SettingButton<ColorSetting> {

    public ColorButton(ColorSetting setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        // fondo
        Color bgColor = isHovered(mouseX, mouseY)
                ? Colors.buttonColor.brighter()
                : Colors.buttonColor;

        gui.fill(x, y, x + w, y + h, bgColor.getRGB());

        // prev. del color
        int previewSize = h - 6;
        int previewX = x + 5;
        int previewY = y + 3;

        DrawUtil.drawBorder(gui,
                previewX - 1, previewY - 1, previewSize + 2, previewSize + 2,
                Colors.withAlpha(Colors.getOppositeColor(setting.getValue(), true), 255).getRGB()
        );

        gui.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, setting.getValue().getRGB());

        // texto
        String label = setting.getName();
        int textX = previewX + previewSize + 5;
        gui.text(mc.font, label, textX, y + 3, -1, true);

        // valor actual
        String valueText = setting.isRainbow() ? "gay." : Colors.ARGBtoHex(setting.getA(), setting.getR(), setting.getG(), setting.getB());
        int valueWidth = mc.font.width(valueText);
        gui.text(mc.font, valueText, x + w - valueWidth - 5, y + 3, -1, true);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) {
                // clic izq.: abrir selector de colores
                ClickGUI.INSTANCE.openColorPickerFrame(
                        setting.getSg().getModule(), setting,
                        parent.getX() + parent.getW() + 120,
                        parent.getRenderY() + offset);
            } else if (button == 1 && KeyUtil.isShiftDown()) {
                // shift + clic dcho.: restablecer color
                setting.reset();
            }
        }
    }

    @Override
    public void drawTooltip(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal(setting.getDescription()));
            tooltip.add(setting.getTooltipText());

            ctx.setComponentTooltipForNextFrame(mc.font, tooltip, mouseX, mouseY + 20);
        }
    }
}
