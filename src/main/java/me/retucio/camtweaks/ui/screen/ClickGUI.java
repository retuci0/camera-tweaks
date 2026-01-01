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
import me.retucio.camtweaks.ui.widgets.buttons.settings.ListButton;
import me.retucio.camtweaks.ui.widgets.buttons.ModuleButton;
import me.retucio.camtweaks.ui.widgets.buttons.SettingButton;
import me.retucio.camtweaks.ui.widgets.frames.settings.ClientSettingsFrame;
import me.retucio.camtweaks.ui.widgets.frames.settings.ColorPickerFrame;
import me.retucio.camtweaks.ui.widgets.frames.ModuleFrame;
import me.retucio.camtweaks.ui.widgets.frames.SettingsFrame;
import me.retucio.camtweaks.ui.widgets.Button;
import me.retucio.camtweaks.ui.widgets.Frame;
import me.retucio.camtweaks.ui.widgets.Widget;
import me.retucio.camtweaks.ui.widgets.misc.ScrollBarWidget;
import me.retucio.camtweaks.ui.widgets.misc.SearchBarWidget;
import me.retucio.camtweaks.util.KeyUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.retucio.camtweaks.ui.widgets.frames.settings.ClientSettingsFrame.guiSettings;

// interfaz gráfica, se abre con el shift derecho por defecto. aquí se encuentran los módulos y sus ajustes
public class ClickGUI extends Screen {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    public static ClickGUI INSTANCE;

    private boolean anyFocused;
    private Widget selected = null;

    private final ModuleFrame modulesFrame = new ModuleFrame(20, 30, 100, 20);
    private final List<SettingsFrame> settingsFrames = new ArrayList<>();
    private final ClientSettingsFrame guiSettingsFrame = new ClientSettingsFrame(200, 30, 100, 20);

    private final ScrollBarWidget scrollBar = new ScrollBarWidget();
    private final SearchBarWidget searchBar = new SearchBarWidget(340, 16, 300, 20);
    private final List<Widget> miscWidgets = Arrays.asList(scrollBar, searchBar);

    public ClickGUI() {
        super(Text.of("interfaz"));
        settingsFrames.add(guiSettingsFrame);
        CameraTweaks.EVENT_BUS.register(this);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        scrollBar.setWindowHeight(mc.getWindow().getScaledHeight());
        scrollBar.setContentHeight(calculateContentHeight());
        scrollBar.render(ctx, mouseX, mouseY, delta);

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
        int bottom = modulesFrame.getY() + modulesFrame.getH() + modulesFrame.getTotalHeight();
        for (SettingsFrame frame : settingsFrames)
            bottom = Math.max(bottom, frame.getY() + frame.getH() + frame.getTotalHeight());

        return bottom + 20;  // padding
    }


    public boolean mouseClicked(Click click, boolean doubled) {
        miscWidgets.forEach(w -> w.mouseClicked(
                (int) click.x(),
                (int) click.y(),
                click.button()));

        // detectar clics sobre los marcos
        modulesFrame.mouseClicked((int) click.x(), (int) click.y(), click.button());
        for (SettingsFrame sf : new ArrayList<>(settingsFrames))
            sf.mouseClicked((int) click.x(), (int) click.y(), click.button());

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        miscWidgets.forEach(w -> w.mouseReleased(
                (int) click.x(),
                (int) click.y(),
                click.button()));

        // registrar cuándo se suelta el clic, en cada marco respectivamente
        modulesFrame.mouseReleased((int) click.x(), (int) click.y(), click.button());
        for (SettingsFrame sf : new ArrayList<>(settingsFrames))
            sf.mouseReleased((int) click.x(), (int) click.y(), click.button());

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        miscWidgets.forEach(w -> w.mouseDragged(
                (int) click.x(),
                (int) click.y()));

        for (SettingsFrame sf : new ArrayList<>(settingsFrames))
            sf.mouseDragged((int) click.x(), (int) click.y());

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @SubscribeEvent
    public void onKey(KeyEvent event) {
        searchBar.onKey(event.getKey(), event.getAction());
    }

    @SubscribeEvent
    public void onMouseScroll(MouseScrollEvent event) {
        miscWidgets.forEach(w -> w.mouseScrolled(
                event.getVertical() * guiSettings.scrollSens.getValue()));
    }

    @SubscribeEvent
    public void onMouseMiddleButton(MouseClickEvent event) {
        // mover todos los marcos a un punto visible al presionar shift + la rueda del ratón
        if (CameraTweaks.mc.currentScreen != this || event.getButton() != 2 || !KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) return;

        int h = CameraTweaks.mc.getWindow().getScaledHeight();
        int minY = Math.min(modulesFrame.getY(), settingsFrames.stream().mapToInt(Frame::getY).min().orElse(modulesFrame.getY()));
        int maxY = Math.max(modulesFrame.getY() + modulesFrame.getH(), settingsFrames.stream().mapToInt(sf -> sf.getY() + sf.getH()).max().orElse(modulesFrame.getY() + modulesFrame.getH()));
        int correction = minY < 0 ? -minY + 4 : (maxY > h ? h - maxY - 4 : 0);

        if (correction != 0) {
            modulesFrame.setY(modulesFrame.getY() + correction);
            settingsFrames.forEach(sf -> sf.setY(sf.getY() + correction));
        }
    }

    public void filterSearchResults() {
        if (!guiSettings.searchBar.isEnabled()) return;
        String searchInput = searchBar.getSearchInput().trim();
        if (!guiSettings.matchCase.isEnabled()) searchInput = searchInput.toLowerCase();

        for (Button b : modulesFrame.getButtons()) {
            if (!(b instanceof ModuleButton mb)) return;
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
            for (Button b : sf.getButtons()) {
                if (!(b instanceof SettingButton<?> sb)) return;
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

    // abrir un marco donde se encuentran los ajustes del módulo deseado
    public void openSettingsFrame(Module module, int x, int y) {
        if (isSettingsFrameOpen(module)) return;  // permitir un solo marco de ajustes por módulo
        SettingsFrame frame = new SettingsFrame(module, x, y, 120, 20);
        settingsFrames.add(frame);
        CameraTweaks.EVENT_BUS.post(new SettingsFrameEvent.Open(frame));
    }

    // abrir un marco de ajustes específicamente para ajustes de selección múltiple
    public void openListSettingsFrame(Module dummy, int x, int y) {
        if (isSettingsFrameOpen(dummy)) return;
        SettingsFrame frame = new SettingsFrame(dummy, x, y, 120, 18);
        settingsFrames.add(frame);
    }

    // cerrar el marco de ajustes
    public void closeSettingsFrame(Module module) {
        // porque java.util.ConcurrentModificationException o algo no sé es lo único que se me ha ocurrido hacer
        List<SettingsFrame> toRemove = new ArrayList<>();
        for (SettingsFrame sf : settingsFrames) {
            if ((sf instanceof ColorPickerFrame cpf && cpf.dummyModule == module)  // para los selectores de colores
                    || (!(sf instanceof ColorPickerFrame) && sf.module == module)) { // lógica muy mierdas, lo sé, pero paso de hacerlo bien
                toRemove.add(sf);
                CameraTweaks.EVENT_BUS.post(new SettingsFrameEvent.Close(sf));
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
        }
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

    public void refreshListButtons() {
        for (SettingsFrame frame : settingsFrames)
            for (Button button : frame.getButtons())
                if (button instanceof ListButton<?> lb)
                    lb.refreshDummy();
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

    public List<Widget> getMiscWidgets() { return miscWidgets; }

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

    public Widget getSelected() {
        return selected;
    }

    public void setSelected(Widget widget) {
        this.selected = widget;
    }

    public boolean canSelect(Widget widget) {
        return getSelected() == null || getSelected() == widget;
    }

    public boolean trySelect(Widget widget) {
        if (canSelect(widget)) {
            setSelected(widget);
            return true;
        }
        return false;
    }

    public void unselect(Widget widget) {
        if (selected == widget) setSelected(null);
    }
}
