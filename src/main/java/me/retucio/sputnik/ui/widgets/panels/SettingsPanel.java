package me.retucio.sputnik.ui.widgets.panels;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.sputnik.SettingsPanelEvent;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.*;
import me.retucio.sputnik.module.setting.settings.*;
import me.retucio.sputnik.ui.widgets.buttons.settings.*;
import me.retucio.sputnik.ui.screen.ClickGui;
import me.retucio.sputnik.ui.widgets.buttons.*;
import me.retucio.sputnik.ui.widgets.Panel;
import me.retucio.sputnik.util.Colors;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


// panel para los botones de los ajustes de cada módulo
public class SettingsPanel extends CloseablePanel<SettingButton<? extends Setting<?>>> {

    protected Module module;
    protected List<SettingGroup> settingGroups;
    public boolean drawHeader = true;

    public SettingsPanel(Module module, int x, int y, int w, int h) {
        super("ajustes de " + module.getName(), x, y, w, h);

        this.module = module;
        this.settingGroups = module.getSgs();

        addButtonsByGroups();

        visibleButtons = buttons.stream()
                .filter(sb -> sb.getSetting().isVisible())
                .toList();
    }

    @Override
    public void render(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        updateWidth();

        int centerX = x + w / 2;
        int centerY = renderY + h / 2;

        if (drawHeader) {
            int titleX = x + 2 * PADDING;
            int titleY = centerY - mc.font.lineHeight / 2;

            int headerColor = module.isEnabled()
                    ? Colors.enabledToggleButtonColor.getRGB()
                    : Colors.disabledToggleButtonColor.getRGB();

            int closeButtonColor = isCloseButtonHovered(mouseX, mouseY)
                    ? Color.RED.getRGB()
                    : -1;

            gui.fill(x, renderY, x + w, renderY + h, headerColor);
            gui.text(mc.font, title, titleX, titleY, -1, true);
            drawCloseButton(gui, mouseX, mouseY, closeButtonColor);
        }

        // filtrar botones visibles
        visibleButtons = buttons.stream()
                .filter(sb -> sb.getSetting().isVisible() && sb.getSetting().isSearchMatch())
                .toList();

        // descripción
        List<String> descLines = wrapDescription(module.getDescription(), w - 2 * PADDING);
        int lineSpacing = 2;

        int descHeight = descLines.size() * mc.font.lineHeight + (descLines.size() - 1) * lineSpacing;
        int descBoxTop = renderY + h + PADDING;
        int descBoxBottom = descBoxTop + descHeight + PADDING * 2;

        totalHeight = descHeight + 3 * PADDING;
        int currentY;

        // obtener valores de altura y tal
        for (SettingGroup group : settingGroups) {
            if (hasVisibleSettingsInGroup(group)) {
                totalHeight += h + PADDING;

                if (group.isExtended()) {
                    List<SettingButton<?>> groupButtons = getButtonsForGroup(group);
                    for (int j = 0; j < groupButtons.size(); j++) {
                        SettingButton<?> sb = groupButtons.get(j);
                        if (sb.getSetting().isVisible() && sb.getSetting().isSearchMatch()) {
                            if (j < groupButtons.size() - 1) {
                                totalHeight += (h - h / 4) + PADDING;
                            } else {
                                totalHeight += (h - h / 4);
                            }
                        }
                    }
                    totalHeight += PADDING;
                }
            }
        }

        // fondo del marco
        gui.fill(x, descBoxTop - PADDING + 1, x + w, descBoxTop + totalHeight, Colors.panelBgColor.getRGB());

        // fondo de la descripción
        gui.fill(x + PADDING, descBoxTop, x + w - PADDING, descBoxBottom, Colors.buttonColor.getRGB());

        // restablecer currentY
        currentY = descBoxBottom + PADDING;

        // dibujar botones y cabezales
        for (int i = 0; i < settingGroups.size(); i++) {
            SettingGroup group = settingGroups.get(i);
            if (hasVisibleSettingsInGroup(group)) {
                int groupHeaderColor = isGroupHeaderHovered(mouseX, mouseY, i)
                        ? Colors.buttonColor.brighter().getRGB()
                        : Colors.buttonColor.getRGB();

                gui.fill(x + PADDING, currentY, x + w - PADDING, currentY + h, groupHeaderColor);
                gui.text(mc.font, group.getName(), x + PADDING * 2, currentY + (h - mc.font.lineHeight) / 2, -1, true);
                gui.text(mc.font, group.isExtended() ? "-" : "+", x + w - 4 * PADDING, currentY + (h - mc.font.lineHeight) / 2, -1, true);

                currentY += h + PADDING;

                // ajustes de grupos extendidos
                if (group.isExtended()) {
                    List<SettingButton<?>> groupButtons = getButtonsForGroup(group);
                    for (int j = 0; j < groupButtons.size(); j++) {
                        SettingButton<?> sb = groupButtons.get(j);
                        if (sb.getSetting().isVisible() && sb.getSetting().isSearchMatch()) {
                            sb.setX(x + PADDING * 2);
                            sb.setY(currentY);
                            sb.setW(w - PADDING * 4);
                            sb.setH(h - h / 4);
                            sb.render(gui, mouseX, mouseY, delta);

                            if (j < groupButtons.size() - 1) {
                                currentY += (h - h / 4) + PADDING;
                            } else {
                                currentY += (h - h / 4);
                            }
                        }
                    }
                    currentY += PADDING;
                }
            }
        }

        // texto de la desc.
        int firstLineY = descBoxTop + ((descBoxBottom - descBoxTop) - descHeight) / 2;
        for (int i = 0; i < descLines.size(); i++) {
            String line = descLines.get(i);
            int lineX = centerX - mc.font.width(line) / 2;
            int lineY = firstLineY + i * (mc.font.lineHeight + lineSpacing);
            gui.text(mc.font, line, lineX, lineY, -1, true);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (isCloseButtonHovered(mouseX, mouseY)) {
            onClose();
            return;
        }

        if (isHovered(mouseX, mouseY) && ClickGui.INSTANCE.trySelect(this)) {
            if (button == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            } else if (button == 1) {
                onClose();
            }
        }

        for (int i = 0; i < settingGroups.size(); i++) {
            SettingGroup group = settingGroups.get(i);
            if (hasVisibleSettingsInGroup(group) && isGroupHeaderHovered(mouseX, mouseY, i)) {
                group.setExtended(!group.isExtended());
                updateVisibleButtonsForGroup(group);
                return;
            }
        }

        for (SettingButton<?> sb : visibleButtons)
            sb.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        if (button == 0) {
            dragging = false;

            if (isHovered(mouseX, mouseY)) {
                Sputnik.EVENT_BUS.post(new SettingsPanelEvent.Move(this));
            }
        }

        for (SettingButton<?> sb : visibleButtons)
            sb.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY) {
        for (SettingButton<?> sb : visibleButtons)
            sb.mouseDragged(mouseX, mouseY);
    }

    @Override
    public void updateWidth() {
        // asegurarse de que todos los botones caben en el marco, haciendo que la anchura se ajuste al texto más largo
        int maxWidth = mc.font.width(title);
        for (SettingButton<?> button : buttons) {
            String text = button.getSetting().getName();

            switch (button) {
                case SliderButton sliderButton -> text += ": " + sliderButton.df.format((sliderButton.getSetting()).getValue());
                case CycleButton<?> cycleButton -> text += ": " + cycleButton.getSetting().getValue();
                case BindButton bindButton -> text += ": " + bindButton.getSetting().getKeyName();
                case TextButton textButton -> text += ": " + textButton.getSetting().getValue();
                case ColorButton colorButton -> text += colorButton.getSetting().getTooltipText();
                default -> {}
            }

            int textWidth = mc.font.width(text);
            maxWidth = Math.max(maxWidth, textWidth);
        }

        for (SettingGroup group : settingGroups) {
            int groupTextWidth = mc.font.width(group.getName() + " +");
            maxWidth = Math.max(maxWidth, groupTextWidth);
        }

        this.w = maxWidth + 30;
    }

    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragX);
            y = (int) (mouseY - dragY);
        }
    }

    public int getGroupHeaderY(int groupIndex) {
        int currentY = renderY + h + PADDING;
        List<String> descLines = wrapDescription(module.getDescription(), w - 2 * PADDING);
        int descHeight = descLines.size() * mc.font.lineHeight + (descLines.size() - 1) * 2;
        currentY += descHeight + 3 * PADDING;  // descBoxBottom + padding

        // iterar hasta llegar al grupo deseado
        int currentIndex = 0;
        for (SettingGroup group : settingGroups) {
            if (hasVisibleSettingsInGroup(group)) {
                if (currentIndex == groupIndex) {
                    return currentY;
                }

                currentY += h + PADDING;

                if (group.isExtended()) {
                    List<SettingButton<?>> groupButtons = getButtonsForGroup(group);
                    for (int j = 0; j < groupButtons.size(); j++) {
                        SettingButton<?> sb = groupButtons.get(j);
                        if (sb.getSetting().isVisible() && sb.getSetting().isSearchMatch()) {
                            if (j < groupButtons.size() - 1) {
                                currentY += (h - h / 4) + PADDING;
                            } else {
                                currentY += (h - h / 4);
                            }
                        }
                    }
                    currentY += PADDING;
                }
                currentIndex++;
            }
        }  // fallback
        return currentY;
    }

    public boolean isGroupHeaderHovered(int mouseX, int mouseY, int groupIndex) {
        int headerY = getGroupHeaderY(groupIndex);

        return mouseX > x + PADDING && mouseX < x + w - PADDING &&
                mouseY > headerY && mouseY < headerY + h;
    }

    private void addButtonsByGroups() {
        int offset = h;

        for (SettingGroup group : settingGroups) {
            for (Setting<?> setting : group) {
                SettingButton<?> button = createSettingButton(setting, offset);
                if (button != null) {
                    addButton(button);
                    offset += button.getH() + PADDING;
                }
            }
        }
    }

    private SettingButton<?> createSettingButton(Setting<?> setting, int offset) {
        return switch (setting) {
            case BooleanSetting b -> new ToggleButton(b, this, offset);
            case NumberSetting n -> new SliderButton(n, this, offset);
            case EnumSetting<?> e -> new CycleButton<>(e, this, offset);
            case KeySetting k -> new BindButton(k, this, offset);
            case StringSetting s -> new TextButton(s, this, offset);
            case ListSetting<?> l -> new ListButton<>(l, this, offset);
            case ColorSetting c -> new ColorButton(c, this, offset);
            case OptionSetting<?> o -> new ChooseButton<>(o, this, offset);
            default -> null;
        };
    }

    protected boolean hasVisibleSettingsInGroup(SettingGroup group) {
        for (Setting<?> setting : group) {
            if (setting.isVisible()) {
                return true;
            }
        }
        return false;
    }

    private List<SettingButton<?>> getButtonsForGroup(SettingGroup group) {
        List<SettingButton<?>> groupButtons = new ArrayList<>();
        for (SettingButton<?> button : buttons) {
            for (Setting<?> setting : group) {
                if (setting == button.getSetting()) {
                    groupButtons.add(button);
                    break;
                }
            }
        }
        return groupButtons;
    }

    protected void updateVisibleButtonsForGroup(SettingGroup group) {
        visibleButtons = new ArrayList<>();
        for (SettingGroup sg : settingGroups) {
            if (sg.isExtended()) {
                visibleButtons.addAll(getButtonsForGroup(sg).stream()
                        .filter(sb -> sb.getSetting().isVisible() && sb.getSetting().isSearchMatch())
                        .toList());
            }
        }
    }

    private List<String> wrapDescription(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line.isEmpty() ? word : line + " " + word;
            if (mc.font.width(testLine) > maxWidth - 2 * PADDING) {
                if (!line.isEmpty()) lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    public void addButton(SettingButton<?> button) {
        buttons.add(button);
        updateWidth();  // actualizar anchura del frame tras cada iteración
    }

    protected boolean isCloseButtonHovered(double mouseX, double mouseY) {
        return mouseX > x + w - mc.font.width("×") - 3 * PADDING && mouseX < x + w - PADDING
                && mouseY > renderY + PADDING && mouseY < renderY + h - PADDING;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public List<SettingGroup> getSettingGroups() {
        return settingGroups;
    }

    public void setSettingGroups(List<SettingGroup> settingGroups) {
        this.settingGroups = settingGroups;
    }

    @Override
    public void onClose() {
        ClickGui.INSTANCE.closeSettingsPanel(this.module);
    }
}