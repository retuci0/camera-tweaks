package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;


// al parecer DamageItemEvent no se lanza si estás en un server de Paper, o sea que ahora se usa onTick()
public class WarnLowDurability extends Module {

    public NumberSetting limitPercentage = addSetting(new NumberSetting("porcentaje", "porcentaje de durabilidad restante a la que se te avisa",
            5, 1, 100, 1));

    public BooleanSetting message = addSetting(new BooleanSetting("mensaje", "enviar un mensaje para alertar al usuario", true));
    public BooleanSetting sound = addSetting(new BooleanSetting("sonido", "reproducir un sonido para alertar al usuario", true));

    private final List<ItemStack> warned = new ArrayList<>();

    public WarnLowDurability() {
        super("aviso de baja dur.", "te avisa cuando la herramienta que sostengas sobrepase un límite de durabilidad");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        ItemStack stack = mc.player.getStackInHand(Hand.MAIN_HAND);
        if (warned.contains(stack)) return;

        float percentage = (1 - (float) stack.getDamage() / stack.getMaxDamage()) * 100;

        if (percentage <= limitPercentage.getValue()) {
            if (sound.isEnabled())
                mc.world.playSound(mc.player, mc.player.getBlockPos(),
                        SoundEvents.BLOCK_BELL_USE, SoundCategory.AMBIENT, 1, 1);

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
    }

    @Override
    public void onDisable() {
        warned.clear();
        super.onDisable();
    }
}
