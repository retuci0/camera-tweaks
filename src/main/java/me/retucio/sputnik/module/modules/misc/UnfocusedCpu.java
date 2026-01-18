package me.retucio.sputnik.module.modules.misc;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;

public class UnfocusedCpu extends Module {

    public UnfocusedCpu() {
        super("aliviar cpu", "evita renderizar objetos en pantalla si la ventana no está seleccionada", Category.MISC);
    }
}
