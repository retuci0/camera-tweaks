package me.retucio.camtweaks.ui.hud;

import me.retucio.camtweaks.config.ConfigManager;
import me.retucio.camtweaks.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HudEditorScreen extends Screen {

    public static HudEditorScreen INSTANCE;
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final List<HudElement> elements = new ArrayList<>();
    @Nullable private HudElement selected = null;

    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() {
        super(Text.literal("editor del HUD"));
    }

    public void setElements(List<HudElement> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, Colors.hudEditorScreenBackgroundColor.getRGB());

        // instrucciones
        ctx.drawCenteredTextWithShadow(mc.textRenderer, "editor del HUD",
                this.width / 2, this.height / 2 - mc.textRenderer.fontHeight, -1);
        ctx.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("arrastrar para mover · clic derecho para visibilidad · ESC para guardar y salir · shift + clic derecho para restablecer"),
                this.width / 2, this.height / 2 + mc.textRenderer.fontHeight, Colors.instructionsTextColor.getRGB());

        // elementos
        for (HudElement element : elements)
            element.renderInEditor(ctx, null);

        // tooltips
        if (!dragging) {
            for (HudElement element : elements) {
                if (!element.isVisible()) continue;

                if (element.isHovered(mouseX, mouseY)) {
                    List<Text> tooltip = element.getTooltip();
                    if (!tooltip.isEmpty())
                        ctx.drawTooltip(mc.textRenderer, tooltip, mouseX + 7, mouseY + 20);
                    break;
                }
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();

        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement element = elements.get(i);
            if (element.isHovered(mouseX, mouseY)) {
                selected = element;

                if (click.button() == 0) {
                    dragging = true;
                    dragOffsetX = mouseX - element.getX();
                    dragOffsetY = mouseY - element.getY();

                    elements.remove(i);
                    elements.add(element);
                    return true;

                } else if (click.button() == 1) {
                    if (mc.isShiftPressed()) {
                        selected.resetPosition();
                    } else {
                        selected.setVisible(!selected.isVisible());
                    }
                    saveElementToConfig(selected);
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (!dragging || selected == null || click.button() != 0)
            return super.mouseDragged(click, deltaX, deltaY);

        int mouseX = (int) click.x();
        int mouseY = (int) click.y();

        int newX = mouseX - dragOffsetX;
        int newY = mouseY - dragOffsetY;

        newX = Math.max(1, Math.min(newX, width - selected.getWidth() - 1));
        newY = Math.max(1, Math.min(newY, height - selected.getHeight() - 1));

        selected.setPosition(newX, newY);
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging && click.button() == 0) {
            dragging = false;
            if (selected != null)
                saveElementToConfig(selected);
            return true;
        }

        selected = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            saveAllElementsToConfig();
            selected = null;
            mc.setScreen(null);
            return true;
        }
        return super.keyPressed(input);
    }

    private void saveElementToConfig(HudElement element) {
        ConfigManager.setHudPosition(element.getId(), element.getX(), element.getY());
        ConfigManager.setHudVisibility(element.getId(), element.isVisible());
        ConfigManager.save();
    }

    private void saveAllElementsToConfig() {
        for (HudElement element : elements) {
            saveElementToConfig(element);
        }
    }

    @Override
    protected void applyBlur(DrawContext ctx) {
        if (ClientSettingsFrame.guiSettings.blur.isEnabled()) {
            super.applyBlur(ctx);
        }
    }

    public boolean isSelected(HudElement element) {
        return selected == element;
    }
}