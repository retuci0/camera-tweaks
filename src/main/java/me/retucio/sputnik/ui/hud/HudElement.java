package me.retucio.sputnik.ui.hud;

import me.retucio.sputnik.module.modules.client.Hud;
import me.retucio.sputnik.ui.widgets.Widget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;


public abstract class HudElement extends Widget {

    private final String id;
    protected boolean visible;
    protected final int defaultX, defaultY;

    public HudElement(String id, int defaultX, int defaultY) {
        super(defaultX, defaultY, 85, 14);
        this.id = id;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.visible = true;
    }

    public abstract void renderInGame(GuiGraphicsExtractor gui, float delta, Hud hud);
    public abstract void renderInEditor(GuiGraphicsExtractor gui, Hud hud);
    public abstract List<Component> getTooltip();

    public String getId() { return id; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public void resetPosition() {
        this.x = defaultX;
        this.y = defaultY;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}