package me.retucio.sputnik.ui.hud;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.config.ConfigManager;
import me.retucio.sputnik.event.input.KeyEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;


public class HudEditorScreen extends Screen {

    public static HudEditorScreen INSTANCE;
    private final Minecraft mc = Minecraft.getInstance();
    private final HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);

    private final List<HudElement> elements = new ArrayList<>();
    @Nullable private HudElement selected = null;

    private boolean dragging = false;
    private int dragX, dragY;

    public HudEditorScreen() {
        super(Component.literal("editor del HUD"));
        Sputnik.EVENT_BUS.subscribe(this);
    }

    public void setElements(List<HudElement> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        gui.fill(0, 0, this.width, this.height, Colors.hudEditorScreenBackgroundColor.getRGB());

        // instrucciones
        gui.centeredText(mc.font, title, this.width / 2, this.height / 2 - mc.font.lineHeight, -1);
        gui.centeredText(mc.font,
                Component.literal("arrastrar para mover · clic derecho para visibilidad · ESC para guardar y salir · shift + clic derecho para restablecer"),
                this.width / 2, this.height / 2 + mc.font.lineHeight, Colors.instructionsTextColor.getRGB());

        // movimiento axial
        if (hud.axialMovement.getValue()) {
            int centerX = mc.getWindow().getGuiScaledWidth() / 2;
            if (dragging && selected != null && Math.abs(centerX - mouseX) < hud.axisOffset.getValue()) {
                dragX = mouseX - centerX + selected.getW() / 2;
                gui.verticalLine(centerX, 0, mc.getWindow().getGuiScaledHeight(), Colors.CELESTE.getRGB());
            }

            int centerY = mc.getWindow().getGuiScaledHeight() / 2;
            if (dragging && selected != null && Math.abs(centerY - mouseY) < hud.axisOffset.getValue()) {
                dragY = mouseY - centerY + selected.getH() / 2;
                gui.horizontalLine(0, mc.getWindow().getGuiScaledWidth(), centerY, Colors.CELESTE.getRGB());
            }
        }

        // elementos
        for (HudElement element : elements) {
            element.renderInEditor(gui, hud);
        }

        // tooltips
        if (!dragging) {
            for (HudElement element : elements) {
                if (!element.isVisible()) continue;

                if (element.isHovered(mouseX, mouseY)) {
                    List<Component> tooltip = element.getTooltip();
                    if (!tooltip.isEmpty())
                        gui.setComponentTooltipForNextFrame(mc.font, tooltip, mouseX + 7, mouseY + 20);
                    break;
                }
            }
        }

        super.extractRenderState(gui, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();

        for (HudElement element : elements) {
            if (element.isHovered(mouseX, mouseY)) {
                selected = element;

                if (click.button() == 0) {
                    dragging = true;
                    dragX = mouseX - element.getX();
                    dragY = mouseY - element.getY();

                    elements.remove(element);
                    elements.add(element);
                    return true;

                } else if (click.button() == 1) {
                    if (KeyUtil.isShiftDown()) {
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
    public boolean mouseDragged(@NonNull MouseButtonEvent click, double deltaX, double deltaY) {
        if (!dragging || selected == null || click.button() != 0)
            return super.mouseDragged(click, deltaX, deltaY);

        int mouseX = (int) click.x();
        int mouseY = (int) click.y();

        int newX = mouseX - dragX;
        int newY = mouseY - dragY;

        newX = Math.clamp(newX, 1, width - selected.getW() - 1);
        newY = Math.clamp(newY, 1, height - selected.getH() - 1);

        selected.setPosition(newX, newY);
        return true;
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent click) {
        if (dragging && click.button() == 0) {
            dragging = false;
            if (selected != null)
                saveElementToConfig(selected);
            return true;
        }

        selected = null;
        return super.mouseReleased(click);
    }

    // he sustituído el método del súper por un evento para ver si así se podían
    // registrar dos teclas a la vez, pero resulta que no :P
    public void onKey(KeyEvent event) {
        if (!(mc.screen == INSTANCE)) return;
        if (event.getAction() == GLFW.GLFW_RELEASE) return;
        event.cancel();

        // guardar y salir
        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) {
            saveAllElementsToConfig();
            selected = null;
            mc.setScreen(null);
        }
        // movimiento de los elementos con las teclas de las flechas
        else if (hud.arrowMovement.getValue() && selected != null) {
            int offset = hud.arrowOffset.getIntValue();
            if (event.getKey() == GLFW.GLFW_KEY_UP) {
                selected.setY(Math.max(1, selected.getY() - offset));
            }
            if (event.getKey() == GLFW.GLFW_KEY_DOWN) {
                selected.setY(Math.min(mc.getWindow().getGuiScaledHeight() - selected.getH() + 1, selected.getY() + offset));
            }
            if (event.getKey() == GLFW.GLFW_KEY_LEFT) {
                selected.setX(Math.max(1, selected.getX() - offset));
            }
            if (event.getKey() == GLFW.GLFW_KEY_RIGHT) {
                selected.setX(Math.min(mc.getWindow().getGuiScaledWidth() - selected.getW() + 1, selected.getX() + offset));
            }
        }
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
    protected void extractBlurredBackground(@NonNull GuiGraphicsExtractor gui) {
        if (ClientSettingsFrame.guiSettings.blur.getValue()) {
            super.extractBlurredBackground(gui);
        }
    }

    public boolean isSelected(HudElement element) {
        return selected == element;
    }
}