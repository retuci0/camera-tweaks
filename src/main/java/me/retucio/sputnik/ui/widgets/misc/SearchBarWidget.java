package me.retucio.sputnik.ui.widgets.misc;

import me.retucio.sputnik.config.ConfigManager;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.OptionSetting;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.buttons.settings.ChooseButton;
import me.retucio.sputnik.ui.widgets.panels.SettingsPanel;
import me.retucio.sputnik.ui.widgets.panels.settings.ClientSettingsPanel;
import me.retucio.sputnik.ui.widgets.Widget;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.KeyUtil;
import me.retucio.sputnik.util.MiscUtil;
import me.retucio.sputnik.util.render.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.List;

public class SearchBarWidget extends Widget {

    private boolean dragging;
    private int dragX, dragY;

    private int renderY;

    private boolean focused;
    private final StringBuilder buffer = new StringBuilder();

    public SearchBarWidget(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        if (!ClientSettingsPanel.clientSettings.searchBar.getValue()) return;

        Color textFieldColor = isTextFieldHovered(mouseX, mouseY)
                ? Colors.buttonColor.brighter()
                : Colors.buttonColor;

        if (focused) textFieldColor = Colors.buttonColor.darker();

        // bordes de la barra, para no obstruir la transparencia del campo de texto
        gui.fill(x, renderY, x + 20, renderY + h, Colors.mainColor.getRGB());
        gui.fill(x, renderY, x + w, renderY + 2, Colors.mainColor.getRGB());
        gui.fill(x, renderY + h, x + w, renderY + h - 2, Colors.mainColor.getRGB());
        gui.fill(x + w - 20, renderY, x + w, renderY + h, Colors.mainColor.getRGB());

        gui.fill(x + 20, renderY + 2, x + w - 20, renderY + h - 2, textFieldColor.getRGB());

        // dibujar líneas en la parte agarrable, para indicárselo al usuario
        for (int i = 0; i < 4; i++) {
            gui.horizontalLine(x + 4, x + 16, renderY + 3 * i + 5, Color.LIGHT_GRAY.getRGB());
        }

        // texto
        Component label = Component.literal(focused ? buffer + "_" : (buffer.isEmpty() ? ChatFormatting.ITALIC + "\uD83D\uDD0D buscar..." : buffer.toString()));
        gui.text(mc.font, label,
                x + 24, renderY + h / 2 - mc.font.lineHeight / 2,
                (buffer.isEmpty() && !focused) ? Color.LIGHT_GRAY.getRGB() : -1, true);

        // botón para borrar búsqueda actual
        gui.text(mc.font, "×", x + w - mc.font.width("×") - 6, renderY + (h / 2) - mc.font.lineHeight / 2,
                isClearButtonHovered(mouseX, mouseY) ? Color.RED.getRGB() : -1, true);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0 || !ClientSettingsPanel.clientSettings.searchBar.getValue()) return;
        if (isHandleHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this)) {
            dragging = true;
            dragX = mouseX - x;
            dragY = mouseY - y;
        } else if (isClearButtonHovered(mouseX, mouseY)) {
            buffer.setLength(0);
        }
        focused = isTextFieldHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (!ClientSettingsPanel.clientSettings.searchBar.getValue()) return;
        ClickGui.INSTANCE.unselect(this);
        if (button == 0 && dragging) {
            dragging = false;
            savePosition();
        }
    }

    @Override
    public void onKey(int key, int action) {
        if (!ClientSettingsPanel.clientSettings.searchBar.getValue()) return;
        if (!focused || action == GLFW.GLFW_RELEASE) return;

        if (key == GLFW.GLFW_KEY_V && KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            paste();
            return;
        }

        switch (key) {
            case GLFW.GLFW_KEY_ENTER -> focused = false;
            case GLFW.GLFW_KEY_ESCAPE -> {
                buffer.setLength(0);
                focused = false;
            }
            case GLFW.GLFW_KEY_SPACE -> charTyped(' ');
            case GLFW.GLFW_KEY_BACKSPACE -> onBackspace();
            default -> {
                String c = KeyUtil.getKeyName(key);
                if (c.length() == 1) {
                    if (KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || KeyUtil.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT))
                        charTyped(KeyUtil.shiftKey(c).charAt(0));
                    else
                        charTyped(c.toLowerCase().charAt(0));
                }
            }
        }
    }

    public void onBackspace() {
        MiscUtil.backspace(buffer);
    }

    public void paste() {
        buffer.append(MiscUtil.getPasteContent(-1));
    }

    public void charTyped(char c) {
        if (!focused) return;
        buffer.append(c);
        if (mc.font.width(buffer.toString()) > w - 30) onBackspace();
    }

    public String getSearchInput() {
        return buffer.toString();
    }

    public boolean isHandleHovered(int mouseX, int mouseY) {
        return ClickGui.INSTANCE.canSelect(this)
                && mouseX > x && mouseX < x + 18
                && mouseY > renderY && mouseY < renderY + h;
    }

    public boolean isTextFieldHovered(int mouseX, int mouseY) {
        return ClickGui.INSTANCE.canSelect(this)
                && mouseX > x + 20 && mouseX < x + w - 20
                && mouseY > renderY + 2 && mouseY < renderY + h - 2;
    }

    public boolean isClearButtonHovered(int mouseX, int mouseY) {
        return ClickGui.INSTANCE.canSelect(this)
                && mouseX > x + w - 15 && mouseX < x + w - 3
                && mouseY > renderY + 3 && mouseY < renderY + h - 5;
    }

    public boolean isFocused() {
        return focused && ClientSettingsPanel.clientSettings.searchBar.getValue();
    }

    public void updateRenderY(int scrollOffset) {
        renderY = y - scrollOffset;
    }

    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }

    public void savePosition() {
        if (ConfigManager.INSTANCE.getConfig() != null) {
            ConfigManager.INSTANCE.setSearchBarPosition(x, y);
        }
    }
}
