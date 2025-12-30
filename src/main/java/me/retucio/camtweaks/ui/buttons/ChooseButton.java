package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.OptionSetting;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.ui.screen.ClickGUI;
import me.retucio.camtweaks.util.Colors;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static me.retucio.camtweaks.CameraTweaks.mc;

public class ChooseButton<T> extends SettingButton {

    private final OptionSetting<T> setting;
    private final Module dummy;
    private final Map<T, BooleanSetting> optionButtons = new HashMap<>();
    private T lastSelectedValue;

    public ChooseButton(OptionSetting<T> setting, SettingsFrame parent, int offset) {
        super(setting, parent, offset);
        this.setting = setting;
        this.lastSelectedValue = setting.getValue();

        // crear un módulo "dummy" (falso)
        dummy = new Module(setting.getName(), setting.getDescription()) {
            @Override public void onEnable() {}
            @Override public void onDisable() {}
        };

        // ocultar cosas innecesarias
        dummy.getBind().setVisible(false);
        dummy.getSettings().stream()
                .filter(s -> s.getName().equalsIgnoreCase("modo de tecla"))
                .findFirst()
                .ifPresent(s -> s.setVisible(false));
        dummy.getSettings().stream()
                .filter(s -> s.getName().equalsIgnoreCase("notificar"))
                .findFirst()
                .ifPresent(s -> s.setVisible(false));
        dummy.shouldSaveSettings(false);
    }

    @Override
    public void render(DrawContext ctx, double mouseX, double mouseY, float delta) {
        int bgColor = isHovered(mouseX, mouseY)
                ? Colors.buttonColor.brighter().getRGB()
                : Colors.buttonColor.getRGB();

        ctx.fill(x, y, x + w, y + h, bgColor);

        String label = setting.getName() + ": " + setting.getDisplayName(setting.getValue());
        ctx.drawText(parent.mc.textRenderer, label, x + 5, y + 3, -1, true);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            // click izquierdo / derecho: abrir marco
            if (button <= 1 && !mc.isShiftPressed()) {
                if (ClickGUI.INSTANCE.isSettingsFrameOpen(dummy)) {
                    ClickGUI.INSTANCE.closeSettingsFrame(dummy);
                    return;
                }

                rebuildDummy();
                ClickGUI.INSTANCE.openListSettingsFrame(dummy, parent.x + 40, parent.y + 40);
                // shift + clic derecho: restablecer valores
            } else if (button == 1 && mc.isShiftPressed()) {
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
                    BooleanSetting b = new BooleanSetting(
                            displayName,
                            "incluir " + displayName,
                            setting.is(option)
                    );

                    b.onUpdate(value -> {
                        if (value) {
                            // deseleccionar el resto de opciones
                            for (Map.Entry<T, BooleanSetting> entry : optionButtons.entrySet()) {
                                if (!entry.getKey().equals(option))
                                    entry.getValue().setEnabled(false);
                            }
                            setting.setValue(option);
                        } else {
                            if (setting.is(option))
                                b.setEnabled(true);
                        }
                    });

                    dummy.addSetting(b);
                    optionButtons.put(option, b);
                });
    }

    public void refreshDummy() {
        T currentValue = setting.getValue();
        for (Map.Entry<T, BooleanSetting> entry : optionButtons.entrySet()) {
            boolean shouldBeEnabled = entry.getKey().equals(currentValue);
            if (entry.getValue().isEnabled() != shouldBeEnabled) {
                entry.getValue().setEnabled(shouldBeEnabled);
            }
        }
    }

    public OptionSetting<T> getSetting() {
        return setting;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY) {}
}