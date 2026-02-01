package me.retucio.sputnik.event.events.render;

import me.retucio.sputnik.event.Event;

public class RenderBlockOutlineEvent extends Event {

    private int color;
    private float lineWidth;

    public RenderBlockOutlineEvent(int color, float lineWidth) {
        this.color = color;
        this.lineWidth = lineWidth;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }
}
