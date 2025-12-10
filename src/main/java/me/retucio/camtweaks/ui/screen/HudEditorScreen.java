package me.retucio.camtweaks.ui.screen;

import me.retucio.camtweaks.config.ConfigManager;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import me.retucio.camtweaks.ui.widgets.HudElement;
import me.retucio.camtweaks.util.Colors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;
import org.jetbrains.annotations.Nullable;

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
        super(Text.literal("editor del hud"));
    }

    public void setElements(List<HudElement> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, Colors.hudEditorScreenBackgroundColor.getRGB());

        // texto con instrucciones
        ctx.drawCenteredTextWithShadow(mc.textRenderer, "EDITOR DEL HUD", this.width / 2, this.height / 2 - mc.textRenderer.fontHeight, -1);
        ctx.drawCenteredTextWithShadow(mc.textRenderer,
                Text.literal("arrastrar para mover · click derecho para alternar visibilidad · esc para guardar y salir · shift + click derecho para restablecer"),
                this.width / 2, this.height / 2 + mc.textRenderer.fontHeight, Colors.instructionsTextColor.getRGB());

        // dibujar elementos
        for (HudElement element : elements)
            element.renderPreview(ctx);

        // tooltips con información sobre el elemento sobre el que está el puntero del ratón
        if (!dragging) {
            for (HudElement element : elements) {
                if (!element.isVisible()) continue;

                if (element.contains(mouseX, mouseY)) {
                    List<Text> tooltip = getTooltipFor(element);
                    if (!tooltip.isEmpty()) ctx.drawTooltip(mc.textRenderer, tooltip, mouseX + 7, mouseY + 20);
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

        // iterar de atrás a adelante para respetar el orden z
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement element = elements.get(i);
            if (element.contains(mouseX, mouseY)) {
                selected = element;

                if (click.button() == 0) {
                    dragging = true;

                    dragOffsetX = mouseX - element.getX();
                    dragOffsetY = mouseY - element.getY();

                    // traer el elemento seleccionado al frente
                    elements.remove(i);
                    elements.add(element);
                    return true;

                } else if (click.button() == 1) {
                    if (mc.isShiftPressed())
                        selected.resetPosition();
                    else
                        selected.setVisible(!selected.isVisible());
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

        // clampear para evitar que los elementos se salgan de la pantalla
        newX = Math.max(1, Math.min(newX, width - selected.getWidth() - 1));
        newY = Math.max(1, Math.min(newY, height - selected.getHeight() - 1));

        selected.setPosition(newX, newY);
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging && click.button() == 0) {
            dragging = false;
            savePositionsToConfig();  // guardar inmediatamente al config
            return true;
        } else if (click.button() == 1 && selected != null) {
            saveVisibilitiesToConfig();
            selected = null;
            return true;
        }
        selected = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        // esc para guardar y salir
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            saveAndClose();
            return true;
        }
        return super.keyPressed(input);
    }

    private void savePositionsToConfig() {
        for (HudElement element : elements)
            ConfigManager.setHudPosition(element.getId(), element.getX(), element.getY());

        ConfigManager.save();
    }

    private void saveVisibilitiesToConfig() {
        for (HudElement element : elements)
            ConfigManager.setHudVisibility(element.getId(), element.isVisible());

        ConfigManager.save();
    }

    private void saveAndClose() {
        savePositionsToConfig();
        selected = null;
        mc.setScreen(null);
    }

    @Override
    protected void applyBlur(DrawContext ctx) {
        if (ClientSettingsFrame.guiSettings.blur.isEnabled()) super.applyBlur(ctx);
    }

    public boolean isSelected(HudElement element) {
        return selected == element;
    }

    // para los tooltips
    private List<Text> getTooltipFor(HudElement element) {
        List<Text> list = new ArrayList<>();

        switch (element.getId()) {
            case "coords" -> {
                list.add(Text.literal("coordenadas"));
                list.add(Text.literal("te muestra tu posición en el mundo en coordenadas XYZ"));
            }
            case "fps" -> {
                list.add(Text.literal("FPS"));
                list.add(Text.literal("te muestra la tasa de fotogramas por segundo del juego"));
            }
            case "tps" -> {
                list.add(Text.literal("tps"));
                list.add(Text.literal("te muestra la tasa de ticks por segundo del servidor (0-20)"));
            }
            case "customText" -> {
                list.add(Text.literal("texto custom"));
                list.add(Text.literal("te permite renderizar texto al gusto"));
            }
            case "time" -> {
                list.add(Text.literal("hora"));
                list.add(Text.literal("te muestra la hora y los minutos de la zona horaria escogida"));
            }
            default -> {
                list.add(Text.literal(element.getId()));
                list.add(Text.literal("."));
            }
        }

        return list;
    }

}
