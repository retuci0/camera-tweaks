package me.retucio.sputnik.ui.hud;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.config.ConfigManager;
import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.input.KeyEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.client.HUD;
import me.retucio.sputnik.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.sputnik.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HudEditorScreen extends Screen {

    public static HudEditorScreen INSTANCE;
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final HUD hud = ModuleManager.INSTANCE.getModuleByClass(HUD.class);

    private final List<HudElement> elements = new ArrayList<>();
    @Nullable private HudElement selected = null;

    private boolean dragging = false;
    private int dragX, dragY;

    public HudEditorScreen() {
        super(Text.literal("editor del HUD"));
        Sputnik.EVENT_BUS.subscribe(this);
    }

    public void setElements(List<HudElement> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, Colors.hudEditorScreenBackgroundColor.getRGB());

        // instrucciones
        ctx.drawCenteredTextWithShadow(mc.textRenderer, title,
                this.width / 2, this.height / 2 - mc.textRenderer.fontHeight, -1);
        ctx.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("arrastrar para mover · clic derecho para visibilidad · ESC para guardar y salir · shift + clic derecho para restablecer"),
                this.width / 2, this.height / 2 + mc.textRenderer.fontHeight, Colors.instructionsTextColor.getRGB());

        // movimiento axial
        if (hud.axialMovement.getValue()) {
            int centerX = mc.getWindow().getScaledWidth() / 2;
            if (dragging && selected != null && Math.abs(centerX - mouseX) < hud.axisOffset.getValue()) {
                dragX = mouseX - centerX + selected.getW() / 2;
                ctx.drawVerticalLine(centerX, 0, mc.getWindow().getScaledHeight(), Colors.CELESTE.getRGB());
            }

            int centerY = mc.getWindow().getScaledHeight() / 2;
            if (dragging && selected != null && Math.abs(centerY - mouseY) < hud.axisOffset.getValue()) {
                dragY = mouseY - centerY + selected.getH() / 2;
                ctx.drawHorizontalLine(0, mc.getWindow().getScaledWidth(), centerY, Colors.CELESTE.getRGB());
            }
        }

        // elementos
        for (HudElement element : elements)
            element.renderInEditor(ctx, hud);

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

        int newX = mouseX - dragX;
        int newY = mouseY - dragY;

        newX = Math.max(1, Math.min(newX, width - selected.getW() - 1));
        newY = Math.max(1, Math.min(newY, height - selected.getH() - 1));

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

    @SubscribeEvent
    // he sustituído el método del súper por un evento para ver si así se podían
    // registrar dos teclas a la vez, pero resulta que no :P
    public void onKey(KeyEvent event) {
        if (!(mc.currentScreen == INSTANCE)) return;
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
                selected.setY(Math.min(mc.getWindow().getScaledHeight() - selected.getH() + 1, selected.getY() + offset));
            }
            if (event.getKey() == GLFW.GLFW_KEY_LEFT) {
                selected.setX(Math.max(1, selected.getX() - offset));
            }
            if (event.getKey() == GLFW.GLFW_KEY_RIGHT) {
                selected.setX(Math.min(mc.getWindow().getScaledWidth() - selected.getW() + 1, selected.getX() + offset));
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
    protected void applyBlur(DrawContext ctx) {
        if (ClientSettingsFrame.guiSettings.blur.getValue()) {
            super.applyBlur(ctx);
        }
    }

    public boolean isSelected(HudElement element) {
        return selected == element;
    }
}