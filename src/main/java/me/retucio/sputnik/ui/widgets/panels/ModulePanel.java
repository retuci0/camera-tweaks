package me.retucio.sputnik.ui.widgets.panels;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.sputnik.ModulePanelEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.buttons.ModuleButton;
import me.retucio.sputnik.util.Colors;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;


// panel para los módulos
public class ModulePanel extends ExtendablePanel<ModuleButton> {

    public final Category category;

    public ModulePanel(Category category, int x, int y, int w, int h) {
        super(category.toString(), x, y, w, h);
        this.category = category;
        dragging = false;

        int offset = h;
        for (Module module : ModuleManager.INSTANCE.getModules()) {
            if (!module.getCategory().equals(this.category)) continue;
            buttons.add(new ModuleButton(module, this, offset));
            offset += h;
        }
    }

    @Override
    protected void updateWidth() {
        int maxWidth = mc.font.width(title);
        for (ModuleButton button : buttons) {
            int textWidth = mc.font.width(button.getModule().getName());
            maxWidth = Math.max(maxWidth, textWidth);
        }
        this.w = maxWidth + 22;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        updateWidth();
        gui.fill(x, renderY, x + w, renderY + h, Colors.mainColor.getRGB());

        gui.text(mc.font, Component.literal(title),
                x + 8,
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                -1, true);

        drawExpandCollapse(gui, mouseX, mouseY);

        List<ModuleButton> visibleButtons = buttons.stream()
                .filter(mb -> mb.getModule().isSearchMatch())
                .toList();

        if (extended) {
            totalHeight = visibleButtons.size() * h + 3;
            gui.fill(x, renderY + h + 1, x + w, renderY + h + totalHeight, Colors.panelBgColor.getRGB());

            int buttonY = renderY + h + 1;
            for (ModuleButton moduleButton : visibleButtons) {
                moduleButton.setOffset(buttonY - renderY);
                moduleButton.render(gui, mouseX, mouseY, delta);
                buttonY += h;
            }
        } else {
            totalHeight = 0;
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
                toggleExtended();
                Sputnik.EVENT_BUS.post(new ModulePanelEvent.Extend(this));
            }
        }

        if (!extended) return;

        List<ModuleButton> visibleModuleButtons = buttons.stream()
                .filter(mb -> mb.getModule().isSearchMatch())
                .toList();

        for (ModuleButton moduleButton : visibleModuleButtons) {
            moduleButton.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        ClickGui.INSTANCE.unselect(this);
        if (button == 0 && dragging) dragging = false;

        List<ModuleButton> visibleModuleButtons = buttons.stream()
                .filter(mb -> mb.getModule().isSearchMatch())
                .toList();

        for (ModuleButton moduleButton : visibleModuleButtons) {
            if (moduleButton.isHovered(mouseX, mouseY))
                moduleButton.mouseReleased(mouseX, mouseY, button);
        }

        if (isHovered(mouseX, mouseY))
            Sputnik.EVENT_BUS.post(new ModulePanelEvent.Move(this));
    }

    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }
}