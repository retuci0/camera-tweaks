package me.retucio.sputnik.module.setting.settings;

import me.retucio.sputnik.module.setting.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

// ajuste que te deja elegir entre distintas opciones definidas, dentro de un Enum
public class EnumSetting<E extends Enum<E>> extends Setting<E> {

    private final List<E> values;
    private int index;

    public EnumSetting(String name, String description, Class<E> enumClass, E defaultValue) {
        super(name, description, defaultValue);
        this.values = Arrays.asList(enumClass.getEnumConstants());
        this.index = values.indexOf(defaultValue);
    }

    public void setValue(E value) {
        if (this.value != value) {
            this.index = values.indexOf(value);
        }

        super.setValue(value);
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
}
