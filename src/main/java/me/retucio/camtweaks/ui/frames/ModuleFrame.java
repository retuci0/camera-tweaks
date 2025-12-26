package me.retucio.camtweaks.ui.frames;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.events.camtweaks.ModuleFrameEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.buttons.ModuleButton;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

// marco para los módulos
public class ModuleFrame {

    public int x, y, w, h;
    public int renderY;
    public int dragX, dragY;
    public int totalHeight = 0;
    public boolean dragging, extended;
    public final String title = "módulos";

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

    public void updateRenderY(int scrollOffset) {
        renderY = y - scrollOffset;
    }

    void updateWidth() {
        // asegurarse de que todos los botones caben en el marco, haciendo que la anchura se ajuste al texto más largo
        if (mc.textRenderer == null) return;

        int maxWidth = mc.textRenderer.getWidth(title);
        for (ModuleButton button : moduleButtons) {
            String text = button.getModule().getName();
            int textWidth = mc.textRenderer.getWidth(text);
            maxWidth = Math.max(maxWidth, textWidth);
        }
        this.w = maxWidth + 22;
    }

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

        List<ModuleButton> visibleButtons = moduleButtons.stream()
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
                moduleButton.offset = buttonY - renderY;
                moduleButton.render(ctx, mouseX, mouseY, delta);
                buttonY += h;
            }
        } else {
            totalHeight = 0;
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        // registrar clics
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) {  // clic izquierdo para arrastrarlo
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            } else if (button == 1) {  // clic derecho para extenderlo
                extended = !extended;
                CameraTweaks.EVENT_BUS.post(new ModuleFrameEvent.Extend());
            }
        }

        if (!extended) return;  // solo dejar clicar en los módulos si el marco está extendido

        List<ModuleButton> visibleModuleButtons = moduleButtons.stream()
                .filter(mb -> mb.getModule().isSearchMatch())
                .toList();

        for (ModuleButton moduleButton : visibleModuleButtons)
            moduleButton.mouseClicked(mouseX, mouseY, button);
    }

    // detectar cuándo se suelta el clic
    public void mouseReleased(double mouseX, double mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
        if (button == 0 && dragging)
            dragging = false;

        List<ModuleButton> visibleModuleButtons = moduleButtons.stream()
                .filter(mb -> mb.getModule().isSearchMatch())
                .toList();

        for (ModuleButton moduleButton : visibleModuleButtons) {
            if (moduleButton.isHovered((int) mouseX, (int) mouseY))
                moduleButton.mouseReleased(mouseX, mouseY, button);
        }

        if (isHovered(mouseX, mouseY))
            CameraTweaks.EVENT_BUS.post(new ModuleFrameEvent.Move());
    }

    // verificar si el puntero del ratón se encuentra encima
    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + w && mouseY > renderY && mouseY < renderY + h;
    }

    // actualizar la posición al arrastrar el marco
    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }

    public List<ModuleButton> getButtons() {
        return moduleButtons;
    }
}