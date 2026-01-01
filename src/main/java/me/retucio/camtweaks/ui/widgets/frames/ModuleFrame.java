package me.retucio.camtweaks.ui.widgets.frames;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.ModuleFrameEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.widgets.buttons.ModuleButton;
import me.retucio.camtweaks.ui.widgets.Frame;
import me.retucio.camtweaks.util.Colors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;


// marco para los módulos
public class ModuleFrame extends Frame<ModuleButton> {

    public boolean extended;

    public MinecraftClient mc = MinecraftClient.getInstance();

    public ModuleFrame(int x, int y, int w, int h) {
        super("módulos", x, y, w, h);
        dragging = false;
        extended = true;

        // determinar el saliente para cada botón de módulo
        int offset = h;
        for (Module module : ModuleManager.INSTANCE.getModules()) {
            buttons.add(new ModuleButton(module, this, offset));
            updateWidth();
            offset += h;
        }
    }

    @Override
    protected void updateWidth() {
        // asegurarse de que todos los botones caben en el marco, haciendo que la anchura se ajuste al texto más largo
        if (mc.textRenderer == null) return;

        int maxWidth = mc.textRenderer.getWidth(title);
        for (ModuleButton button : buttons) {
            String text = button.getModule().getName();
            int textWidth = mc.textRenderer.getWidth(text);
            maxWidth = Math.max(maxWidth, textWidth);
        }
        this.w = maxWidth + 22;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateWidth();
        ctx.fill(x, renderY, x + w, renderY + h, Colors.mainColor.getRGB()); // cabeza del marco

        // título del marco
        ctx.drawText(mc.textRenderer, Text.literal(Formatting.BOLD + title),
                x + 8,
                renderY + (h / 2) - (mc.textRenderer.fontHeight / 2),
                -1, true);

        ctx.drawText(mc.textRenderer, extended ? "-" : "+",
                x + w - mc.textRenderer.getWidth("+") - 8,  // '+' y '-' tienen la misma anchura, o sea que no importa cuál use
                renderY + (h / 2) - (mc.textRenderer.fontHeight / 2),
                -1, true);

        List<ModuleButton> visibleButtons = buttons.stream()
                .filter(mb -> mb.getModule().isSearchMatch())
                .toList();

        // dibujar sus módulos solo si está extendido
        if (extended) {
            totalHeight = visibleButtons.size() * h + 3;
            ctx.fill(  // fondo para los botones
                    x, renderY + h + 1,
                    x + w, renderY + h + totalHeight,
                    Colors.frameBGColor.getRGB());

            // dibujar los botones para cada módulo
            int buttonY =  renderY + h + 1;
            for (ModuleButton moduleButton : visibleButtons) {
                moduleButton.setOffset(buttonY - renderY);
                moduleButton.render(ctx, mouseX, mouseY, delta);
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
                CameraTweaks.EVENT_BUS.post(new ModuleFrameEvent.Extend());
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
            CameraTweaks.EVENT_BUS.post(new ModuleFrameEvent.Move());
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY) {}

    @Override
    public void onKey(int key, int action) {}

    // actualizar la posición al arrastrar el marco
    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }
}