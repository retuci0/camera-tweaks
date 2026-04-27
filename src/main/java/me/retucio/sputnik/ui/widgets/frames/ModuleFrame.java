package me.retucio.sputnik.ui.widgets.frames;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.sputnik.ModuleFrameEvent;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.widgets.buttons.ModuleButton;
import me.retucio.sputnik.ui.widgets.Frame;
import me.retucio.sputnik.util.Colors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;


// marco para los módulos
public class ModuleFrame extends Frame<ModuleButton> {

    public boolean extended;

    public Minecraft mc = Minecraft.getInstance();

    public ModuleFrame(int x, int y, int w, int h) {
        super("módulos", x, y, w, h);
        dragging = false;
        extended = true;

        // determinar el saliente para cada botón de módulo
        int offset = h;
        for (Module module : ModuleManager.INSTANCE.getModules()) {
            buttons.add(new ModuleButton(module, this, offset));
            offset += h;
        }
    }

    @Override
    protected void updateWidth() {
        // asegurarse de que todos los botones caben en el marco, haciendo que la anchura se ajuste al texto más largo

        int maxWidth = mc.font.width(title);
        for (ModuleButton button : buttons) {
            String text = button.getModule().getName();
            int textWidth = mc.font.width(text);
            maxWidth = Math.max(maxWidth, textWidth);
        }
        this.w = maxWidth + 22;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        updateWidth();
        gui.fill(x, renderY, x + w, renderY + h, Colors.mainColor.getRGB()); // cabeza del marco

        // título del marco
        gui.text(mc.font, Component.literal(ChatFormatting.BOLD + title),
                x + 8,
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                -1, true);

        gui.text(mc.font, extended ? "-" : "+",
                x + w - mc.font.width("+") - 8,  // '+' y '-' tienen la misma anchura, o sea que no importa cuál use
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                -1, true);

        List<ModuleButton> visibleButtons = buttons.stream()
                .filter(mb -> mb.getModule().isSearchMatch())
                .toList();

        // dibujar sus módulos solo si está extendido
        if (extended) {
            totalHeight = visibleButtons.size() * h + 3;
            gui.fill(  // fondo para los botones
                    x, renderY + h + 1,
                    x + w, renderY + h + totalHeight,
                    Colors.frameBGColor.getRGB());

            // dibujar los botones para cada módulo
            int buttonY =  renderY + h + 1;
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
        // registrar clics
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) {  // clic izquierdo para arrastrarlo
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            } else if (button == 1) {  // clic derecho para extenderlo
                extended = !extended;
                Sputnik.EVENT_BUS.post(new ModuleFrameEvent.Extend());
            }
        }

        if (!extended) return;  // solo dejar clicar en los módulos si el marco está extendido

        List<ModuleButton> visibleModuleButtons = buttons.stream()
                .filter(mb -> mb.getModule().isSearchMatch())
                .toList();

        for (ModuleButton moduleButton : visibleModuleButtons)
            moduleButton.mouseClicked(mouseX, mouseY, button);
    }

    // detectar cuándo se suelta el clic
    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
        if (button == 0 && dragging)
            dragging = false;

        List<ModuleButton> visibleModuleButtons = buttons.stream()
                .filter(mb -> mb.getModule().isSearchMatch())
                .toList();

        for (ModuleButton moduleButton : visibleModuleButtons) {
            if (moduleButton.isHovered(mouseX, mouseY))
                moduleButton.mouseReleased(mouseX, mouseY, button);
        }

        if (isHovered(mouseX, mouseY))
            Sputnik.EVENT_BUS.post(new ModuleFrameEvent.Move());
    }

    // actualizar la posición al arrastrar el marco
    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }
}