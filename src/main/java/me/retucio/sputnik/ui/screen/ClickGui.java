package me.retucio.sputnik.ui.screen;

import com.github.retucio.neutrino.EventListener;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.input.KeyEvent;
import me.retucio.sputnik.event.input.MouseClickEvent;
import me.retucio.sputnik.event.input.MouseScrollEvent;
import me.retucio.sputnik.event.sputnik.SettingsPanelEvent;
import me.retucio.sputnik.friend.Friend;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.Setting;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.ui.widgets.buttons.FriendButton;
import me.retucio.sputnik.ui.widgets.buttons.settings.ListButton;
import me.retucio.sputnik.ui.widgets.buttons.ModuleButton;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.ui.widgets.panels.FriendsPanel;
import me.retucio.sputnik.ui.widgets.panels.settings.ClientSettingsPanel;
import me.retucio.sputnik.ui.widgets.panels.settings.ColorPickerPanel;
import me.retucio.sputnik.ui.widgets.panels.ModulePanel;
import me.retucio.sputnik.ui.widgets.panels.SettingsPanel;
import me.retucio.sputnik.ui.widgets.Button;
import me.retucio.sputnik.ui.widgets.Panel;
import me.retucio.sputnik.ui.widgets.Widget;
import me.retucio.sputnik.ui.widgets.misc.ScrollBarWidget;
import me.retucio.sputnik.ui.widgets.misc.SearchBarWidget;
import me.retucio.sputnik.ui.widgets.panels.settings.FriendSettingsPanel;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.KeyUtil;

import me.retucio.sputnik.util.MiscUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static me.retucio.sputnik.ui.widgets.panels.settings.ClientSettingsPanel.clientSettings;


/**
 * interfaz gráfica, Screen que se abre con el shift derecho por defecto. aquí se encuentran los módulos y sus ajustes.
 * @author retucio
 */

public class ClickGui extends Screen {

    private final Minecraft mc = Minecraft.getInstance();
    public static ClickGui INSTANCE;

    private boolean anyFocused;
    private @Nullable Widget selected = null;

    private final List<ModulePanel> modulePanels = new ArrayList<>();
    private final List<SettingsPanel> settingsPanels = new ArrayList<>();
    private final ClientSettingsPanel clientSettingsPanel = new ClientSettingsPanel(620, 280, 100, 20);
    private final FriendsPanel friendsPanel = new FriendsPanel(40, 280, 100, 20);

    private final ScrollBarWidget scrollBar = new ScrollBarWidget();
    private final SearchBarWidget searchBar = new SearchBarWidget(320, 10, 320, 20);
    private final List<Widget> miscWidgets = List.of(scrollBar, searchBar);

    public ClickGui() {
        super(Component.literal("interfaz"));
        Sputnik.EVENT_BUS.subscribe(this);

        settingsPanels.add(clientSettingsPanel);

        int x = 4;
        for (Category category : Category.values()) {
            modulePanels.add(new ModulePanel(category, x, 40, 100, 20));
            x += 88;
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        scrollBar.setWindowHeight(mc.getWindow().getGuiScaledHeight());
        scrollBar.setContentHeight(calculateContentHeight());
        scrollBar.render(gui, mouseX, mouseY, delta);

        int scrollOffset = scrollBar.getScrollOffset();

        searchBar.updateRenderY(scrollOffset);
        searchBar.render(gui, mouseX, mouseY, delta);
        searchBar.updatePosition(mouseX, mouseY);

        // actualizar la posición de renderizado vertical de los paneles cada tick
        friendsPanel.updateRenderY(scrollOffset);
        for (ModulePanel mp : modulePanels) {
            mp.updateRenderY(scrollOffset);
        }
        for (SettingsPanel sp : settingsPanels) {
            sp.updateRenderY(scrollOffset);
        }

        // renderizar el panel de los ajustes de cada módulo que lo tenga abierto. se abre haciendo clic derecho sobre el módulo.
        for (SettingsPanel sp : settingsPanels.reversed()) {
            sp.render(gui, mouseX, mouseY, delta);
            sp.updatePosition(mouseX, mouseY);
        }

        // renderizar los paneles de los módulos
        for (ModulePanel mp : modulePanels) {
            mp.render(gui, mouseX, mouseY, delta);
            mp.updatePosition(mouseX, mouseY);
        }

        for (SettingsPanel sp : settingsPanels) {
            sp.drawTooltips(gui, mouseX, mouseY);
        }

        friendsPanel.render(gui, mouseX, mouseY, delta);
        friendsPanel.updatePosition(mouseX, mouseY);

        filterSearchResults();

        renderBottomGradient(gui, scrollOffset);

        super.extractRenderState(gui, mouseX, mouseY, delta);
    }

    // renderizar un gradiente negro leve en la parte inferior de la pantalla si el contenido excede el límite inferior de la pantalla, para indicarlo visualmente
    private void renderBottomGradient(GuiGraphicsExtractor ctx, int scrollOffset) {
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int totalContentHeight = calculateContentHeight();

        if (scrollOffset + screenHeight < totalContentHeight) {
            int gradientHeight = 30;
            int startY = screenHeight - gradientHeight;

            for (int y = 0; y < gradientHeight; y++) {
                float alpha = (float) y / gradientHeight;
                int color = (int) (alpha * 0.7 * 255) << 24;

                ctx.fill(0, startY + y, mc.getWindow().getGuiScaledWidth(), startY + y + 1, color);
            }
        }
    }

    private int calculateContentHeight() {
        int bottom = mc.getWindow().getGuiScaledHeight();
        for (ModulePanel mp : modulePanels) {
            bottom = mp.getY() + mp.getH() + mp.getTotalHeight();
        }
        for (SettingsPanel sp : settingsPanels) {
            bottom = Math.max(bottom, sp.getY() + sp.getH() + sp.getTotalHeight());
        }

        return bottom + 20;  // padding
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int x = (int) click.x();
        int y = (int) click.y();

        ChatUtil.info(x + " " + y);

        miscWidgets.forEach(w -> w.mouseClicked(
                x, y, click.button()
        ));

        // detectar clics sobre los marcos
        friendsPanel.mouseClicked(x, y, click.button());
        for (ModulePanel mp : new ArrayList<>(modulePanels)) {
            mp.mouseClicked(x, y, click.button());
        }
        for (SettingsPanel sp : new ArrayList<>(settingsPanels)) {
            sp.mouseClicked(x, y, click.button());
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        int x = (int) click.x();
        int y = (int) click.y();

        miscWidgets.forEach(w -> w.mouseReleased(
                x, y, click.button()));

        // registrar cuándo se suelta el clic, en cada marco respectivamente
        friendsPanel.mouseReleased(x, y, click.button());
        for (ModulePanel mp : new ArrayList<>(modulePanels)) {
            mp.mouseReleased(x, y, click.button());
        }
        for (SettingsPanel sp : new ArrayList<>(settingsPanels)) {
            sp.mouseReleased(x, y, click.button());
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent click, double deltaX, double deltaY) {
        miscWidgets.forEach(w -> w.mouseDragged(
                (int) click.x(),
                (int) click.y()));

        for (SettingsPanel sp : new ArrayList<>(settingsPanels))
            sp.mouseDragged((int) click.x(), (int) click.y());

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @EventListener
    public void onKey(KeyEvent event) {
        searchBar.onKey(event.getKey(), event.getAction());
    }

    @EventListener
    public void onMouseScroll(MouseScrollEvent event) {
        miscWidgets.forEach(w -> w.mouseScrolled(
                event.getVertical() * clientSettings.scrollSens.getValue()));
    }

    @EventListener
    public void onMouseMiddleButton(MouseClickEvent event) {
        // mover todos los marcos a un punto visible al presionar shift + la rueda del ratón
        if (Sputnik.mc.screen != this || event.getButton() != 2 || !KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) return;

        int h = Sputnik.mc.getWindow().getGuiScaledHeight();
        int minY = Math.min(modulePanels.stream().mapToInt(Panel::getY).min().getAsInt(), settingsPanels.stream().mapToInt(Panel::getY).min().getAsInt());
        int maxY = Math.max(modulePanels.stream().mapToInt(mp -> mp.getY() + mp.getH()).max().getAsInt(), settingsPanels.stream().mapToInt(sp -> sp.getY() + sp.getH()).max().getAsInt());
        int correction = minY < 0 ? -minY + 4 : (maxY > h ? h - maxY - 4 : 0);

        if (correction != 0) {
            modulePanels.forEach(mp -> mp.setY(mp.getY() + correction));
            settingsPanels.forEach(sp -> sp.setY(sp.getY() + correction));
        }
    }

    public void filterSearchResults() {
        if (!clientSettings.searchBar.getValue()) return;
        String searchInput = searchBar.getSearchInput().trim();
        if (!clientSettings.matchCase.getValue()) searchInput = searchInput.toLowerCase();


        for (ModulePanel mp : modulePanels) {
            for (Button b : mp.getButtons()) {
                if (!(b instanceof ModuleButton mb)) continue;
                Module module = mb.getModule();

                if (searchInput.isEmpty()) {
                    continue;
                }

                String name = MiscUtil.removeAccentMarks(module.getName());
                String description = MiscUtil.removeAccentMarks(module.getDescription());
                String category = MiscUtil.removeAccentMarks(module.getCategory().toString());

                if (!clientSettings.matchCase.getValue()) {
                    name = name.toLowerCase();
                    description = description.toLowerCase();
                    category = category.toLowerCase();
                }

                module.setSearchMatch(
                        name.contains(searchInput)
                                || description.contains(searchInput)
                                || category.contains(searchInput));
            }
        }

        for (SettingsPanel sp : settingsPanels) {
            for (Button b : sp.getButtons()) {
                if (!(b instanceof SettingButton<?> sb)) continue;
                Setting<?> setting = sb.getSetting();

                if (searchInput.isEmpty()) {
                    setting.setSearchMatch(true);
                    continue;
                }

                String name = MiscUtil.removeAccentMarks(setting.getName());
                String description = MiscUtil.removeAccentMarks(setting.getDescription());
                String sgName = MiscUtil.removeAccentMarks(setting.getSg().getName());

                if (!clientSettings.matchCase.getValue()) {
                    name = name.toLowerCase();
                    description = description.toLowerCase();
                    sgName = sgName.toLowerCase();
                }

                setting.setSearchMatch(name.contains(searchInput) || description.contains(searchInput) || sgName.contains(searchInput));
            }
        }

        for (Button b : friendsPanel.getButtons()) {
            if (!(b instanceof FriendButton fb)) continue;
            Friend friend = fb.getFriend();

            if (searchInput.isEmpty()) {
                friend.setSearchMatch(true);
                continue;
            }

            String name = MiscUtil.removeAccentMarks(friend.getName());
            String uuid = MiscUtil.removeAccentMarks(friend.getUuid().toString());

            if (!clientSettings.matchCase.getValue()) {
                name = name.toLowerCase();
                uuid = uuid.toLowerCase();
            }

            friend.setSearchMatch(name.contains(searchInput) || uuid.contains(searchInput));
        }
    }

    public SettingsPanel getSettingsPanelOfModule(Module module) {
        Optional<SettingsPanel> panel = settingsPanels.stream()
                .filter(sp -> sp.getModule().equals(module))
                .findFirst();

        return panel.orElseGet(() ->
                new SettingsPanel(module, 0, 0, 100, 20)
        );
    }

    // abrir un marco donde se encuentran los ajustes del módulo deseado
    public void openSettingsPanel(Module module, int x, int y) {
        // asegurarse de que no se sale de la pantalla
        x = Math.clamp(x, 0, mc.getWindow().getGuiScaledWidth() - 80);

        SettingsPanel panel = new SettingsPanel(module, x, y, 100, 20);
        settingsPanels.add(panel);

        Sputnik.EVENT_BUS.post(new SettingsPanelEvent.Open(panel));
    }

    // abrir un marco de ajustes específicamente para ajustes de selección múltiple
    public void openListSettingsPanel(Module dummy, int x, int y) {
        if (isSettingsPanelOpen(dummy)) return;
        SettingsPanel panel = new SettingsPanel(dummy, x, y, 120, 18);
        settingsPanels.add(panel);
    }

    // cerrar el marco de ajustes
    public void closeSettingsPanel(Module module) {
        // porque java.util.ConcurrentModificationException o algo no sé es lo único que se me ha ocurrido hacer
        List<SettingsPanel> toRemove = new ArrayList<>();
        for (SettingsPanel sp : settingsPanels) {
            if ((sp instanceof ColorPickerPanel cpp && cpp.dummyModule == module)  // para los selectores de colores
                    || (!(sp instanceof ColorPickerPanel) && sp.getModule() == module)) { // lógica muy mierdas, lo sé, pero paso de hacerlo bien
                toRemove.add(sp);
                Sputnik.EVENT_BUS.post(new SettingsPanelEvent.Close(sp));
                unselect(sp);
            }
        }

        settingsPanels.removeAll(toRemove);
    }

    // verificar si un módulo tiene su marco de ajustes abierto
    public boolean isSettingsPanelOpen(Module module) {
        return settingsPanels.stream().anyMatch(
                sp -> sp.getModule().equals(module));
    }

    public boolean isColorPickerPanelOpen(ColorSetting setting) {
        for (SettingsPanel sp : getSettingsPanels()) {
            if (sp instanceof ColorPickerPanel cpp)
                if (cpp.getColorSetting().equals(setting)) return true;
        }
        return false;
    }

    public void openColorPickerPanel(Module module, ColorSetting colorSetting, int x, int y) {
        ColorPickerPanel panel = new ColorPickerPanel(module, colorSetting, x + 80, y + 5, 153, 20);
        if (isColorPickerPanelOpen(panel.getColorSetting())) {
            closeColorPickerPanel(panel.getColorSetting());
            return;
        }
        settingsPanels.add(panel);
    }

    public void closeColorPickerPanel(ColorSetting setting) {
        for (SettingsPanel sp : getSettingsPanels()) {
            if (sp instanceof ColorPickerPanel cpp && cpp.getColorSetting().equals(setting)) {
                settingsPanels.remove(cpp);
                break;
            }
        }
    }

    public boolean isFriendSettingsPanelOpen(Friend friend) {
        for (SettingsPanel sp : getSettingsPanels()) {
            if (sp instanceof FriendSettingsPanel fsp) {
                if (fsp.getFriend().getUuid().equals(friend.getUuid())) return true;
            }
        }
        return false;
    }

    public void openFriendSettingsPanel(Friend friend, int x, int y) {
        FriendSettingsPanel panel = new FriendSettingsPanel(friend, x + 80, y + 5, 153, 20);
        if (isFriendSettingsPanelOpen(friend)) {
            closeFriendSettingsPanel(friend);
            return;
        }
        settingsPanels.add(panel);
    }

    public void closeFriendSettingsPanel(Friend friend) {
        for (SettingsPanel sp : getSettingsPanels()) {
            if (sp instanceof FriendSettingsPanel fsp && fsp.getFriend().getUuid().equals(friend.getUuid())) {
                settingsPanels.remove(fsp);
                break;
            }
        }
    }

    public void refreshListButtons() {
        for (SettingsPanel panel : settingsPanels)
            for (Button button : panel.getButtons())
                if (button instanceof ListButton<?> lb)
                    lb.refreshDummy();
    }

    // métodos del súper

    @Override
    public void onClose() {  // evitar que al reabrir la interfaz sin previamente haber soltado el clic, se sigan arrastrando objetos
        modulePanels.forEach(mp -> mp.mouseReleased(0, 0, 0));
        settingsPanels.forEach(sp -> sp.mouseReleased(0, 0, 0));
        scrollBar.mouseReleased(0, 0, 0);
        searchBar.mouseReleased(0, 0, 0);

        unselect(selected);
        super.onClose();
    }


    public boolean isPauseScreen() {
        // no pausar el juego cuando se abre la interfaz
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return super.shouldCloseOnEsc() && !anyFocused;
    }

    @Override
    protected void extractBlurredBackground(@NonNull GuiGraphicsExtractor gui) {
        if (clientSettings.blur.getValue()) super.extractBlurredBackground(gui);
    }


    // getters y setters de widgets

    public List<ModulePanel> getModulePanels() {
        return modulePanels;
    }

    public ClientSettingsPanel getClientSettingsPanel() {
        return clientSettingsPanel;
    }

    public FriendsPanel getFriendsPanel() {
        return friendsPanel;
    }

    public List<Widget> getMiscWidgets() { return miscWidgets; }

    public List<SettingsPanel> getSettingsPanels() {
        return settingsPanels;
    }

    public SearchBarWidget getSearchBar() {
        return searchBar;
    }

    public void setAnyFocused(boolean anyFocused) {
        this.anyFocused = anyFocused;
    }


    // selección de widgets

    public @Nullable Widget getSelected() {
        return selected;
    }

    public void setSelected(@Nullable Widget widget) {
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
