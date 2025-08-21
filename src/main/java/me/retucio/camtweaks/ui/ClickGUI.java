package me.retucio.camtweaks.ui;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.Subscribe;
import me.retucio.camtweaks.event.events.MouseScrollEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.ui.settings.ClickGUISettingsFrame;
import me.retucio.camtweaks.ui.settings.SettingsFrame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static me.retucio.camtweaks.ui.settings.ClickGUISettingsFrame.guiSettings;

// interfaz gráfica, se abre con el shift derecho por defecto. aquí se encuentran los módulos y sus ajustes
public class ClickGUI extends Screen {

    public static final ClickGUI INSTANCE = new ClickGUI();

    private final ModuleFrame modulesFrame = new ModuleFrame(20, 30, 100, 20);
    private final List<SettingsFrame> settingsFrames = new ArrayList<>();
    private final ClickGUISettingsFrame guiSettingsFrame = new ClickGUISettingsFrame(200, 30, 100, 20);

    protected ClickGUI() {
        super(Text.of("interfaz"));
        settingsFrames.add(guiSettingsFrame);
        CameraTweaks.EVENT_BUS.register(this);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // renderizar el marco de los módulos
        modulesFrame.render(ctx, mouseX, mouseY, delta);
        modulesFrame.updatePosition(mouseX, mouseY);

        // renderizar el marco de los ajustes de cada módulo que lo tenga abierto. se abre haciendo clic derecho sobre el módulo
        for (SettingsFrame sf : new ArrayList<>(settingsFrames)) {
            sf.render(ctx, mouseX, mouseY, delta);
            sf.updatePosition(mouseX, mouseY);
        }

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

    @Subscribe
    public void onMouseScroll(MouseScrollEvent event) {
        // subir y bajar la pantalla con la rueda del ratón
        if (CameraTweaks.mc.currentScreen != this) return;

        modulesFrame.y += (int) (event.getVertical() * guiSettings.scrollSens.getValue());
        for (SettingsFrame sf : settingsFrames)
            sf.y += (int) (event.getVertical() * guiSettings.scrollSens.getValue());
    }

    public void openSettingsFrame(Module module, int x, int y) {
        // abrir un marco donde se encuentran los ajustes del módulo deseado
        if (isSettingsFrameOpen(module)) return;  // permitir un solo marco de ajustes por módulo
        settingsFrames.add(new SettingsFrame(module, x + 120, y, 120, 18));
    }

    // cerrar el marco de ajustes
    public void closeSettingsFrame(Module module) {
        settingsFrames.removeIf(sf -> sf.module == module);
    }

    // verificar si un módulo tiene su marco de ajustes abierto
    public boolean isSettingsFrameOpen(Module module) {
        return settingsFrames.stream()
                .anyMatch(sf -> sf.module.getName().equals(module.getName()));
    }

    // no pausar el juego cuando se abre la interfaz
    @Override
    public boolean shouldPause() {
        return false;
    }

    public ModuleFrame getModulesFrame() {
        return modulesFrame;
    }

    public ClickGUISettingsFrame getGuiSettingsFrame() {
        return guiSettingsFrame;
    }

    public List<SettingsFrame> getSettingsFrames() {
        return settingsFrames;
    }

}
