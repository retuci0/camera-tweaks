package me.retucio.camtweaks.ui.frames;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.AbstractSetting;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.ui.buttons.*;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.render.DrawUtil;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;


public class ColorPickerFrame extends SettingsFrame {

    private final ColorSetting colorSetting;
    private Color currentColor;

    // Color picker state
    private boolean pickingHue = false;
    private boolean pickingSaturationBrightness = false;
    private boolean pickingAlpha = false;

    // Picker areas
    private int huePickerX, huePickerY, huePickerWidth, huePickerHeight;
    private int saturationBrightnessPickerX, saturationBrightnessPickerY, saturationBrightnessPickerWidth, saturationBrightnessPickerHeight;
    private int alphaPickerX, alphaPickerY, alphaPickerWidth, alphaPickerHeight;

    // ajustes
    NumberSetting redSetting, greenSetting, blueSetting;
    BooleanSetting rainbowSetting;
    NumberSetting rainbowSpeedSetting;
    NumberSetting saturationSetting, brightnessSetting;

    public final Module dummyModule;

    public ColorPickerFrame(Module module, ColorSetting colorSetting, int x, int y, int w, int h) {
        super(module, x, y, w, h);

        this.colorSetting = colorSetting;
        this.currentColor = colorSetting.getColor();

        dummyModule = new Module("selector de colores", "elige colores") {
            @Override public void onEnable() {}
            @Override public void onDisable() {}
        };

        // ocultar ajustes innecesarios
        dummyModule.getSettings().forEach(s -> s.setVisible(false));
        dummyModule.shouldSaveSettings(false);

        // ajustes de color
        redSetting = new NumberSetting("rojo", "cantidad de rojo", colorSetting.getR(), 0, 255, 1);
        redSetting.setDefaultValue(colorSetting.getDefaultR());
        greenSetting = new NumberSetting("verde", "cantidad de verde", colorSetting.getG(), 0, 255, 1);
        greenSetting.setDefaultValue(colorSetting.getDefaultG());
        blueSetting = new NumberSetting("azul", "cantidad de azul", colorSetting.getB(), 0, 255, 1);
        blueSetting.setDefaultValue(colorSetting.getDefaultB());

        // ajustes de arcoíris
        rainbowSetting = new BooleanSetting("arcoíris", "gay.", colorSetting.isRainbow());
        rainbowSetting.setDefaultValue(colorSetting.getDefaultRainbow());
        rainbowSpeedSetting = new NumberSetting("velocidad del arcoíris", "velocidad de gaming", colorSetting.getRainbowSpeed(), 1, 10000, 2);
        rainbowSpeedSetting.setDefaultValue(colorSetting.getDefaultRainbowSpeed());
        saturationSetting = new NumberSetting("saturación", "intensidad de la homosexualidad", colorSetting.getSaturation(), 0, 1, 0.01f);
        saturationSetting.setDefaultValue(colorSetting.getDefaultSaturation());
        brightnessSetting = new NumberSetting("brillo", "brillo de la homosexualidad", colorSetting.getBrightness(), 0, 1, 0.01f);
        brightnessSetting.setDefaultValue(colorSetting.getDefaultBrightness());

        // añadir ajustes y tal
        dummyModule.addSettings(redSetting, greenSetting, blueSetting, rainbowSpeedSetting, saturationSetting, brightnessSetting, rainbowSetting);
        for (AbstractSetting setting : dummyModule.getSettings()) {
            if (setting instanceof NumberSetting n) n.onUpdate(v -> updateColorFromSettings());
            if (setting instanceof BooleanSetting b) b.onUpdate(v -> updateColorFromSettings());
            rainbowSetting.onUpdate(v -> {
                updateColorFromSettings();
                redSetting.setVisible(!v);
                greenSetting.setVisible(!v);
                blueSetting.setVisible(!v);
                rainbowSpeedSetting.setVisible(v);
                saturationSetting.setVisible(v);
                brightnessSetting.setVisible(v);
            });
        }

        settingButtons.clear();

        Module originalModule = this.module;
        this.module = dummyModule;

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

        this.module = originalModule;
    }


    // RENDERIZADO

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (colorSetting == null) return;
        updateWidth();

        // al parecer este era el problema :/
        for (AbstractSetting setting : dummyModule.getSettings())
            if (setting instanceof NumberSetting ns) ns.setLocked(
                    pickingAlpha || pickingHue || pickingSaturationBrightness);

        currentColor = colorSetting.getColor();

        int padding = 5;

        // cabezal
        int centerY = renderY + h / 2;
        int titleX = x + 8;
        int titleY = centerY - mc.textRenderer.fontHeight / 2;

        String title = colorSetting.getName();
        ctx.fill(x, renderY, x + w, renderY + h, Colors.mainColor.getRGB());
        ctx.drawText(mc.textRenderer, title, titleX, titleY, -1, true);

        // botón de cerrar
        int closeButtonColor = isCloseButtonHovered(mouseX, mouseY)
                ? Color.RED.getRGB()
                : -1;
        ctx.drawText(mc.textRenderer, "×", x + w - mc.textRenderer.getWidth("×") - 8, titleY, closeButtonColor, true);


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

        ctx.fill(bgX1, bgY1, bgX2, bgY2, Colors.frameBGColor.getRGB());

        DrawUtil.drawBorder(ctx, previewX, previewY, previewWidth, previewHeight, -1);

        if (colorSetting.isRainbow()) {
            float hueStep = 1f / previewWidth;
            int stripWidth = 2;
            for (int i = 0; i < previewWidth; i += stripWidth) {
                float hue = i * hueStep;
                Color rainbowColor = Color.getHSBColor(hue, colorSetting.getSaturation(), colorSetting.getBrightness());
                int endX = Math.min(i + stripWidth, previewWidth);
                ctx.fill(previewX + i, previewY, previewX + endX, previewY + previewHeight,
                        (colorSetting.getA() << 24) | (rainbowColor.getRGB() & 0x00FFFFFF));
            }
        } else {
            ctx.fill(previewX, previewY, previewX + previewWidth, previewY + previewHeight, currentColor.getRGB());
        }

        // hex del color actual
        String hexText = colorSetting.isRainbow() ? "gay." : Colors.ARGBtoHex(colorSetting.getA(), colorSetting.getR(), colorSetting.getG(), colorSetting.getB());
        int hexWidth = mc.textRenderer.getWidth(hexText);
        ctx.drawText(mc.textRenderer, hexText, x + w / 2 - hexWidth / 2, previewY + previewHeight / 2 - mc.textRenderer.fontHeight / 2, -1, true);

        // utilizar un gradiente combinado, para mejorar la optimización
        renderSaturationBrightnessGradient(ctx);
        DrawUtil.drawBorder(ctx, saturationBrightnessPickerX, saturationBrightnessPickerY,
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
        DrawUtil.drawCircle(ctx, indicatorX, indicatorY, 3, -1);

        // gradiente del "hue" (no sé cómo se dice en castellano)
        for (int hy = 0; hy < huePickerHeight; hy += 2) {
            float hue = 1.0f - (hy / (float) huePickerHeight);
            Color hueColor = Color.getHSBColor(hue, 1.0f, 1.0f);
            ctx.fill(huePickerX, huePickerY + hy, huePickerX + huePickerWidth,
                    huePickerY + Math.min(hy + 2, huePickerHeight), hueColor.getRGB());
        }

        DrawUtil.drawBorder(ctx, huePickerX, huePickerY, huePickerWidth, huePickerHeight, -1);

        if (!colorSetting.isRainbow()) {
            float[] hsb2 = Color.RGBtoHSB(colorSetting.getR(), colorSetting.getG(), colorSetting.getB(), null);
            int hueIndicatorY = huePickerY + (int)((1 - hsb2[0]) * huePickerHeight);
            DrawUtil.drawCircle(ctx, huePickerX + huePickerWidth / 2, hueIndicatorY, 3, -1);
        }

        renderAlphaGradient(ctx);
        DrawUtil.drawBorder(ctx, alphaPickerX, alphaPickerY, alphaPickerWidth, alphaPickerHeight, -1);
        int alphaIndicatorX = alphaPickerX + (int)((colorSetting.getA() / 255f) * alphaPickerWidth);
        DrawUtil.drawCircle(ctx, alphaIndicatorX, alphaPickerY + alphaPickerHeight / 2, 3, -1);

        totalHeight = previewHeight + saturationBrightnessPickerHeight + alphaPickerHeight + (padding * 4) + h;

        // botones de ajustes
        int startButtonY = alphaPickerY + alphaPickerHeight + padding;
        visibleButtons = settingButtons.stream()
                .filter(sb -> sb.getSetting().isVisible() && sb.getSetting().isSearchMatch())
                .toList();

        for (SettingButton sb : visibleButtons) {
            sb.setX(x + 4);
            sb.setY(startButtonY);
            sb.setW(w - 8);
            sb.setH(h - h / 4);
            sb.render(ctx, mouseX, mouseY, delta);
            startButtonY += h;
        }

        totalHeight += (visibleButtons.size() * h) + padding;
    }

    private void renderSaturationBrightnessGradient(DrawContext ctx) {
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
            ctx.fill(saturationBrightnessPickerX + sx, saturationBrightnessPickerY,
                    saturationBrightnessPickerX + sx + 1, saturationBrightnessPickerY + saturationBrightnessPickerHeight,
                    colorAtX.getRGB());
        }

        // gradiente vertical
        for (int sy = 0; sy < saturationBrightnessPickerHeight; sy++) {
            float brightness = 1 - (sy / (float) saturationBrightnessPickerHeight);
            int alpha = (int)(brightness * 255);
            Color overlay = new Color(0, 0, 0, alpha);
            ctx.fill(saturationBrightnessPickerX, saturationBrightnessPickerY + sy,
                    saturationBrightnessPickerX + saturationBrightnessPickerWidth, saturationBrightnessPickerY + sy + 1,
                    overlay.getRGB());
        }
    }

    private void renderAlphaGradient(DrawContext ctx) {
        // dibujar casilleros alternando el tono de gris para el slider de la opacidad
        DrawUtil.drawCheckerBoard(ctx, alphaPickerX + 1, alphaPickerY, alphaPickerWidth, alphaPickerHeight, 3, 0xFF333333, 0xFF666666);

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
                ctx.fill(alphaPickerX + ax, alphaPickerY,
                        alphaPickerX + stripEndX, alphaPickerY + alphaPickerHeight, overlayColor.getRGB());
            }
        }
    }


    // DETECCIÓN DE INPUTS

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (isCloseButtonHovered(mouseX, mouseY)) {
                ClickGUI.INSTANCE.closeSettingsFrame(this.dummyModule);
                return;
            }
            if (button == 0) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            } else if (button == 1) {
                ClickGUI.INSTANCE.closeSettingsFrame(dummyModule);
            }
        }

        // manejar interacciones del selector de colores
        if (colorSetting != null) {
            if (isSBPickerHovered(mouseX, mouseY)) {
                pickingSaturationBrightness = true;
                updateColorFromPicker((int) mouseX, (int) mouseY);
            }

            if (!colorSetting.isRainbow() && isHuePickerHovered(mouseX, mouseY)) {
                pickingHue = true;
                updateColorFromPicker((int) mouseX, (int) mouseY);
            }
        }

        // opacidad
        if (isAlphaPickerHovered(mouseX, mouseY)) {
            pickingAlpha = true;
            updateColorFromPicker((int) mouseX, (int) mouseY);
        }

        for (SettingButton sb : visibleButtons)
            sb.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
        if (button == 0) {  // soltar todo
            dragging = false;
            pickingHue = false;
            pickingSaturationBrightness = false;
            pickingAlpha = false;
        }

        for (SettingButton sb : visibleButtons)
            sb.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY) {
        if (pickingHue || pickingSaturationBrightness || pickingAlpha)
            updateColorFromPicker((int) mouseX, (int) mouseY);

        for (SettingButton sb : visibleButtons)
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
        rainbow = rainbowSetting.isEnabled();
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

        for (AbstractSetting setting : dummyModule.getSettings()) {
            switch (setting.getName()) {
                case "rojo" -> ((NumberSetting) setting).setValue(colorSetting.getR());
                case "verde" -> ((NumberSetting) setting).setValue(colorSetting.getG());
                case "azul" -> ((NumberSetting) setting).setValue(colorSetting.getB());
                case "opacidad" -> ((NumberSetting) setting).setValue(colorSetting.getA());
                case "arcoíris" -> ((BooleanSetting) setting).setEnabled(colorSetting.isRainbow());
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