package me.retucio.camtweaks.ui.widgets.buttons.settings;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.ListSetting;
import me.retucio.camtweaks.ui.widgets.buttons.SettingButton;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.ui.widgets.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.*;


// botón para los ajustes de selección múltiple
public class ListButton<T> extends SettingButton<ListSetting<T>> {

    private final Module dummy;

    private final Map<T, BooleanSetting> optionButtons = new HashMap<>();

    public ListButton(ListSetting<T> setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;

        // crear un módulo "dummy" (falso), que se le añade cada entrada en la lista como un BooleanSetting
        dummy = new Module(setting.getName(), setting.getDescription()) {
            @Override public void onEnable() {}
            @Override public void onDisable() {}
        };

        // ocultar cosas innecesarias
        dummy.getSettings().get(0).setVisible(false);
        dummy.getSettings().get(1).setVisible(false);
        dummy.getSettings().get(2).setVisible(false);
        dummy.shouldSaveSettings(false);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // fondo
        int bgColor = isHovered(mouseX, mouseY)
                ? Colors.buttonColor.brighter().getRGB()
                : Colors.buttonColor.getRGB();
        ctx.fill(x, y, x + w, y + h, bgColor);

        // texto: nombre + número de opciones seleccionadas / total de opciones
        String label = setting.getName() + " (" + countEnabled() + "/" + setting.getOptions().size() + ")";
        ctx.drawText(mc.textRenderer, label, x + 5, y + 3, -1, true);
    }

    @Override
    public void drawTooltip(DrawContext ctx, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY))
            ctx.drawTooltip(Text.of(
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
        optionButtons.clear();

        setting.getOptions().stream()
                .sorted(Comparator.comparing(setting::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .forEach(option -> {
                    String displayName = setting.getDisplayName(option);
                    BooleanSetting b = dummy.addSetting(new BooleanSetting(
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
                boolSetting.setEnabled(setting.isEnabled(option)));
    }

    private int countEnabled() {
        return (int) setting.getOptions().stream().filter(setting::isEnabled).count();
    }
}
