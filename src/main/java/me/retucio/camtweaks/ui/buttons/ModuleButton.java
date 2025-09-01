package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.ui.ClickGUI;
import me.retucio.camtweaks.ui.frames.ModuleFrame;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

// clase para el botón para cada módulo
public class ModuleButton {

    public Module module;
    public ModuleFrame parent;
    public int offset;
    public final int height = 18;

    public ModuleButton(Module module, ModuleFrame parent, int offset) {
        this.module = module;
        this.parent = parent;
        this.offset = offset;
    }

    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        ctx.fill( // dibujar el contorno del botón
                parent.x + 2, parent.y + offset + 3,
                parent.x + parent.w - 2 , parent.y + height + offset,
                determineColor(mouseX, mouseY));

        ctx.drawText( // dibujar el nombre del módulo
                parent.mc.textRenderer, module.getName(),
                parent.x + 5, parent.y + offset + (height / 2) - (parent.mc.textRenderer.fontHeight / 2) + 2,
                -1, true);

        // dibujar "tooltips" (cajas de texto) al pasar el puntero encima del botón, para mostrar su descripción
        if (isHovered((int) mouseX, (int) mouseY)) {
            Screen currentScreen = parent.mc.currentScreen;
            if (currentScreen != null)
                ctx.drawTooltip(Text.of(module.getDescription()), (int) mouseX, (int) mouseY + 20);
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {// clic izquierdo para activar / desactivar el módulo
            module.toggle();
        } else if (button == 1) {  // clic derecho para el marco de ajustes (también lo cierra si está abierto)
            if (ClickGUI.INSTANCE.isSettingsFrameOpen(module))
                ClickGUI.INSTANCE.closeSettingsFrame(module);
            else
                ClickGUI.INSTANCE.openSettingsFrame(module,
                        parent.x + parent.w + 120, parent.y + offset);
        }
    }

    // verifica si el puntero del ratón se encuentra sobre el botón del módulo
    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX > parent.x && mouseX < parent.x + parent.w && mouseY > parent.y + offset && mouseY < parent.y + height + offset;
    }

    public int determineColor(double mouseX, double mouseY) {
        // determina el color del botón, dependiendo de si está el puntero encima y si está activado
        if (module.isEnabled())
            return isHovered((int) mouseX, (int) mouseY) ? Colors.enabledHoveredModuleButtonColor : Colors.enabledModuleButtonColor;
        return isHovered((int) mouseX, (int) mouseY) ? Colors.hoveredModuleButtonColor : Colors.moduleButtonColor;
    }
}
