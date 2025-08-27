package me.retucio.camtweaks.ui.frames;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.ModuleFrameEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.ui.buttons.ModuleButton;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

// marco para los módulos
public class ModuleFrame {

    public int x, y, w, h, dragX, dragY;
    public boolean dragging, extended;
    public String title = "módulos -";

    private final List<ModuleButton> moduleButtons = new ArrayList<>();

    public MinecraftClient mc = MinecraftClient.getInstance();

    public ModuleFrame(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        dragging = false;
        extended = true;

        // determinar el saliente para cada botón de módulo
        int offset = h;
        for (Module module : ModuleManager.INSTANCE.getModules()) {
            moduleButtons.add(new ModuleButton(module, this, offset));
            updateWidth();
            offset += h;
        }
    }

    void updateWidth() {
        // asegurarse de que todos los botones caben en el marco, haciendo que la anchura se ajuste al texto más largo
        if (mc.textRenderer == null) return;

        int maxWidth = mc.textRenderer.getWidth(title);
        for (ModuleButton button : moduleButtons) {
            String text = button.module.getName();
            int textWidth = mc.textRenderer.getWidth(text);
            maxWidth = Math.max(maxWidth, textWidth);
        }
        this.w = maxWidth + 22;
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateWidth();
        ctx.fill(x, y, x + w, y + h, Colors.frameHeadColor); // cabeza del marco
        // título del marco
        ctx.drawText(mc.textRenderer, title,
                x + (w / 2) - (mc.textRenderer.getWidth(title) / 2),
                y + ( h /2) - (mc.textRenderer.fontHeight / 2),
                -1, false);

        // dibujar sus módulos solo si está extendido
        if (extended) {
            int totalHeight = moduleButtons.size() * h + 3;
            ctx.fill( // fondo para los botones
                    x, y + h + 1,
                    x + w, y + h + totalHeight,
                    Colors.frameBGColor);

            // dibujar los botones para cada módulo
            for (ModuleButton moduleButton : moduleButtons)
                moduleButton.render(ctx, mouseX, mouseY, delta);
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        // registrar clics
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {  // clic izquierdo para arrastrarlo
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            } else if (button == 1) {  // clic derecho para extenderlo
                extended = !extended;
                title = extended ? "módulos -" : "módulos +";
                CameraTweaks.EVENT_BUS.post(new ModuleFrameEvent.Extend());
            }
        }

        if (!extended) return;  // solo dejar clicar en los módulos si el marco está extendido
        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.isHovered((int) mouseX, (int) mouseY)) {
                moduleButton.mouseClicked(mouseX, mouseY, button);
                break;
            }
        }
    }

    // detectar cuándo se suelta el clic
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
        }
        CameraTweaks.EVENT_BUS.post(new ModuleFrameEvent.Move());
    }

    // verificar si el puntero del ratón se encuentra encima
    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h;
    }

    // actualizar la posición al arrastrar el marco
    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }
}