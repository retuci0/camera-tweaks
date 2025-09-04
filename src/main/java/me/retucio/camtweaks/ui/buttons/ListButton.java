package me.retucio.camtweaks.ui.buttons;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.ListSetting;
import me.retucio.camtweaks.ui.ClickGUI;
import me.retucio.camtweaks.ui.frames.SettingsFrame;
import me.retucio.camtweaks.util.Colors;
import me.retucio.camtweaks.util.KeyUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ListButton<T> extends SettingButton {

    private final ListSetting<T> setting;
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

        String label = setting.getName() + " (" + countEnabled() + "/" + setting.getOptions().size() + ")";
        ctx.drawText(parent.mc.textRenderer, label, x + 5, y + 3, -1, true);
    }

    @Override
    public void drawTooltip(DrawContext ctx, double mouseX, double mouseY) {
        if (isHovered(mouseX, mouseY))
            ctx.drawTooltip(Text.of(setting.getDescription() + "( " + countEnabled() + " de " + setting.getOptions().size() + ")"), (int) mouseX, (int) mouseY);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && ClickGUI.INSTANCE.trySelect(this)) {
            // click izquierdo / derecho: abrir marco
            if (button <= 1 && !KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                if (ClickGUI.INSTANCE.isSettingsFrameOpen(dummy)) {
                    ClickGUI.INSTANCE.closeSettingsFrame(dummy);
                    return;
                }

                rebuildDummy();
                ClickGUI.INSTANCE.openListSettingsFrame(dummy, parent.x + 40, parent.y + 40);
            // shift + clic derecho: restablecer valores
            } else if (button == 1 && KeyUtil.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
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

    public ListSetting<T> getSetting() {
        return setting;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        ClickGUI.INSTANCE.unselect(this);
    }
}
