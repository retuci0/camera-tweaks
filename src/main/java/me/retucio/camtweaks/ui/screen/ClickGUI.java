package me.retucio.camtweaks.ui.screen;

import me.retucio.camtweaks.CameraTweaks;
import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.KeyEvent;
import me.retucio.camtweaks.event.events.MouseClickEvent;
import me.retucio.camtweaks.event.events.MouseScrollEvent;
import me.retucio.camtweaks.event.events.camtweaks.SettingsFrameEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.Setting;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.ui.buttons.ListButton;
import me.retucio.camtweaks.ui.buttons.ModuleButton;
import me.retucio.camtweaks.ui.buttons.SettingButton;
import me.retucio.camtweaks.ui.frames.ClientSettingsFrame;
import me.retucio.camtweaks.ui.frames.ColorPickerFrame;
import me.retucio.camtweaks.ui.frames.ModuleFrame;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.ui.widgets.ScrollBarWidget;
import me.retucio.camtweaks.ui.widgets.SearchBarWidget;
import me.retucio.camtweaks.util.KeyUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
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
    private Object selected = null;

    private final ModuleFrame modulesFrame = new ModuleFrame(20, 30, 100, 20);
    private final List<SettingsFrame> settingsFrames = new ArrayList<>();
    private final ClientSettingsFrame guiSettingsFrame = new ClientSettingsFrame(200, 30, 100, 20);

    private final ScrollBarWidget scrollBar = new ScrollBarWidget();
    private final SearchBarWidget searchBar = new SearchBarWidget(340, 16, 300, 20);

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public ClickGUI() {
        super(Text.of("interfaz"));
        settingsFrames.add(guiSettingsFrame);
        CameraTweaks.EVENT_BUS.register(this);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        scrollBar.setWindowHeight(mc.getWindow().getScaledHeight());
        scrollBar.setContentHeight(calculateContentHeight());
        scrollBar.render(ctx, mouseX, mouseY);

        int scrollOffset = scrollBar.getScrollOffset();

        searchBar.updateRenderY(scrollOffset);
        searchBar.render(ctx, mouseX, mouseY, delta);
        searchBar.updatePosition(mouseX, mouseY);

        // actualizar la posición de renderizado vertical de los marcos cada tick
        modulesFrame.updateRenderY(scrollOffset);
        for (SettingsFrame sf : settingsFrames)
            sf.updateRenderY(scrollOffset);

        // renderizar el marco de los ajustes de cada módulo que lo tenga abierto. se abre haciendo clic derecho sobre el módulo
        for (SettingsFrame sf : settingsFrames.reversed()) {
            sf.render(ctx, mouseX, mouseY, delta);
            sf.updatePosition(mouseX, mouseY);
        }

        // renderizar el marco de los módulos
        modulesFrame.render(ctx, mouseX, mouseY, delta);
        modulesFrame.updatePosition(mouseX, mouseY);

        for (SettingsFrame sf : settingsFrames)
            sf.drawTooltips(ctx, mouseX, mouseY);

        filterSearchResults();

        renderBottomGradient(ctx, scrollOffset);

        super.render(ctx, mouseX, mouseY, delta);
    }

    // renderizar un gradiente negro leve en la parte inferior de la pantalla si el contenido excede el límite inferior de la pantalla, para indicarlo visualmente
    private void renderBottomGradient(DrawContext ctx, int scrollOffset) {
        int screenHeight = mc.getWindow().getScaledHeight();
        int totalContentHeight = calculateContentHeight();

        if (scrollOffset + screenHeight < totalContentHeight) {
            int gradientHeight = 30;
            int startY = screenHeight - gradientHeight;

            for (int y = 0; y < gradientHeight; y++) {
                float alpha = (float) y / gradientHeight;
                int color = (int) (alpha * 0.7 * 255) << 24;

                ctx.fill(0, startY + y, mc.getWindow().getScaledWidth(), startY + y + 1, color);
            }
        }
    }

    private int calculateContentHeight() {
        int bottom = modulesFrame.y + modulesFrame.h + modulesFrame.totalHeight;
        for (SettingsFrame frame : settingsFrames)
            bottom = Math.max(bottom, frame.y + frame.h + frame.totalHeight);

        return bottom + 20;  // padding
    }


    public boolean mouseClicked(Click click, boolean doubled) {
        scrollBar.mouseClicked(click.x(), click.y(), click.button());
        searchBar.mouseClicked(click.x(), click.y(), click.button());

        // detectar clics sobre los marcos
        modulesFrame.mouseClicked(click.x(), click.y(), click.button());
        for (SettingsFrame sf : new ArrayList<>(settingsFrames))
            sf.mouseClicked(click.x(), click.y(), click.button());

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        scrollBar.mouseReleased(click.x(), click.y(), click.button());
        searchBar.mouseReleased(click.x(), click.y(), click.button());

        // registrar cuándo se suelta el clic, en cada marco respectivamente
        modulesFrame.mouseReleased(click.x(), click.y(), click.button());
        for (SettingsFrame sf : new ArrayList<>(settingsFrames))
            sf.mouseReleased(click.x(), click.y(), click.button());
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        scrollBar.mouseDragged(click.y());
        for (SettingsFrame sf : new ArrayList<>(settingsFrames))
            sf.mouseDragged(click.x(), click.y());
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @SubscribeEvent
    public void onKey(KeyEvent event) {
        searchBar.onKey(event.getKey(), event.getAction());
    }

    @SubscribeEvent
    public void onMouseScroll(MouseScrollEvent event) {
        // subir y bajar la pantalla con la rueda del ratón
        scrollBar.onMouseScroll(event.getVertical() * guiSettings.scrollSens.getValue());
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

    public void filterSearchResults() {
        if (!guiSettings.searchBar.isEnabled()) return;
        String searchInput = searchBar.getSearchInput().trim();
        if (!guiSettings.matchCase.isEnabled()) searchInput = searchInput.toLowerCase();

        for (ModuleButton mb : modulesFrame.getButtons()) {
            Module module = mb.getModule();

            if (searchInput.isEmpty()) {
                module.setSearchMatch(true);
                continue;
            }

            String name = module.getName();
            String description = module.getDescription();

            if (!guiSettings.matchCase.isEnabled()) {
                name = name.toLowerCase();
                description = description.toLowerCase();
            }

            module.setSearchMatch(name.contains(searchInput) || description.contains(searchInput));
        }

        for (SettingsFrame sf : settingsFrames) {
            for (SettingButton sb : sf.getButtons()) {
                Setting setting = sb.getSetting();

                if (searchInput.isEmpty()) {
                    setting.setSearchMatch(true);
                    continue;
                }

                String name = setting.getName();
                String description = setting.getDescription();

                if (!guiSettings.matchCase.isEnabled()) {
                    name = name.toLowerCase();
                    description = description.toLowerCase();
                }

                setting.setSearchMatch(name.contains(searchInput) || description.contains(searchInput));
            }
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
            if ((sf instanceof ColorPickerFrame cpf && cpf.dummyModule == module)  // para los selectores de colores
                    || (!(sf instanceof ColorPickerFrame) && sf.module == module)) {    // lógica muy mierdas, lo sé, pero paso de hacerlo bien
                CameraTweaks.EVENT_BUS.post(new SettingsFrameEvent.Close(sf));
                toRemove.add(sf);
                unselect(sf);
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

    public boolean isColorPickerFrameOpen(ColorSetting setting) {
        for (SettingsFrame sf : getSettingsFrames()) {
            if (sf instanceof ColorPickerFrame cpf)
                if (cpf.getColorSetting().equals(setting)) return true;
        }
        return false;
    }

    public void openColorPickerFrame(Module module, ColorSetting colorSetting, int x, int y) {
        ColorPickerFrame frame = new ColorPickerFrame(module, colorSetting, x + 80, y + 5, 153, 20);
        if (isColorPickerFrameOpen(frame.getColorSetting())) {
            closeColorPickerFrame(frame.getColorSetting());
            return;
        };
        settingsFrames.add(frame);
    }

    public void closeColorPickerFrame(ColorSetting setting) {
        for (SettingsFrame sf : getSettingsFrames()) {
            if (sf instanceof ColorPickerFrame cpf && cpf.getColorSetting().equals(setting)) {
                settingsFrames.remove(cpf);
                break;
            }
        }
    }

    // métodos del súper

    @Override
    public void close() {  // evitar que al reabrir la interfaz sin previamente haber soltado el clic, se sigan arrastrando objetos
        modulesFrame.mouseReleased(0, 0, 0);
        settingsFrames.forEach(sf -> sf.mouseReleased(0, 0, 0));
        scrollBar.mouseReleased(0, 0, 0);
        searchBar.mouseReleased(0, 0, 0);

        unselect(selected);
        super.close();
    }

    @Override
    public boolean shouldPause() {
        // no pausar el juego cuando se abre la interfaz
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return super.shouldCloseOnEsc() && !anyFocused;
    }

    @Override
    protected void applyBlur(DrawContext ctx) {
        if (guiSettings.blur.isEnabled()) super.applyBlur(ctx);
    }


    // getters y setters de widgets

    public ModuleFrame getModulesFrame() {
        return modulesFrame;
    }

    public ClientSettingsFrame getGuiSettingsFrame() {
        return guiSettingsFrame;
    }

    public List<SettingsFrame> getSettingsFrames() {
        return settingsFrames;
    }

    public SearchBarWidget getSearchBar() {
        return searchBar;
    }

    public void setAnyFocused(boolean anyFocused) {
        this.anyFocused = anyFocused;
    }


    // selección de widgets

    public Object getSelected() {
        return selected;
    }

    public void setSelected(Object object) {
        this.selected = object;
    }

    public boolean canSelect(Object object) {
        return getSelected() == null || getSelected() == object;
    }

    public boolean trySelect(Object object) {
        if (canSelect(object)) {
            setSelected(object);
            return true;
        }
        return false;
    }

    public void unselect(Object object) {
        if (selected == object) setSelected(null);
    }
}
