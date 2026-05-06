package me.retucio.sputnik.ui.widgets.panels;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.sputnik.FriendEvent;
import me.retucio.sputnik.event.sputnik.FriendPanelEvent;
import me.retucio.sputnik.event.sputnik.ModulePanelEvent;
import me.retucio.sputnik.friend.Friend;
import me.retucio.sputnik.friend.FriendManager;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.Panel;
import me.retucio.sputnik.ui.widgets.buttons.FriendButton;
import me.retucio.sputnik.ui.widgets.buttons.ModuleButton;
import me.retucio.sputnik.util.Colors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;


public class FriendsPanel extends Panel<FriendButton> {

    public boolean extended;
    private int offset;

    public FriendsPanel(int x, int y, int w, int h) {
        super("amigos", x, y, w, h);
        dragging = false;
        extended = true;

        Sputnik.EVENT_BUS.subscribe(this);

        offset = h;
        for (Friend friend : FriendManager.INSTANCE.getFriends()) {
            buttons.add(new FriendButton(friend, this, offset));
            offset += h;
        }
    }

    @Override
    protected void updateWidth() {
        // asegurarse de que todos los botones caben en el panel, haciendo
        // que la anchura se ajuste al texto más largo
        int maxWidth = mc.font.width(title) + mc.font.width("  +");
        for (FriendButton button : buttons) {
            String text = button.getFriend().getName();
            int textWidth = mc.font.width(text);
            maxWidth = Math.max(maxWidth, textWidth);
        }
        this.w = maxWidth + 22;
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        updateWidth();
        gui.fill(x, renderY, x + w, renderY + h, Colors.mainColor.getRGB());  // cabeza del panel

        // título del panel
        gui.text(mc.font, Component.literal(title),
                x + 8,
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                -1, true);

        gui.text(mc.font, extended ? "-" : "+",
                x + w - mc.font.width("+") - 8,  // '+' y '-' tienen la misma anchura, o sea que no importa cuál use
                renderY + (h / 2) - (mc.font.lineHeight / 2),
                -1, true);

        List<FriendButton> visibleButtons = buttons.stream()
                .filter(fb -> fb.getFriend().isSearchMatch())
                .toList();

        // dibujar los botones solo si está extendido
        if (extended) {
            totalHeight = visibleButtons.size() * h + 3;
            gui.fill(  // fondo para los botones
                    x, renderY + h + 1,
                    x + w, renderY + h + totalHeight,
                    Colors.panelBgColor.getRGB());

            // dibujar los botones para cada módulo
            int buttonY =  renderY + h + 1;
            for (FriendButton moduleButton : visibleButtons) {
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
        if (isHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this)) {
            if (button == 0) {  // clic izquierdo para arrastrarlo
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            } else if (button == 1) {  // clic derecho para extenderlo
                extended = !extended;
                Sputnik.EVENT_BUS.post(new FriendPanelEvent.Extend());
            }
        }

        if (!extended) return;  // solo dejar clicar en los botones si el marco está extendido

        List<FriendButton> visibleFriendButtons = buttons.stream()
                .filter(fb -> fb.getFriend().isSearchMatch())
                .toList();

        for (FriendButton friendButton : visibleFriendButtons) {
            friendButton.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        ClickGui.INSTANCE.unselect(this);
        if (button == 0 && dragging) {
            dragging = false;
        }

        List<FriendButton> visibleFriendButtons = buttons.stream()
                .filter(fb -> fb.getFriend().isSearchMatch())
                .toList();

        for (FriendButton friendButton : visibleFriendButtons) {
            if (friendButton.isHovered(mouseX, mouseY)) {
                friendButton.mouseReleased(mouseX, mouseY, button);
            }
        }

        if (isHovered(mouseX, mouseY)) {
            Sputnik.EVENT_BUS.post(new FriendPanelEvent.Move());
        }
    }

    // actualizar la posición al arrastrar el panel
    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }

    @EventListener
    private void onAddFriend(FriendEvent.Add event) {
        buttons.add(new FriendButton(event.getFriend(), this, offset));
    }

    @EventListener
    private void onRemoveFriend(FriendEvent.Remove event) {
        buttons.remove(getButtonOfFriend(event.getFriend()));
    }

    public FriendButton getButtonOfFriend(Friend friend) {
        for (FriendButton button : buttons) {
            if (button.getFriend().getUuid().equals(friend.getUuid())) {
                return button;
            }
        }
        return null;
    }
}
