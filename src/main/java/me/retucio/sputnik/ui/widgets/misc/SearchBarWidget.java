package me.retucio.sputnik.ui.widgets.misc;

import me.retucio.sputnik.config.ConfigManager;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.OptionSetting;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.widgets.buttons.settings.ChooseButton;
import me.retucio.sputnik.ui.widgets.frames.SettingsFrame;
import me.retucio.sputnik.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.sputnik.ui.widgets.Widget;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.KeyUtil;
import me.retucio.sputnik.util.render.Textures;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.List;

public class SearchBarWidget extends Widget {

    private boolean dragging;
    private int dragX, dragY;

    private int renderY;

    private boolean focused;
    private final StringBuilder buffer = new StringBuilder();

    // simular un módulo
    private final Module dummy = new Module("", "", Category.ALL);
    private final OptionSetting<Category> categories = dummy.getSgGeneral().add(
            new OptionSetting<>(
                "filtrar",
                "filtrar módulos por categoría",
                List.of(Category.values()),
                Category.ALL
            )
    );
    private final ChooseButton<Category> dummyButton = new ChooseButton<>(
            categories, new SettingsFrame(dummy, 0, 0, 50, 50), 0
    );


    public SearchBarWidget(int x, int y, int w, int h) {
        super(x, y, w, h);

        categories.onUpdate(v -> {
            if (ClickGUI.INSTANCE != null) ClickGUI.INSTANCE.setCategoryFilter(v);
        });
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (!ClientSettingsFrame.guiSettings.searchBar.getValue()) return;

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

        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, Textures.SEARCH_BAR_FILTER,
                x + w - 35, renderY + h / 2 - 6,
                0, 0, 12, 12, 12, 12,
                isFilterButtonHovered(mouseX, mouseY)
                        ? Colors.SILVER.getRGB()
                        : Colors.WHITE.getRGB()
        );

        // dibujar líneas en la parte agarrable, para indicárselo al usuario
        for (int i = 0; i < 4; i++) {
            ctx.drawHorizontalLine(x + 4, x + 16, renderY + 3 * i + 5, Color.LIGHT_GRAY.getRGB());
        }

        // texto
        Text label = Text.literal(focused ? buffer + "_" : (buffer.isEmpty() ? Formatting.ITALIC + "\uD83D\uDD0D buscar..." : buffer.toString()));
        ctx.drawText(mc.textRenderer, label,
                x + 24, renderY + h / 2 - mc.textRenderer.fontHeight / 2,
                (buffer.isEmpty() && !focused) ? Color.LIGHT_GRAY.getRGB() : -1, true);

        // botón para borrar búsqueda actual
        ctx.drawText(mc.textRenderer, "×", x + w - mc.textRenderer.getWidth("×") - 6, renderY + (h / 2) - mc.textRenderer.fontHeight / 2,
                isClearButtonHovered(mouseX, mouseY) ? Color.RED.getRGB() : -1, true);

        if (isFilterButtonHovered(mouseX, mouseY)) {
            ctx.drawTooltip(mc.textRenderer, Text.of("filtrar por categoría"), mouseX + 7, mouseY + 20);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0 || !ClientSettingsFrame.guiSettings.searchBar.getValue()) return;
        if (isHandleHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            dragging = true;
            dragX = mouseX - x;
            dragY = mouseY - y;
        } else if (isClearButtonHovered(mouseX, mouseY)) {
            buffer.setLength(0);
        } else if (isFilterButtonHovered(mouseX, mouseY)) {
            openFilterFrame(mouseX, mouseY);
        }
        focused = isTextFieldHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (!ClientSettingsFrame.guiSettings.searchBar.getValue()) return;
        ClickGUI.INSTANCE.unselect(this);
        if (button == 0 && dragging) {
            dragging = false;
            savePosition();
        }
    }

    @Override
    public void onKey(int key, int action) {
        if (!ClientSettingsFrame.guiSettings.searchBar.getValue()) return;
        if (!focused || action == GLFW.GLFW_RELEASE) return;

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

    public boolean isHandleHovered(int mouseX, int mouseY) {
        return ClickGUI.INSTANCE.canSelect(this)
                && mouseX > x && mouseX < x + 18
                && mouseY > renderY && mouseY < renderY + h;
    }

    public boolean isTextFieldHovered(int mouseX, int mouseY) {
        return ClickGUI.INSTANCE.canSelect(this)
                && mouseX > x + 20 && mouseX < x + w - 20
                && mouseY > renderY + 2 && mouseY < renderY + h - 2
                && !isFilterButtonHovered(mouseX, mouseY);
    }

    public boolean isClearButtonHovered(int mouseX, int mouseY) {
        return ClickGUI.INSTANCE.canSelect(this)
                && mouseX > x + w - 15 && mouseX < x + w - 3
                && mouseY > renderY + 3 && mouseY < renderY + h - 5;
    }

    public boolean isFilterButtonHovered(int mouseX, int mouseY) {
        return ClickGUI.INSTANCE.canSelect(this)
                && mouseX > x + w - 37 && mouseX < x + w - 21
                && mouseY > renderY + h / 2 - 8 && mouseY < renderY + h / 2 + 8;
    }

    public boolean isFocused() {
        return focused && ClientSettingsFrame.guiSettings.searchBar.getValue();
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
        if (ConfigManager.getConfig() != null) {
            ConfigManager.setSearchBarPosition(x, y);
        }
    }

    private void openFilterFrame(int mouseX, int mouseY) {
        dummyButton.getParent().setX(mouseX);
        dummyButton.getParent().setY(mouseY);
        dummyButton.openFrame();
    }
}
