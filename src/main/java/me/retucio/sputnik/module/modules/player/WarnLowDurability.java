package me.retucio.sputnik.module.modules.player;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.module.setting.settings.OptionSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class WarnLowDurability extends Module {

    private final SettingGroup sgWarn = addSg(new SettingGroup("aviso", true));
    private final SettingGroup sgSound = addSg(new SettingGroup("sonido", true));

    private final NumberSetting limitPercentage = sgGeneral.add(new NumberSetting("porcentaje", "porcentaje de durabilidad restante a la que se te avisa",
            5, 1, 100, 1));

    private final BooleanSetting message = sgWarn.add(new BooleanSetting("enviar mensaje", "enviar un mensaje para alertar al usuario", true));
    private final BooleanSetting playSound = sgWarn.add(new BooleanSetting("reproducir sonido", "reproducir un sonido para alertar al usuario", true));

    private final OptionSetting<SoundEvent> sound = sgSound.add(new OptionSetting<>("sonido", "qué sonido reproducir",
            Lists.soundList, SoundEvents.NOTE_BLOCK_BELL.value(), Lists.soundNames));
    private final NumberSetting volume = sgSound.add(new NumberSetting("volumen", "volumen del sonido", 70, 0, 125, 1));
    private final NumberSetting pitch = sgSound.add(new NumberSetting("frecuencia", "altura del sonido", 70, 0, 125, 1));

    private final List<ItemStack> warned = new ArrayList<>();
    private float prevPercentage = -1;

    public WarnLowDurability() {
        super("aviso de baja dur.",
                "te avisa cuando la herramienta que sostengas sobrepase un límite de durabilidad",
                Category.PLAYER);

        playSound.onUpdate(v -> {
            sound.visibility(v);
            volume.visibility(v);
            pitch.visibility(v);
        });
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (warned.contains(stack)) return;

        float percentage = (1 - (float) stack.getDamageValue() / stack.getMaxDamage()) * 100;

        if (percentage <= limitPercentage.getValue() && percentage < prevPercentage) {
            if (playSound.getValue())
                mc.level.playSound(mc.player, mc.player.blockPosition(),
                        sound.getValue(),
                        SoundSource.AMBIENT,
                        toExponential(volume),
                        toExponential(pitch)
                );

            if (message.getValue()) {
                String customName = stack.getCustomName() == null ? "" : " \"" + stack.getCustomName().getString() + "\"";
                Component text = Component.literal(ChatFormatting.AQUA + stack.getItemName().getString()
                        + ChatFormatting.GREEN + customName + ChatFormatting.RESET
                        + " a " + ChatFormatting.GOLD + ((int) percentage + "") + "%"
                        + ChatFormatting.RESET + " de durabilidad");
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
