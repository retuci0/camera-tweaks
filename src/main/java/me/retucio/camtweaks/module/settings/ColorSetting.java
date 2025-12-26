package me.retucio.camtweaks.module.settings;

import me.retucio.camtweaks.util.Colors;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ColorSetting extends AbstractSetting {

    private Color color;
    private Consumer<Color> updateListener;

    private Color defaultColor;
    private boolean defaultRainbow;
    private int defaultRainbowSpeed;

    private int r, g, b, a;

    private boolean rainbow;
    private int rainbowSpeed;
    private float saturation;
    private float brightness;

    public ColorSetting(String name, String description, Color defaultColor, boolean rainbow) {
        super(name, description);
        this.color = this.defaultColor = defaultColor;

        this.r = defaultColor.getRed();
        this.g = defaultColor.getGreen();
        this.b = defaultColor.getBlue();
        this.a = defaultColor.getAlpha();

        this.rainbow = rainbow;
        this.defaultRainbow = rainbow;
        this.rainbowSpeed = 1000;
        this.defaultRainbowSpeed = rainbowSpeed;

        this.saturation = 1f;
        this.brightness = 1f;
    }

    public Color getColor() {
        if (rainbow) return Colors.rainbowColor(rainbowSpeed, a, saturation, brightness);
        return color;
    }

    public int getR() {
        return getColor().getRed();
    }

    public void setR(int r) {
        if (this.r != r) {
            this.r = Math.clamp(r, 0, 255);
            updateColorFromRGB();
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(color);
        }
    }

    public int getG() {
        return getColor().getGreen();
    }

    public void setG(int g) {
        if (this.g != g) {
            this.g = Math.clamp(g, 0, 255);
            updateColorFromRGB();
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(color);
        }
    }

    public int getB() {
        return getColor().getBlue();
    }

    public void setB(int b) {
        if (this.b != b) {
            this.b = Math.clamp(b, 0, 255);
            updateColorFromRGB();
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(color);
        }
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        if (this.a != a) {
            this.a = Math.clamp(a, 0, 255);
            updateColorFromRGB();
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(color);
        }
    }

    private void updateColorFromRGB() {
        this.color = new Color(r, g, b, a);
    }

    public void setColor(Color color) {
        if (!this.color.equals(color)) {
            this.color = color;
            this.r = color.getRed();
            this.g = color.getGreen();
            this.b = color.getBlue();
            this.a = color.getAlpha();
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(color);
        }
    }

    public void setRGB(int r, int g, int b) {
        setRGB(r, g, b, this.a);
    }

    public void setRGB(int r, int g, int b, int a) {
        boolean changed = false;

        if (this.r != r) {
            this.r = Math.clamp(r, 0, 255);
            changed = true;
        }
        if (this.g != g) {
            this.g = Math.clamp(g, 0, 255);
            changed = true;
        }
        if (this.b != b) {
            this.b = Math.clamp(b, 0, 255);
            changed = true;
        }
        if (this.a != a) {
            this.a = Math.clamp(a, 0, 255);
            changed = true;
        }

        if (changed) {
            updateColorFromRGB();
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(color);
        }
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        if (this.rainbow != rainbow) {
            this.rainbow = rainbow;
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(getColor());
        }
    }

    public int getRainbowSpeed() {
        return rainbowSpeed;
    }

    public void setRainbowSpeed(int rainbowSpeed) {
        if (this.rainbowSpeed != rainbowSpeed) {
            this.rainbowSpeed = rainbowSpeed;
            fireUpdateEvent();
            if (updateListener != null && rainbow) updateListener.accept(getColor());
        }
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        if (this.saturation != saturation) {
            this.saturation = Math.clamp(saturation, 0f, 1f);
            fireUpdateEvent();
            if (updateListener != null && rainbow) updateListener.accept(getColor());
        }
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        if (this.brightness != brightness) {
            this.brightness = Math.clamp(brightness, 0f, 1f);
            fireUpdateEvent();
            if (updateListener != null && rainbow) updateListener.accept(getColor());
        }
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public int getDefaultR() {
        return defaultColor.getRed();
    }

    public int getDefaultG() {
        return defaultColor.getGreen();
    }

    public int getDefaultB() {
        return defaultColor.getBlue();
    }

    public int getDefaultA() {
        return defaultColor.getAlpha();
    }

    public float getDefaultSaturation() {
        return 1.0f;
    }

    public float getDefaultBrightness() {
        return 1.0f;
    }

    public boolean getDefaultRainbow() {
        return defaultRainbow;
    }

    public int getDefaultRainbowSpeed() {
        return defaultRainbowSpeed;
    }

    public int getRGB() {
        return getColor().getRGB();
    }

    public Text getTooltipText() {
        if (rainbow) return Text.literal("arcoíris");
        return Text.literal(Colors.getFormatting(color) + Colors.ARGBtoHex(a, r, g, b));
    }

    public void reset() {
        this.color = defaultColor;
        this.r = defaultColor.getRed();
        this.g = defaultColor.getGreen();
        this.b = defaultColor.getBlue();
        this.a = defaultColor.getAlpha();
        this.rainbow = false;
        this.rainbowSpeed = 2;
        this.saturation = 1f;
        this.brightness = 1f;
        fireUpdateEvent();
        if (updateListener != null) updateListener.accept(color);
    }

    public void onUpdate(Consumer<Color> listener) {
        this.updateListener = listener;
        if (updateListener != null) updateListener.accept(getColor());
    }

    public Map<String, ?> getConfigValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("r", r);
        map.put("g", g);
        map.put("b", b);
        map.put("a", a);
        map.put("rb", rainbow);
        map.put("rs", rainbowSpeed);
        map.put("sat", saturation);
        map.put("bri", brightness);
        return map;
    }
}