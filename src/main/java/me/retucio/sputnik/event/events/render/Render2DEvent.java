package me.retucio.sputnik.event.events.render;

import me.retucio.sputnik.event.Event;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class Render2DEvent extends Event {

    private final DrawContext ctx;
    private final RenderTickCounter tc;

    public Render2DEvent(DrawContext ctx, RenderTickCounter tc) {
        this.ctx = ctx;
        this.tc = tc;
    }

    public DrawContext getCtx() {
        return ctx;
    }

    public RenderTickCounter getTc() {
        return tc;
    }
}
