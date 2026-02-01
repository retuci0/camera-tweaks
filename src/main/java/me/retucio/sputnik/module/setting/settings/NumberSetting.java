package me.retucio.sputnik.module.setting.settings;

import me.retucio.sputnik.module.setting.Setting;

import java.util.function.Consumer;

// ajuste numérico, es decir, que eliges un valor númerico entre el mínimo y el máximo disponibles
public class NumberSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double increment;  // el "increment" es el salto que hay entre valores disponibles

    private boolean locked;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double increment) {
        super(name, description, defaultValue);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    // no permitir valores ajenos a los límites definidos
    public static double clamp(double value, double min, double max) {
        return Math.clamp(value, min, max);
    }

    // como decía, el valor crece o decrece por el "increment" definido
    public void increment(boolean positive) {
        if (locked) return;
        if (positive) value += increment;
        else value -= increment;
        value = clamp(value, min, max);
        if (updateListener != null) updateListener.accept(value);
    }


    public float getFloatValue() {
        return value.floatValue();  // en "float" por conveniencia
    }

    public int getIntValue() {
        return value.intValue();  // en "int" (número entero) por conveniencia
    }

    public long getLongValue() {
        return value.longValue();  // lo mismo para "longs"
    }

    public long getShortValue() {
        return value.shortValue();
    }

    public long getByteValue() {
        return value.byteValue();
    }

    public void setValue(double value) {
        if (this.value == value || this.locked) return;
        double clamped = clamp(value, min, max);
        value = Math.round(clamped / increment) * increment;
        super.setValue(value);
    }

    public boolean isValid(double value) {
        return value >= min && value <= max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
