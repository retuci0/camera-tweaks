package me.retucio.camtweaks.ui;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.MouseClickEvent;
import me.retucio.camtweaks.event.events.MouseScrollEvent;
import me.retucio.camtweaks.event.events.camtweaks.SettingsFrameEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.ui.buttons.ListButton;
import me.retucio.camtweaks.ui.buttons.SettingButton;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import me.retucio.camtweaks.ui.frames.ModuleFrame;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static me.retucio.camtweaks.ui.frames.ClientSettingsFrame.guiSettings;

// interfaz gráfica, se abre con el shift derecho por defecto. aquí se encuentran los módulos y sus ajustes
public class ClickGUI extends Screen {

    public static ClickGUI INSTANCE;
    private boolean anyFocused;

    private final ModuleFrame modulesFrame = new ModuleFrame(20, 30, 100, 20);
    private final List<SettingsFrame> settingsFrames = new ArrayList<>();
    private final ClientSettingsFrame guiSettingsFrame = new ClientSettingsFrame(200, 30, 100, 20);

    public ClickGUI() {
        super(Text.of("interfaz"));
        settingsFrames.add(guiSettingsFrame);
        CameraTweaks.EVENT_BUS.register(this);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // renderizar el marco de los ajustes de cada módulo que lo tenga abierto. se abre haciendo clic derecho sobre el módulo
        for (SettingsFrame sf : settingsFrames.reversed()) {
            sf.render(ctx, mouseX, mouseY, delta);
            sf.updatePosition(mouseX, mouseY);
        }

        // renderizar el marco de los módulos
        modulesFrame.render(ctx, mouseX, mouseY, delta);
        modulesFrame.updatePosition(mouseX, mouseY);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // detectar clics sobre los marcos
        modulesFrame.mouseClicked(mouseX, mouseY, button);
        for (SettingsFrame sf : new ArrayList<>(settingsFrames))
            sf.mouseClicked(mouseX, mouseY, button);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // registrar cuándo se suelta el clic, en cada marco respectivamente
        modulesFrame.mouseReleased(mouseX, mouseY, button);
        for (SettingsFrame sf : new ArrayList<>(settingsFrames))
            sf.mouseRelease(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @SubscribeEvent
    public void onMouseScroll(MouseScrollEvent event) {
        // subir y bajar la pantalla con la rueda del ratón
        if (CameraTweaks.mc.currentScreen != this) return;

        modulesFrame.y += (int) (event.getVertical() * guiSettings.scrollSens.getValue());
        for (SettingsFrame sf : settingsFrames)
            sf.y += (int) (event.getVertical() * guiSettings.scrollSens.getValue());
    }

    @SubscribeEvent
    public void onMouseMiddleButton(MouseClickEvent event) {
        // mover todos los marcos a un punto visible al presionar shift + la rueda del ratón
        if (CameraTweaks.mc.currentScreen != this || event.getButton() != 2 || !KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) return;

        int h = CameraTweaks.mc.getWindow().getScaledHeight();
        int minY = Math.min(modulesFrame.y, settingsFrames.stream().mapToInt(sf -> sf.y).min().orElse(modulesFrame.y));
        int maxY = Math.max(modulesFrame.y + modulesFrame.h, settingsFrames.stream().mapToInt(sf -> sf.y + sf.h).max().orElse(modulesFrame.y + modulesFrame.h));
        int correction = minY < 0 ? -minY + 4 : (maxY > h ? h - maxY - 4 : 0);
        if (correction != 0) {
            modulesFrame.y += correction;
            settingsFrames.forEach(sf -> sf.y += correction);
        }
    }

    public void openSettingsFrame(Module module, int x, int y) {
        // abrir un marco donde se encuentran los ajustes del módulo deseado
        if (isSettingsFrameOpen(module)) return;  // permitir un solo marco de ajustes por módulo
        SettingsFrame frame = new SettingsFrame(module, x, y, 120, 20);
        settingsFrames.add(frame);
        CameraTweaks.EVENT_BUS.post(new SettingsFrameEvent.Open(frame));
    }

    public void openListSettingsFrame(Module module, int x, int y) {
        if (isSettingsFrameOpen(module)) return;
        SettingsFrame frame = new SettingsFrame(module, x, y, 120, 18);
        settingsFrames.add(frame);
    }

    // cerrar el marco de ajustes
    public void closeSettingsFrame(Module module) {
        // porque java.util.ConcurrentModificationException o algo no sé es lo único que se me ha ocurrido hacer
        List<SettingsFrame> toRemove = new ArrayList<>();
        for (SettingsFrame sf : settingsFrames) {
            if (sf.module == module) {
                CameraTweaks.EVENT_BUS.post(new SettingsFrameEvent.Close(sf));
                toRemove.add(sf);
            }
        }
        settingsFrames.removeAll(toRemove);
    }

    // verificar si un módulo tiene su marco de ajustes abierto
    public boolean isSettingsFrameOpen(Module module) {
        return settingsFrames.stream()
                .anyMatch(sf -> sf.module.getName().equals(module.getName()));
    }

    public void refreshListButtons() {
        for (SettingsFrame frame : settingsFrames)
            for (SettingButton button : frame.getButtons())
                if (button instanceof ListButton<?> lb)
                    lb.refreshDummy();
    }

    // no pausar el juego cuando se abre la interfaz
    @Override
    public boolean shouldPause() {
        return false;
    }

    public ModuleFrame getModulesFrame() {
        return modulesFrame;
    }

    public ClientSettingsFrame getGuiSettingsFrame() {
        return guiSettingsFrame;
    }

    public void setAnyFocused(boolean anyFocused) {
        this.anyFocused = anyFocused;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return super.shouldCloseOnEsc() && !anyFocused;
    }

    public List<SettingsFrame> getSettingsFrames() {
        return settingsFrames;
    }
}
