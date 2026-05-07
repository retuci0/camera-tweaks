package me.retucio.sputnik.module.modules.client;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.FakePlayer;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.*;
import me.retucio.sputnik.ui.hud.HudRenderer;
import me.retucio.sputnik.ui.widgets.misc.NotificationWidget;
import me.retucio.sputnik.util.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class Notifications extends Module {

    private final SettingGroup sgSound = addSg(new SettingGroup("sonido", false));
    private final SettingGroup sgPlayer = addSg(new SettingGroup("jugador en rango", true));
    private final SettingGroup sgDurability = addSg(new SettingGroup("baja durabilidad", true));


    /* ajustes generales */

    private final NumberSetting duration = sgGeneral.add(new NumberSetting(
            "duración",
            "duración de las notis en segundos",
            5, 0.1, 10, 0.1
    ));

    private final BooleanSetting popup = sgGeneral.add(new BooleanSetting(
            "pop-up",
            "mostrar pop-up de notificación",
            true
    ));


    /* ajustes del volumen */

    private final OptionSetting<SoundEvent> sound = sgSound.add(new OptionSetting<>(
            "sonido",
            "sonido a reproducir con las notis",
            Lists.soundList,
            SoundEvents.NOTE_BLOCK_BELL.value(),
            Lists.soundNames
    ));

    private final NumberSetting volume = sgSound.add(new NumberSetting(
            "volumen",
            "volumen del sonido",
            70, 0, 125, 1
    ));

    private final NumberSetting pitch = sgSound.add(new NumberSetting(
            "frecuencia",
            "altura del sonido",
            70, 0, 125, 1
    ));


    /* cuando un jugador entra en rango */

    private final BooleanSetting warnPlayerInRange = sgPlayer.add(new BooleanSetting(
            "jugador en rango",
            "notificar cuando un jugador entre en rango",
            true
    ));

    private final NumberSetting playerRange = sgPlayer.add(new NumberSetting(
            "rango de jugadores",
            "rango a tener en cuenta al notificar",
            12,
            1,
            32,
            1
    )).visibility(warnPlayerInRange::getValue);

    private final BooleanSetting ignoreFakePlayer = sgPlayer.add(new BooleanSetting(
            "ignorar jugador falso",
            "ignorar al muñeco de \"jugador falso\"",
            false
    ));


    /* cuando una herramienta tiene la durabilidad baja */

    private final BooleanSetting warnToolLowDurability = sgDurability.add(new BooleanSetting(
            "baja durabilidad",
            "notificar cuando la herramienta en uso tiene baja durabilidad",
            true
    ));

    private final ListSetting<Item> excludeItems = sgDurability.add(new ListSetting<>(
            "items a excluir",
            "no avisar de la baja durabilidad de estos",
            Lists.itemList,
            Lists.allFalse(Lists.itemList),
            Lists.itemNames
    ));

    private final NumberSetting limitPercentage = sgDurability.add(new NumberSetting(
            "porcentaje", "porcentaje de durabilidad restante a la que se te avisa",
            5, 1, 100, 1
    ));


    /* fields */

    private final List<Player> notifiedPlayers = new ArrayList<>();
    private final List<ItemStack> notifiedTools = new ArrayList<>();

    private float prevPercentage = -1;


    public Notifications() {
        super(
                "notificaciones",
                "te notifica cuando sucedan ciertos eventos",
                Category.CLIENT
        );
        duration.onUpdate(v -> NotificationWidget.duration = (int) (v * 1000));
    }

    @Override
    public void onDisable() {
        notifiedPlayers.clear();
        notifiedTools.clear();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        checkForPlayersInRange();
        checkForLowDurabilityTools();
    }

    // comprobar si algún jugador ha entrado en el rango
    private void checkForPlayersInRange() {
        if (!warnPlayerInRange.getValue()) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            if (player == ModuleManager.INSTANCE.getModuleByClass(FakePlayer.class).getPlayer()
                    && ignoreFakePlayer.getValue()) continue;

            float d = player.distanceTo(mc.player);
            if (d <= playerRange.getValue() * 16) {
                if (!notifiedPlayers.contains(player)) {
                    notifiedPlayers.add(player);
                    String title = ChatFormatting.GREEN + player.getDisplayName().getString() + ChatFormatting.RESET + " ha entrado en rango!";
                    String desc = player.getDisplayName().getString() + " está a " + ChatFormatting.GOLD + (int) d + ChatFormatting.RESET + " bloques de distancia.";
                    push(title, desc);
                }
            } else {
                notifiedPlayers.remove(player);
            }
        }
    }

    // comprobar si la herramienta en uso tiene la durabilidad por debajo del porcentaje seleccionado
    private void checkForLowDurabilityTools() {
        if (!warnToolLowDurability.getValue()) return;

        ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (notifiedTools.contains(stack) || excludeItems.isEnabled(stack.getItem())) return;

        float percentage = (1 - (float) stack.getDamageValue() / stack.getMaxDamage()) * 100;

        if (percentage <= limitPercentage.getValue() && percentage < prevPercentage) {
            String customName = stack.getCustomName() == null ? "" : " \"" + stack.getCustomName().getString() + "\"";
            String desc = ChatFormatting.AQUA + stack.getItemName().getString()
                    + ChatFormatting.GREEN + customName + ChatFormatting.RESET
                    + " a " + ChatFormatting.GOLD + ((int) percentage + "") + "%"
                    + ChatFormatting.RESET + " de durabilidad";
            push("baja durabilidad!", desc);
            notifiedTools.add(stack);
        }

        prevPercentage = percentage;
    }


    // enviar una notificación
    private void push(String title, String desc) {
        if (popup.getValue()) {
            HudRenderer.pushNotification(title, desc);
        }
        if (sound.getValue() != SoundEvents.EMPTY) {
            mc.level.playSound(mc.player, mc.player.blockPosition(),
                    sound.getValue(),
                    SoundSource.AMBIENT,
                    toExponential(volume),
                    toExponential(pitch)
            );
        }
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
