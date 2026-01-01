package me.retucio.camtweaks.ui.widgets.buttons;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.widgets.frames.ModuleFrame;
import me.retucio.camtweaks.ui.widgets.Button;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

// clase para el botón para cada módulo
public class ModuleButton extends Button {

    private final Module module;
    public final int height = 18;

    public ModuleButton(Module module, ModuleFrame parent, int offset) {
        super(parent, offset);
        this.module = module;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill( // dibujar el contorno del botón
                parent.getX() + 2, parent.getRenderY() + offset + 3,
                parent.getX() + parent.getW() - 2 , parent.getRenderY() + height + offset,
                determineColor(mouseX, mouseY));

        ctx.drawText( // dibujar el nombre del módulo
                mc.textRenderer, module.getName(),
                parent.getX() + 5, parent.getRenderY() + offset + (height / 2) - (mc.textRenderer.fontHeight / 2) + 2,
                -1, true);

        // dibujar "tooltips" (cajas de texto) al pasar el puntero encima del botón, para mostrar su descripción
        if (isHovered(mouseX, mouseY))
            drawTooltip(ctx, mouseX, mouseY);
    }

    @Override
    public void drawTooltip(DrawContext ctx, int mouseX, int mouseY) {
        ctx.drawTooltip(Text.of(module.getDescription()), mouseX, mouseY + 20);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered((int) mouseX, (int) mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) {  // clic izquierdo para activar / desactivar el módulo
                module.toggle();
            } else if (button == 1) {  // clic derecho para el marco de ajustes (también lo cierra si está abierto)
                if (ClickGUI.INSTANCE.isSettingsFrameOpen(module))
                    ClickGUI.INSTANCE.closeSettingsFrame(module);
                else
                    ClickGUI.INSTANCE.openSettingsFrame(module,
                            parent.getX() + parent.getW() + 120, parent.getRenderY() + offset);
            }
        }
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY) {}

    @Override
    public void onKey(int key, int action) {}

    // verifica si el puntero del ratón se encuentra sobre el botón del módulo
    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return ClickGUI.INSTANCE.canSelect(this)
                && mouseX > parent.getX()
                && mouseX < parent.getX() + parent.getW()
                && mouseY > parent.getRenderY() + offset
                && mouseY < parent.getRenderY() + height + offset;
    }

    public int determineColor(double mouseX, double mouseY) {
        // determina el color del botón, dependiendo de si está el puntero encima y si está activado
        if (module.isEnabled())
            return isHovered((int) mouseX, (int) mouseY)
                    ? Colors.mainColor.brighter().getRGB()
                    : Colors.mainColor.getRGB();
        return isHovered((int) mouseX, (int) mouseY)
                ? Colors.buttonColor.brighter().getRGB()
                : Colors.buttonColor.getRGB();
    }

    public Module getModule() {
        return module;
    }
}
