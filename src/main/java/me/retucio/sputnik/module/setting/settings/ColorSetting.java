package me.retucio.sputnik.module.setting.settings;

import me.retucio.sputnik.module.setting.Setting;
import me.retucio.sputnik.util.Colors;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ColorSetting extends Setting<Color> {

    private final boolean defaultRainbow;
    private final int defaultRainbowSpeed;

    private int r, g, b, a;

    private boolean rainbow;
    private int rainbowSpeed;
    private float saturation;
    private float brightness;

    public ColorSetting(String name, String description, Color defaultValue, boolean rainbow) {
        super(name, description, defaultValue);

        this.r = defaultValue.getRed();
        this.g = defaultValue.getGreen();
        this.b = defaultValue.getBlue();
        this.a = defaultValue.getAlpha();

        this.rainbow = rainbow;
        this.defaultRainbow = rainbow;
        this.rainbowSpeed = 1000;
        this.defaultRainbowSpeed = rainbowSpeed;

        this.saturation = 1f;
        this.brightness = 1f;
    }

    public int getR() {
        return getValue().getRed();
    }

    public void setR(int r) {
        if (this.r != r) {
            this.r = Math.clamp(r, 0, 255);
            updateColorFromRGB();
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(value);
        }
    }

    public int getG() {
        return getValue().getGreen();
    }

    public void setG(int g) {
        if (this.g != g) {
            this.g = Math.clamp(g, 0, 255);
            updateColorFromRGB();
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(value);
        }
    }

    public int getB() {
        return getValue().getBlue();
    }

    public void setB(int b) {
        if (this.b != b) {
            this.b = Math.clamp(b, 0, 255);
            updateColorFromRGB();
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(value);
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
            if (updateListener != null) updateListener.accept(value);
        }
    }

    private void updateColorFromRGB() {
        this.value = new Color(r, g, b, a);
    }

    @Override
    public Color getValue() {
        if (rainbow) return Colors.rainbowColor(rainbowSpeed, a, saturation, brightness);
        return super.getValue();
    }

    @Override
    public void setValue(Color value) {
        if (!this.value.equals(value)) {
            this.value = value;
            this.r = value.getRed();
            this.g = value.getGreen();
            this.b = value.getBlue();
            this.a = value.getAlpha();
        }

        super.setValue(value);
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
            if (updateListener != null) updateListener.accept(value);
        }
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        if (this.rainbow != rainbow) {
            this.rainbow = rainbow;
            fireUpdateEvent();
            if (updateListener != null) updateListener.accept(getValue());
        }
    }

    public int getRainbowSpeed() {
        return rainbowSpeed;
    }

    public void setRainbowSpeed(int rainbowSpeed) {
        if (this.rainbowSpeed != rainbowSpeed) {
            this.rainbowSpeed = rainbowSpeed;
            fireUpdateEvent();
            if (updateListener != null && rainbow) updateListener.accept(getValue());
        }
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        if (this.saturation != saturation) {
            this.saturation = Math.clamp(saturation, 0f, 1f);
            fireUpdateEvent();
            if (updateListener != null && rainbow) updateListener.accept(getValue());
        }
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        if (this.brightness != brightness) {
            this.brightness = Math.clamp(brightness, 0f, 1f);
            fireUpdateEvent();
            if (updateListener != null && rainbow) updateListener.accept(getValue());
        }
    }

    public int getDefaultR() {
        return defaultValue.getRed();
    }

    public int getDefaultG() {
        return defaultValue.getGreen();
    }

    public int getDefaultB() {
        return defaultValue.getBlue();
    }

    public int getDefaultA() {
        return defaultValue.getAlpha();
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
        return getValue().getRGB();
    }

    public Component getTooltipText() {
        if (rainbow) return Component.literal("arcoíris");
        return Component.literal(Colors.getFormatting(value) + Colors.ARGBtoHex(a, r, g, b));
    }

    public void reset() {
        this.r = defaultValue.getRed();
        this.g = defaultValue.getGreen();
        this.b = defaultValue.getBlue();
        this.a = defaultValue.getAlpha();
        this.rainbow = false;
        this.rainbowSpeed = 2;
        this.saturation = 1f;
        this.brightness = 1f;
        super.reset();
    }

    public Map<String, ?> getConfigValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("r", r);
        map.put("g", g);
        map.put("b", b);
        map.put("a", a);
        map.put("rb", rainbow);
        map.put("rs", rainbowSpeed);
        map.put("sat", (double) saturation);
        map.put("bri", (double) brightness);
        return map;
    }
}
