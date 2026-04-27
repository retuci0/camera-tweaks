package me.retucio.sputnik.ui.widgets.buttons.settings;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.ui.widgets.buttons.SettingButton;
import me.retucio.sputnik.ui.screen.ClickGUI;
import me.retucio.sputnik.ui.widgets.frames.SettingsFrame;
import me.retucio.sputnik.util.Colors;
import me.retucio.sputnik.util.KeyUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.*;


// botón para los ajustes de selección múltiple
public class ListButton<T> extends SettingButton<ListSetting<T>> {

    private final Module dummy;

    private final Map<T, BooleanSetting> optionButtons = new HashMap<>();

    public ListButton(ListSetting<T> setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;

        // crear un módulo "dummy" (falso), que se le añade cada entrada en la lista como un BooleanSetting
        dummy = new Module(setting.getName(), setting.getDescription(), Category.CLIENT) {
            @Override public void onEnable() {}
            @Override public void onDisable() {}
        };

        // ocultar cosas innecesarias
        dummy.getSettings().get(0).visibility(false);
        dummy.getSettings().get(1).visibility(false);
        dummy.getSettings().get(2).visibility(false);
        dummy.shouldSaveSettings(false);
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        // fondo
        int bgColor = isHovered(mouseX, mouseY)
                ? Colors.buttonColor.brighter().getRGB()
                : Colors.buttonColor.getRGB();
        gui.fill(x, y, x + w, y + h, bgColor);

        // texto: nombre + número de opciones seleccionadas / total de opciones
        String label = setting.getName() + " (" + countEnabled() + "/" + setting.getOptions().size() + ")";
        gui.text(mc.font, label, x + 5, y + 3, -1, true);
    }

    @Override
    public void drawTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY))
            gui.setTooltipForNextFrame(Component.literal(
                    setting.getDescription()
                            + " (" + countEnabled() + " de "
                            + setting.getOptions().size() + ")"),
                    mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            // clic izquierdo / derecho: abrir marco
            if (button <= 1 && !KeyUtil.isShiftDown()) {
                if (ClickGUI.INSTANCE.isSettingsFrameOpen(dummy)) {
                    ClickGUI.INSTANCE.closeSettingsFrame(dummy);
                    return;
                }

                rebuildDummy();
                ClickGUI.INSTANCE.openListSettingsFrame(dummy, parent.getX() + 40, parent.getY() + 40);
            // shift + clic derecho: restablecer valores
            } else if (button == 1 && KeyUtil.isShiftDown()) {
                setting.reset();
                refreshDummy();
            }
        }
    }

    private void rebuildDummy() {
        dummy.getSettings().clear();
        dummy.getSgGeneral().getSettings().clear();
        optionButtons.clear();

        setting.getOptions().stream()
                .sorted(Comparator.comparing(setting::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .forEach(option -> {
                    String displayName = setting.getDisplayName(option);
                    BooleanSetting b = dummy.getSgGeneral().add(new BooleanSetting(
                            displayName,
                            "incluir " + displayName,
                            setting.isEnabled(option)
                    ));
                    b.onUpdate(value -> setting.setEnabled(option, value));
                    optionButtons.put(option, b);
                });
    }
    public void refreshDummy() {
        optionButtons.forEach((option, boolSetting) ->
                boolSetting.setValue(setting.isEnabled(option)));
    }

    private int countEnabled() {
        return (int) setting.getOptions().stream().filter(setting::isEnabled).count();
    }
}
