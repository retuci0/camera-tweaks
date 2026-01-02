package me.retucio.camtweaks.module.modules.player;

import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.module.settings.OptionSetting;
import me.retucio.camtweaks.util.ChatUtil;
import me.retucio.camtweaks.util.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;


public class WarnLowDurability extends Module {

    public NumberSetting limitPercentage = addSetting(new NumberSetting("porcentaje", "porcentaje de durabilidad restante a la que se te avisa",
            5, 1, 100, 1));

    public BooleanSetting message = addSetting(new BooleanSetting("enviar mensaje", "enviar un mensaje para alertar al usuario", true));

    public BooleanSetting playSound = addSetting(new BooleanSetting("reproducir sonido", "reproducir un sonido para alertar al usuario", true));
    public OptionSetting<SoundEvent> sound = addSetting(new OptionSetting<>("sonido", "qué sonido reproducir",
            Lists.soundList, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), Lists.soundNames));
    public NumberSetting volume = addSetting(new NumberSetting("volumen", "volumen del sonido", 70, 0, 125, 1));
    public NumberSetting pitch = addSetting(new NumberSetting("frecuencia", "altura del sonido", 70, 0, 125, 1));

    private final List<ItemStack> warned = new ArrayList<>();
    private float prevPercentage = -1;

    public WarnLowDurability() {
        super("aviso de baja dur.",
                "te avisa cuando la herramienta que sostengas sobrepase un límite de durabilidad",
                Category.PLAYER);

        playSound.onUpdate(v -> {
            sound.setVisible(v);
            volume.setVisible(v);
            pitch.setVisible(v);
        });
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        ItemStack stack = mc.player.getStackInHand(Hand.MAIN_HAND);
        if (warned.contains(stack)) return;

        float percentage = (1 - (float) stack.getDamage() / stack.getMaxDamage()) * 100;

        if (percentage <= limitPercentage.getValue() && percentage < prevPercentage) {
            if (playSound.isEnabled())
                mc.world.playSound(mc.player, mc.player.getBlockPos(),
                        sound.getValue(),
                        SoundCategory.AMBIENT,
                        toExponential(volume),
                        toExponential(pitch)
                );

            if (message.isEnabled()) {
                String customName = stack.getCustomName() == null ? "" : " \"" + stack.getCustomName().getString() + "\"";
                Text text = Text.literal(Formatting.AQUA + stack.getItemName().getString()
                        + Formatting.GREEN + customName + Formatting.RESET
                        + " a " + Formatting.GOLD + ((int) percentage + "") + "%"
                        + Formatting.RESET + " de durabilidad");
                ChatUtil.warn(text);
            }

            warned.add(stack);
        }

        prevPercentage = percentage;
    }

    @Override
    public void onDisable() {
        warned.clear();
        super.onDisable();
    }

    // el sistema decibélico es un sistema logarítmico
    private float toExponential(NumberSetting setting) {
        double linear = setting.getValue();

        double normalized = linear / (setting.getMax() - setting.getMax() / 4);
        double exponential = Math.pow(normalized, 3);

        if (exponential < 0.01) exponential = 0;
        return (float) exponential;
    }
}
