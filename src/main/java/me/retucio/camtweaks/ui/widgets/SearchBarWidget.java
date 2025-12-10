package me.retucio.camtweaks.ui.widgets;

import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class SearchBarWidget {

    private boolean dragging;
    private int dragX, dragY;

    private int x, y;
    private final int w, h;
    private int renderY;

    private boolean focused;
    private final StringBuilder buffer = new StringBuilder();

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public SearchBarWidget(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        if (!ClientSettingsFrame.guiSettings.searchBar.isEnabled()) return;

        Color textFieldColor = isTextFieldHovered(mouseX, mouseY)
                ? Colors.buttonColor.brighter()
                : Colors.buttonColor;

        if (focused) textFieldColor = Colors.buttonColor.darker();

        // bordes de la barra, para no obstruir la transparencia del campo de texto
        ctx.fill(x, renderY, x + 20, renderY + h, Colors.mainColor.getRGB());
        ctx.fill(x, renderY, x + w, renderY + 2, Colors.mainColor.getRGB());
        ctx.fill(x, renderY + h, x + w, renderY + h - 2, Colors.mainColor.getRGB());
        ctx.fill(x + w - 20, renderY, x + w, renderY + h, Colors.mainColor.getRGB());

        ctx.fill(x + 20, renderY + 2, x + w - 20, renderY + h - 2, textFieldColor.getRGB());

        // dibujar líneas en la parte agarrable, para indicárselo al usuario
        for (int i = 0; i < 4; i++)
            ctx.drawHorizontalLine(x + 4, x + 16, renderY + 3 * i + 5, Color.LIGHT_GRAY.getRGB());

        // texto
        Text label = Text.literal(focused ? buffer + "_" : (buffer.isEmpty() ? Formatting.ITALIC + "\uD83D\uDD0D buscar..." : buffer.toString()));
        ctx.drawText(mc.textRenderer, label,
                x + 24, renderY + h / 2 - mc.textRenderer.fontHeight / 2,
                (buffer.isEmpty() && !focused) ? Color.LIGHT_GRAY.getRGB() : -1, true);

        // botón para borrar búsqueda actual
        ctx.drawText(mc.textRenderer, "×", x + w - mc.textRenderer.getWidth("×") - 6, renderY + (h / 2) - mc.textRenderer.fontHeight / 2,
                isClearButtonHovered(mouseX, mouseY) ? Color.RED.getRGB() : -1, true);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !ClientSettingsFrame.guiSettings.searchBar.isEnabled()) return;
        if (isHandleHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            dragging = true;
            dragX = (int) mouseX - x;
            dragY = (int) mouseY - y;
        } else if (isClearButtonHovered(mouseX, mouseY)) {
            buffer.setLength(0);
        }
        focused = isTextFieldHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (!ClientSettingsFrame.guiSettings.searchBar.isEnabled()) return;
        ClickGUI.INSTANCE.unselect(this);
        if (button == 0 && dragging) dragging = false;
    }

    public void onKey(int key, int action) {
        if (!ClientSettingsFrame.guiSettings.searchBar.isEnabled()) return;
        if (!focused || action == GLFW.GLFW_RELEASE) return;

        switch (key) {
            case GLFW.GLFW_KEY_ENTER -> focused = false;
            case GLFW.GLFW_KEY_ESCAPE -> {
                buffer.setLength(0);
                focused = false;
            }
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
        if (!buffer.isEmpty()) buffer.deleteCharAt(buffer.length() - 1);
    }

    public void charTyped(char c) {
        if (!focused) return;
        buffer.append(c);
        if (mc.textRenderer.getWidth(buffer.toString()) > w - 30) onBackspace();
    }

    public String getSearchInput() {
        return buffer.toString();
    }

    public boolean isHandleHovered(double mouseX, double mouseY) {
        return ClickGUI.INSTANCE.canSelect(this)
                && mouseX > x && mouseX < x + 18
                && mouseY > renderY && mouseY < renderY + h;
    }

    public boolean isTextFieldHovered(double mouseX, double mouseY) {
        return ClickGUI.INSTANCE.canSelect(this)
                && mouseX > x + 20 && mouseX < x + w - 20
                && mouseY > renderY + 2 && mouseY < renderY + h - 2;
    }

    public boolean isClearButtonHovered(double mouseX, double mouseY) {
        return ClickGUI.INSTANCE.canSelect(this)
                && mouseX > x + w - 15 && mouseX < x + w - 3
                && mouseY > renderY + 3 && mouseY < renderY + h - 5;
    }

    public boolean isFocused() {
        return focused && ClientSettingsFrame.guiSettings.searchBar.isEnabled();
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
}
