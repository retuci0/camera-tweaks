package me.retucio.sputnik.ui.widgets.buttons;

import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.widgets.frames.ModuleFrame;
import me.retucio.sputnik.ui.widgets.Button;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;


// clase para el botón para cada módulo
public class ModuleButton extends Button {

    private final Module module;
    public final int height = 18;

    public ModuleButton(Module module, ModuleFrame parent, int offset) {
        super(parent, offset);
        this.module = module;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        gui.fill( // dibujar el contorno del botón
                parent.getX() + 2, parent.getRenderY() + offset + 3,
                parent.getX() + parent.getW() - 2 , parent.getRenderY() + height + offset,
                determineColor(mouseX, mouseY));

        gui.text( // dibujar el nombre del módulo
                mc.font, module.getName(),
                parent.getX() + 5, parent.getRenderY() + offset + (height / 2) - (mc.font.lineHeight / 2) + 2,
                -1, true);

        // dibujar "tooltips" (cajas de texto) al pasar el puntero encima del botón, para mostrar su descripción
        if (isHovered(mouseX, mouseY)) {
            drawTooltip(gui, mouseX, mouseY);
        }
    }

    @Override
    public void drawTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        gui.setTooltipForNextFrame(Component.literal(module.getDescription()), mouseX, mouseY + 20);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            if (button == 0) {  // clic izquierdo para activar / desactivar el módulo
                module.toggle();
            } else if (button == 1) {  // clic derecho para el marco de ajustes (también lo cierra si está abierto)
                if (ClickGUI.INSTANCE.isSettingsFrameOpen(module)) {
                    ClickGUI.INSTANCE.closeSettingsFrame(module);
                } else {
                    ClickGUI.INSTANCE.openSettingsFrame(
                            module,
                            parent.getX() + parent.getW() + 120,
                            parent.getY() + offset
                    );
                }
            }
        }
    }

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
