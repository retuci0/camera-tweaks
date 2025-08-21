package me.retucio.camtweaks.module.settings;

import java.util.function.Consumer;

// ajuste numérico, es decir, que eliges un valor númerico entre el mínimo y el máximo disponibles
public class NumberSetting extends Setting {

    private double value;
    private final double defaultValue;

    private final double min;
    private final double max;
    private final double increment;  // el "increment" es el salto que hay entre valores disponibles

    private Consumer<Double> updateListener;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double increment) {
        super(name, description);
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
        if (positive) value += increment;
        else value -= increment;
        value = clamp(value, min, max);
        if (updateListener != null) updateListener.accept(value);
    }


    public double getValue() {
        return value;
    }

    public float getFloatValue() {
        return (float) value;  // en "float" por conveniencia
    }

    public int getIntValue() {
        return (int) value;  // en "int" (número entero) por conveniencia
    }

    public void setValue(double value) {
        if (this.value == value) return;
        double clamped = clamp(value, min, max);
        this.value = Math.round(clamped / increment) * increment;
        if (updateListener != null) updateListener.accept(this.value);
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public void reset() {
        setValue(defaultValue);
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

    public void onUpdate(Consumer<Double> listener) {
        this.updateListener = listener;
    }
}
