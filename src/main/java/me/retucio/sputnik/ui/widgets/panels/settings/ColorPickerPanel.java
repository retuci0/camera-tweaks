package me.retucio.sputnik.ui.widgets.panels.settings;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.Setting;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.ui.widgets.buttons.*;
import me.retucio.sputnik.ui.widgets.buttons.settings.SliderButton;
import me.retucio.sputnik.ui.widgets.buttons.settings.ToggleButton;
import me.retucio.sputnik.ui.widgets.panels.SettingsPanel;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.render.DrawUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.*;


public class ColorPickerPanel extends SettingsPanel {

    private final ColorSetting colorSetting;
    private Color currentColor;

    private boolean pickingHue = false;
    private boolean pickingSaturationBrightness = false;
    private boolean pickingAlpha = false;

    private int huePickerX, huePickerY, huePickerWidth, huePickerHeight;
    private int saturationBrightnessPickerX, saturationBrightnessPickerY, saturationBrightnessPickerWidth, saturationBrightnessPickerHeight;
    private int alphaPickerX, alphaPickerY, alphaPickerWidth, alphaPickerHeight;

    // ajustes
    NumberSetting redSetting, greenSetting, blueSetting;
    BooleanSetting rainbowSetting;
    NumberSetting rainbowSpeedSetting;
    NumberSetting saturationSetting, brightnessSetting;

    public final Module dummyModule;

    public ColorPickerPanel(Module module, ColorSetting colorSetting, int x, int y, int w, int h) {
        super(module, x, y, w, h);

        this.colorSetting = colorSetting;
        this.currentColor = colorSetting.getValue();

        dummyModule = new Module("selector de colores", "elige colores", Category.CLIENT) {
            @Override public void onEnable() {}
            @Override public void onDisable() {}
        };

        // ocultar ajustes innecesarios
        dummyModule.getSettings().forEach(s -> s.visibility(false));
        dummyModule.shouldSaveSettings(false);

        // ajustes de color
        redSetting = new NumberSetting("rojo", "cantidad de rojo", colorSetting.getR(), 0, 255, 1);
        redSetting.setDefaultValue((double) colorSetting.getDefaultR());
        greenSetting = new NumberSetting("verde", "cantidad de verde", colorSetting.getG(), 0, 255, 1);
        greenSetting.setDefaultValue((double) colorSetting.getDefaultG());
        blueSetting = new NumberSetting("azul", "cantidad de azul", colorSetting.getB(), 0, 255, 1);
        blueSetting.setDefaultValue((double) colorSetting.getDefaultB());

        // ajustes de arcoíris
        rainbowSetting = new BooleanSetting("arcoíris", "gay.", colorSetting.isRainbow());
        rainbowSetting.setDefaultValue(colorSetting.getDefaultRainbow());
        rainbowSpeedSetting = new NumberSetting("velocidad del arcoíris", "velocidad de gaming", colorSetting.getRainbowSpeed(), 1, 10000, 2);
        rainbowSpeedSetting.setDefaultValue((double) colorSetting.getDefaultRainbowSpeed());
        saturationSetting = new NumberSetting("saturación", "intensidad de la homosexualidad", colorSetting.getSaturation(), 0, 1, 0.01f);
        saturationSetting.setDefaultValue((double) colorSetting.getDefaultSaturation());
        brightnessSetting = new NumberSetting("brillo", "brillo de la homosexualidad", colorSetting.getBrightness(), 0, 1, 0.01f);
        brightnessSetting.setDefaultValue((double) colorSetting.getDefaultBrightness());

        // añadir ajustes y tal
        dummyModule.getSgGeneral().addAll(redSetting, greenSetting, blueSetting, rainbowSpeedSetting, saturationSetting, brightnessSetting, rainbowSetting);
        for (Setting<?> setting : dummyModule.getSettings()) {
            setting.onUpdate(v -> updateColorFromSettings());
            rainbowSetting.onUpdate(v -> {
                updateColorFromSettings();
                redSetting.visibility(!v);
                greenSetting.visibility(!v);
                blueSetting.visibility(!v);
                rainbowSpeedSetting.visibility(v);
                saturationSetting.visibility(v);
                brightnessSetting.visibility(v);
            });
        }

        buttons.clear();

        Module originalModule = this.getModule();
        this.setModule(dummyModule);

        // añadir botones
        int offset = h;
        for (var setting : dummyModule.getSettings()) {
            switch (setting) {
                case BooleanSetting b -> {
                    addButton(new ToggleButton(b, this, offset));
                    offset += 18;
                }
                case NumberSetting n -> {
                    addButton(new SliderButton(n, this, offset));
                    offset += 18;
                }
                default -> {}
            }
        }

        this.setModule(originalModule);
    }


    // RENDERIZADO

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        if (colorSetting == null) return;
        updateWidth();

        // al parecer este era el problema :/
        for (Setting<?> setting : dummyModule.getSettings()) {
            if (setting instanceof NumberSetting ns) {
                ns.setLocked(pickingAlpha || pickingHue || pickingSaturationBrightness);
            }
        }

        currentColor = colorSetting.getValue();

        int padding = 5;

        // cabezal
        int centerY = renderY + h / 2;
        int titleX = x + 8;
        int titleY = centerY - mc.font.lineHeight / 2;

        String title = colorSetting.getName();
        gui.fill(x, renderY, x + w, renderY + h, Colors.mainColor.getRGB());
        gui.text(mc.font, title, titleX, titleY, -1, true);

        // botón de cerrar
        int closeButtonColor = isCloseButtonHovered(mouseX, mouseY)
                ? Color.RED.getRGB()
                : -1;
        gui.text(mc.font, "×", x + w - mc.font.width("×") - 8, titleY, closeButtonColor, true);


        int previewX = x + padding;
        int previewY = renderY + h + padding;
        int previewWidth = w - 2 * padding;
        int previewHeight = 30;

        // dimensiones
        saturationBrightnessPickerX = x + padding;
        saturationBrightnessPickerY = previewY + previewHeight + padding;
        saturationBrightnessPickerWidth = w - 2 * padding - 20;
        saturationBrightnessPickerHeight = 80;

        huePickerX = saturationBrightnessPickerX + saturationBrightnessPickerWidth + 5;
        huePickerY = saturationBrightnessPickerY;
        huePickerWidth = 15;
        huePickerHeight = saturationBrightnessPickerHeight;

        alphaPickerX = x + padding;
        alphaPickerY = saturationBrightnessPickerY + saturationBrightnessPickerHeight + padding;
        alphaPickerWidth = w - 2 * padding;
        alphaPickerHeight = 15;

        int buttonAreaHeight = visibleButtons.size() * h;

        int bgX1 = x;
        int bgY1 = renderY + h + 1;
        int bgX2 = x + w;
        int bgY2 = bgY1 + previewHeight + saturationBrightnessPickerHeight + alphaPickerHeight
                + buttonAreaHeight + (padding * 6) - 11;

        gui.fill(bgX1, bgY1, bgX2, bgY2, Colors.panelBgColor.getRGB());

        DrawUtil.drawBorder(gui, previewX, previewY, previewWidth, previewHeight, -1);

        if (colorSetting.isRainbow()) {
            float hueStep = 1f / previewWidth;
            int stripWidth = 2;
            for (int i = 0; i < previewWidth; i += stripWidth) {
                float hue = i * hueStep;
                Color rainbowColor = Color.getHSBColor(hue, colorSetting.getSaturation(), colorSetting.getBrightness());
                int endX = Math.min(i + stripWidth, previewWidth);
                gui.fill(previewX + i, previewY, previewX + endX, previewY + previewHeight,
                        (colorSetting.getA() << 24) | (rainbowColor.getRGB() & 0x00FFFFFF));
            }
        } else {
            gui.fill(previewX, previewY, previewX + previewWidth, previewY + previewHeight, currentColor.getRGB());
        }

        // hex del color actual
        String hexText = colorSetting.isRainbow() ? "gay." : Colors.ARGBtoHex(colorSetting.getA(), colorSetting.getR(), colorSetting.getG(), colorSetting.getB());
        int hexWidth = mc.font.width(hexText);
        gui.text(mc.font, hexText, x + w / 2 - hexWidth / 2, previewY + previewHeight / 2 - mc.font.lineHeight / 2, -1, true);

        // utilizar un gradiente combinado, para mejorar la optimización
        renderSaturationBrightnessGradient(gui);
        DrawUtil.drawBorder(gui, saturationBrightnessPickerX, saturationBrightnessPickerY,
                saturationBrightnessPickerWidth, saturationBrightnessPickerHeight, -1);

        float[] hsb1;
        if (colorSetting.isRainbow()) {
            float hue = (System.currentTimeMillis() % (colorSetting.getRainbowSpeed() * 1000L))
                    / (float) (colorSetting.getRainbowSpeed() * 1000L);
            hsb1 = new float[]{hue, colorSetting.getSaturation(), colorSetting.getBrightness()};
        } else {
            hsb1 = Color.RGBtoHSB(colorSetting.getR(), colorSetting.getG(), colorSetting.getB(), null);
        }

        int indicatorX = saturationBrightnessPickerX + (int)(hsb1[1] * saturationBrightnessPickerWidth);
        int indicatorY = saturationBrightnessPickerY + (int)((1 - hsb1[2]) * saturationBrightnessPickerHeight);
        DrawUtil.drawCircle(gui, indicatorX, indicatorY, 3, -1);

        // gradiente del "hue" (no sé cómo se dice en castellano)
        for (int hy = 0; hy < huePickerHeight; hy += 2) {
            float hue = 1.0f - (hy / (float) huePickerHeight);
            Color hueColor = Color.getHSBColor(hue, 1.0f, 1.0f);
            gui.fill(huePickerX, huePickerY + hy, huePickerX + huePickerWidth,
                    huePickerY + Math.min(hy + 2, huePickerHeight), hueColor.getRGB());
        }

        DrawUtil.drawBorder(gui, huePickerX, huePickerY, huePickerWidth, huePickerHeight, -1);

        if (!colorSetting.isRainbow()) {
            float[] hsb2 = Color.RGBtoHSB(colorSetting.getR(), colorSetting.getG(), colorSetting.getB(), null);
            int hueIndicatorY = huePickerY + (int)((1 - hsb2[0]) * huePickerHeight);
            DrawUtil.drawCircle(gui, huePickerX + huePickerWidth / 2, hueIndicatorY, 3, -1);
        }

        renderAlphaGradient(gui);
        DrawUtil.drawBorder(gui, alphaPickerX, alphaPickerY, alphaPickerWidth, alphaPickerHeight, -1);
        int alphaIndicatorX = alphaPickerX + (int)((colorSetting.getA() / 255f) * alphaPickerWidth);
        DrawUtil.drawCircle(gui, alphaIndicatorX, alphaPickerY + alphaPickerHeight / 2, 3, -1);

        totalHeight = previewHeight + saturationBrightnessPickerHeight + alphaPickerHeight + (padding * 4) + h;

        // botones de ajustes
        int startButtonY = alphaPickerY + alphaPickerHeight + padding;
        visibleButtons = buttons.stream()
                .filter(sb -> sb.getSetting().isVisible() && sb.getSetting().isSearchMatch())
                .toList();

        for (SettingButton<?> sb : visibleButtons) {
            sb.setX(x + 4);
            sb.setY(startButtonY);
            sb.setW(w - 8);
            sb.setH(h - h / 4);
            sb.render(gui, mouseX, mouseY, delta);
            startButtonY += h;
        }

        totalHeight += (visibleButtons.size() * h) + padding;
    }

    private void renderSaturationBrightnessGradient(GuiGraphicsExtractor gui) {
        float hue;
        if (colorSetting.isRainbow()) {
            hue = (System.currentTimeMillis() % (colorSetting.getRainbowSpeed() * 1000L))
                    / (float) (colorSetting.getRainbowSpeed() * 1000L);
        } else {
            float[] hsb = Color.RGBtoHSB(colorSetting.getR(), colorSetting.getG(), colorSetting.getB(), null);
            hue = hsb[0];
        }

        // gradiente horizontal
        for (int sx = 0; sx < saturationBrightnessPickerWidth; sx++) {
            float saturation = sx / (float) saturationBrightnessPickerWidth;
            Color colorAtX = Color.getHSBColor(hue, saturation, 1);
            gui.fill(saturationBrightnessPickerX + sx, saturationBrightnessPickerY,
                    saturationBrightnessPickerX + sx + 1, saturationBrightnessPickerY + saturationBrightnessPickerHeight,
                    colorAtX.getRGB());
        }

        // gradiente vertical
        for (int sy = 0; sy < saturationBrightnessPickerHeight; sy++) {
            float brightness = (sy / (float) saturationBrightnessPickerHeight);
            int alpha = (int)(brightness * 255);
            Color overlay = new Color(0, 0, 0, alpha);
            gui.fill(saturationBrightnessPickerX, saturationBrightnessPickerY + sy,
                    saturationBrightnessPickerX + saturationBrightnessPickerWidth, saturationBrightnessPickerY + sy + 1,
                    overlay.getRGB());
        }
    }

    private void renderAlphaGradient(GuiGraphicsExtractor gui) {
        // dibujar casilleros alternando el tono de gris para el slider de la opacidad
        DrawUtil.drawCheckerBoard(gui, alphaPickerX + 1, alphaPickerY, alphaPickerWidth, alphaPickerHeight, 3, 0xFF333333, 0xFF666666);

        if (colorSetting != null) {
            int alphaWidth = (int)(alphaPickerWidth * (colorSetting.getA() / 255f));

            int stripWidth = 2;
            for (int ax = 0; ax < alphaWidth; ax += stripWidth) {
                float alphaPos = (ax + (float) stripWidth / 2) / (float) alphaPickerWidth;
                int alphaValue = (int)(alphaPos * 255);
                Color overlayColor = !colorSetting.isRainbow() ?
                        new Color(colorSetting.getR(), colorSetting.getG(), colorSetting.getB(), alphaValue) :
                        new Color(255, 255, 255, alphaValue);

                int stripEndX = Math.min(ax + stripWidth, alphaWidth);
                gui.fill(alphaPickerX + ax, alphaPickerY,
                        alphaPickerX + stripEndX, alphaPickerY + alphaPickerHeight, overlayColor.getRGB());
            }
        }
    }


    // DETECCIÓN DE INPUTS

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this)) {
            if (isCloseButtonHovered(mouseX, mouseY)) {
                ClickGui.INSTANCE.closeSettingsPanel(this.dummyModule);
                return;
            }
            if (button == 0) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            } else if (button == 1) {
                ClickGui.INSTANCE.closeSettingsPanel(dummyModule);
            }
        }

        // manejar interacciones del selector de colores
        if (colorSetting != null) {
            if (isSBPickerHovered(mouseX, mouseY)) {
                pickingSaturationBrightness = true;
                updateColorFromPicker(mouseX, mouseY);
            }

            if (!colorSetting.isRainbow() && isHuePickerHovered(mouseX, mouseY)) {
                pickingHue = true;
                updateColorFromPicker(mouseX, mouseY);
            }
        }

        // opacidad
        if (isAlphaPickerHovered(mouseX, mouseY)) {
            pickingAlpha = true;
            updateColorFromPicker(mouseX, mouseY);
        }

        for (SettingButton<?> sb : visibleButtons)
            sb.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        if (button == 0) {  // soltar todo
            dragging = false;
            pickingHue = false;
            pickingSaturationBrightness = false;
            pickingAlpha = false;
        }

        for (SettingButton<?> sb : visibleButtons)
            sb.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY) {
        if (pickingHue || pickingSaturationBrightness || pickingAlpha)
            updateColorFromPicker((int) mouseX, (int) mouseY);

        for (SettingButton<?> sb : visibleButtons)
            sb.mouseDragged(mouseX, mouseY);
    }

    private boolean isSBPickerHovered(double mouseX, double mouseY) {
        return (mouseX >= saturationBrightnessPickerX && mouseX <= saturationBrightnessPickerX + saturationBrightnessPickerWidth &&
                mouseY >= saturationBrightnessPickerY && mouseY <= saturationBrightnessPickerY + saturationBrightnessPickerHeight);
    }

    private boolean isHuePickerHovered(double mouseX, double mouseY) {
        return (mouseX >= huePickerX && mouseX <= huePickerX + huePickerWidth &&
                mouseY >= huePickerY && mouseY <= huePickerY + huePickerHeight);
    }

    private boolean isAlphaPickerHovered(double mouseX, double mouseY) {
        return (mouseX >= alphaPickerX && mouseX <= alphaPickerX + alphaPickerWidth &&
                mouseY >= alphaPickerY && mouseY <= alphaPickerY + alphaPickerHeight);
    }


    // ACTUALIZAR VALORES

    private void updateColorFromPicker(int mouseX, int mouseY) {
        if (colorSetting == null) return;

        if (pickingSaturationBrightness) {
            int x = Math.max(saturationBrightnessPickerX,
                    Math.min(mouseX, saturationBrightnessPickerX + saturationBrightnessPickerWidth));
            int y = Math.max(saturationBrightnessPickerY,
                    Math.min(mouseY, saturationBrightnessPickerY + saturationBrightnessPickerHeight));

            float saturation = (x - saturationBrightnessPickerX) / (float) saturationBrightnessPickerWidth;
            float brightness = 1 - (y - saturationBrightnessPickerY) / (float) saturationBrightnessPickerHeight;

            saturation = Math.max(0, Math.min(1, saturation));
            brightness = Math.max(0, Math.min(1, brightness));

            if (colorSetting.isRainbow()) {  // gaming
                float hue = (System.currentTimeMillis() % (colorSetting.getRainbowSpeed() * 1000L))
                        / (float) (colorSetting.getRainbowSpeed() * 1000L);
                Color newColor = Color.getHSBColor(hue, saturation, brightness);
                colorSetting.setRGB(newColor.getRed(), newColor.getGreen(), newColor.getBlue());
            } else {  // non gaming
                float[] currentHSB = Color.RGBtoHSB(colorSetting.getR(), colorSetting.getG(), colorSetting.getB(), null);
                Color newColor = Color.getHSBColor(currentHSB[0], saturation, brightness);
                colorSetting.setRGB(newColor.getRed(), newColor.getGreen(), newColor.getBlue());
            }

            updateSettingsFromColor();
            return;
        }

        if (pickingHue && !colorSetting.isRainbow()) {
            int y = Math.max(huePickerY,
                    Math.min(mouseY, huePickerY + huePickerHeight));

            float hue = 1 - (y - huePickerY) / (float) huePickerHeight;
            hue = Math.max(0, Math.min(1, hue));

            float[] currentHSB = Color.RGBtoHSB(colorSetting.getR(), colorSetting.getG(), colorSetting.getB(), null);
            Color newColor = Color.getHSBColor(hue, currentHSB[1], currentHSB[2]);
            colorSetting.setRGB(newColor.getRed(), newColor.getGreen(), newColor.getBlue());

            updateSettingsFromColor();
            return;
        }

        if (pickingAlpha) {
            int clampedX = Math.max(alphaPickerX,
                    Math.min(mouseX, alphaPickerX + alphaPickerWidth));

            int alpha = (int) (255 * (clampedX - alphaPickerX) / (float) alphaPickerWidth);
            alpha = Math.max(0, Math.min(255, alpha));

            colorSetting.setA(alpha);
        }
    }

    private void updateColorFromSettings() {
        if (colorSetting == null) return;

        int red, green, blue, alpha;
        boolean rainbow;
        int rainbowSpeed;
        float saturation, brightness;

        red = redSetting.getIntValue();
        green = greenSetting.getIntValue();
        blue = blueSetting.getIntValue();
        alpha = colorSetting.getA();
        rainbow = rainbowSetting.getValue();
        rainbowSpeed = rainbowSpeedSetting.getIntValue();
        saturation = saturationSetting.getFloatValue();
        brightness = brightnessSetting.getFloatValue();

        if (!rainbow) colorSetting.setRGB(red, green, blue, alpha);
        colorSetting.setRainbow(rainbow);
        colorSetting.setRainbowSpeed(rainbowSpeed);
        colorSetting.setSaturation(saturation);
        colorSetting.setBrightness(brightness);
    }

    private void updateSettingsFromColor() {
        if (colorSetting == null) return;

        for (Setting<?> setting : dummyModule.getSettings()) {
            switch (setting.getName()) {
                case "rojo" -> ((NumberSetting) setting).setValue(colorSetting.getR());
                case "verde" -> ((NumberSetting) setting).setValue(colorSetting.getG());
                case "azul" -> ((NumberSetting) setting).setValue(colorSetting.getB());
                case "opacidad" -> ((NumberSetting) setting).setValue(colorSetting.getA());
                case "arcoíris" -> ((BooleanSetting) setting).setValue(colorSetting.isRainbow());
                case "velocidad del arcoíris" -> ((NumberSetting) setting).setValue(colorSetting.getRainbowSpeed());
                case "saturación" -> ((NumberSetting) setting).setValue(colorSetting.getSaturation());
                case "brillo" -> ((NumberSetting) setting).setValue(colorSetting.getBrightness());
            }
        }
    }

    public ColorSetting getColorSetting() {
        return colorSetting;
    }
}