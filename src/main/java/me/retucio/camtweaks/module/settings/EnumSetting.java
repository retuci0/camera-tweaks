package me.retucio.camtweaks.module.settings;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.UpdateSettingEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

// ajuste que te deja elegir entre distintas opciones definidas, dentro de un Enum
public class EnumSetting<E extends Enum<E>> extends Setting {

    private final Class<E> enumClass;
    private final List<E> values;
    private E defaultValue;

    private E value;
    private int index;

    private Consumer<E> updateListener;

    public EnumSetting(String name, String description, Class<E> enumClass, E defaultValue) {
        super(name, description);
        this.enumClass = enumClass;
        this.values = Arrays.asList(enumClass.getEnumConstants());
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.index = values.indexOf(defaultValue);
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        if (this.value != value) {
            this.value = value;
            this.index = values.indexOf(value);
            CameraTweaks.EVENT_BUS.post(new UpdateSettingEvent(this));
            if (updateListener != null) updateListener.accept(value);
        }
    }

    public List<E> getValues() {
        return values;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        if (this.index != index)
            setValue(values.get(index));
    }

    public boolean is(E value) {
        return this.value == value;
    }

    // ciclar entre los valores del enum
    public void cycle() {
        if (index < values.size() - 1)
            setIndex(index + 1);
        else
            setIndex(0);
    }

    public void cycleBackwards() {
        if (index > 0)
            setIndex(index - 1);
        else
            setIndex(values.size() - 1);
    }

    public void setDefaultValue(E value) {
        defaultValue = value;
    }

    public E getDefaultValue() {
        return defaultValue;
    }

    public void reset() {
        setValue(defaultValue);
    }

    public void onUpdate(Consumer<E> listener) {
        this.updateListener = listener;
    }
}
