package me.retucio.sputnik.ui.widgets.buttons;

import me.retucio.sputnik.friend.Friend;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.panels.FriendsPanel;
import me.retucio.sputnik.ui.widgets.panels.ModulePanel;
import me.retucio.sputnik.ui.widgets.Button;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;


// clase para el botón para cada módulo
public class FriendButton extends Button {

    private final Friend friend;
    public final int height = 18;

    public FriendButton(Friend friend, FriendsPanel parent, int offset) {
        super(parent, offset);
        this.friend = friend;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        gui.fill( // dibujar el contorno del botón
                parent.getX() + 2, parent.getRenderY() + offset + 3,
                parent.getX() + parent.getW() - 2 , parent.getRenderY() + height + offset,
                determineColor(mouseX, mouseY));

        gui.text( // dibujar el nombre del amigo
                mc.font, friend.getName(),
                parent.getX() + 5, parent.getRenderY() + offset + (height / 2) - (mc.font.lineHeight / 2) + 2,
                -1, true);

        // dibujar "tooltips" (cajas de texto) al pasar el puntero encima del botón, para mostrar su UUID
        if (isHovered(mouseX, mouseY)) {
            drawTooltip(gui, mouseX, mouseY);
        }
    }

    @Override
    public void drawTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        gui.setTooltipForNextFrame(Component.literal(friend.getUuid().toString()), mouseX, mouseY + 20);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this)) {
            ClickGui.INSTANCE.openFriendSettingsPanel(friend, mouseX, mouseY);
        }
    }

    // verifica si el puntero del ratón se encuentra sobre el botón
    @Override
    public boolean isHovered(int mouseX, int mouseY) {
        return ClickGui.INSTANCE.canSelect(this)
                && mouseX > parent.getX()
                && mouseX < parent.getX() + parent.getW()
                && mouseY > parent.getRenderY() + offset
                && mouseY < parent.getRenderY() + height + offset;
    }

    public int determineColor(double mouseX, double mouseY) {
        // determina el color del botón, dependiendo de si está el puntero encima
        return isHovered((int) mouseX, (int) mouseY)
                ? Colors.buttonColor.brighter().getRGB()
                : Colors.buttonColor.getRGB();
    }

    public Friend getFriend() {
        return friend;
    }
}
