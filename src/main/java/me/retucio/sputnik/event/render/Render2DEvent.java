package me.retucio.sputnik.event.render;

import com.github.retucio.neutrino.Event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;


public class Render2DEvent extends Event {

    private final GuiGraphicsExtractor gui;
    private final DeltaTracker dt;

    public Render2DEvent(GuiGraphicsExtractor gui, DeltaTracker dt) {
        this.gui = gui;
        this.dt = dt;
    }

    public GuiGraphicsExtractor getGui() {
        return gui;
    }

    public DeltaTracker getDt() {
        return dt;
    }
}
