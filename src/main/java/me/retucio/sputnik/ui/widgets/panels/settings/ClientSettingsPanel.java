package me.retucio.sputnik.ui.widgets.panels.settings;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.sputnik.ClientSettingsPanelEvent;
import me.retucio.sputnik.module.modules.client.ClientSettingsModule;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.panels.SettingsPanel;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ClientSettingsPanel extends SettingsPanel {

    public static final ClientSettingsModule clientSettings = new ClientSettingsModule();
    private boolean extended = false;

    public ClientSettingsPanel(int x, int y, int w, int h) {
        super(clientSettings, x, y, w, h);
        this.title = "ajustes del mod";
        this.drawHeader = false;   // disable the original header
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        updateWidth();

        // Draw our own custom header (no close button, with +/-
        gui.fill(x, renderY, x + w, renderY + h, Colors.mainColor.getRGB());
        gui.text(mc.font, title,
                x + 8,
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                -1, true);

        String symbol = extended ? "-" : "+";
        int textWidth = mc.font.width(symbol);
        gui.text(mc.font, symbol,
                x + w - textWidth - 8,
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                -1, true);

        // Only draw the rest of the panel (content) if extended
        if (extended) {
            // Call super.render() – but since drawHeader=false, it will only draw the content
            super.render(gui, mouseX, mouseY, delta);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this)) {
            if (button == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            } else if (button == 1) {
                extended = !extended;
                Sputnik.EVENT_BUS.post(new ClientSettingsPanelEvent.Extend());
                return;
            }
        }

        if (extended) {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        ClickGui.INSTANCE.unselect(this);
        if (button == 0 && dragging) {
            dragging = false;
            Sputnik.EVENT_BUS.post(new ClientSettingsPanelEvent.Move());
        }
        if (extended) {
            super.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY) {
        if (extended) {
            super.mouseDragged(mouseX, mouseY);
        }
    }

    @Override
    public void mouseScrolled(double amount) {
        if (extended) {
            super.mouseScrolled(amount);
        }
    }

    @Override
    public void onKey(int key, int action) {
        if (extended) {
            super.onKey(key, action);
        }
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }
}