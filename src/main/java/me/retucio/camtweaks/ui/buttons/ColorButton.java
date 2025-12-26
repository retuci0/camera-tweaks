package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.render.DrawUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.retucio.camtweaks.CameraTweaks.mc;

public class ColorButton extends SettingButton {

    private final ColorSetting setting;

    public ColorButton(ColorSetting setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        // fondo
        Color bgColor = isHovered((int) mouseX, (int) mouseY)
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
        ctx.drawText(parent.mc.textRenderer, label, textX, y + 3, -1, true);

        // valor actual
        String valueText = setting.isRainbow() ? "gay." : Colors.ARGBtoHex(setting.getA(), setting.getR(), setting.getG(), setting.getB());
        int valueWidth = parent.mc.textRenderer.getWidth(valueText);
        ctx.drawText(parent.mc.textRenderer, valueText, x + w - valueWidth - 5, y + 3, -1, true);
    }

    @Override
    public void drawTooltip(DrawContext ctx, double mouseX, double mouseY) {
        if (isHovered((int) mouseX, (int) mouseY)) {
            List<Text> tooltip = new ArrayList<>();
            tooltip.add(Text.of(setting.getDescription()));
            tooltip.add(setting.getTooltipText());

            ctx.drawTooltip(mc.textRenderer, tooltip, (int) mouseX, (int) mouseY + 20);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered((int) mouseX, (int) mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) {
                // click izq.: abrir selector de colores
                ClickGUI.INSTANCE.openColorPickerFrame(setting.getModule(), setting, parent.x + parent.w + 120, parent.renderY + offset);
            } else if (button == 1 && mc.isShiftPressed()) {
                // shift + clic dcho.: restablecer color
                setting.reset();
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY) {}

    public ColorSetting getSetting() {
        return setting;
    }

    public void refreshDummy() {
        // This is called when the color picker frame needs to be refreshed
        // We'll update any linked dummy module in the color picker frame
    }
}