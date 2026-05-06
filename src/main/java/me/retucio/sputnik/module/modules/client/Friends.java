package me.retucio.sputnik.module.modules.client;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.input.MouseClickEvent;
import me.retucio.sputnik.event.interact.AttackEntityEvent;
import me.retucio.sputnik.friend.FriendManager;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.util.ChatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;


public class Friends extends Module {

    public BooleanSetting middleClick = sgGeneral.add(new BooleanSetting(
            "ruedita para añadir amigo",
            "pulsara la ruedita del ratón para (des)marcar a un jugador como amigo",
            true
    ));

    public BooleanSetting preventAttack = sgGeneral.add(new BooleanSetting(
            "prevenir ataques",
            "no dejar que les ataques manualmente",
            true
    ));

    public Friends() {
        super("amigos", "sistema de amigos para evitar atacar a ciertos jugadores con módulos de combate", Category.CLIENT);
    }

    @EventListener
    public void onMouseClick(MouseClickEvent event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE
                && event.getAction() == GLFW.GLFW_PRESS
                && middleClick.getValue())
        {
            Entity target = mc.crosshairPickEntity;
            if (target == null) return;

            if (FriendManager.INSTANCE.isFriend(target)) {
                FriendManager.INSTANCE.remove(target);
                ChatUtil.info(ChatFormatting.GREEN + target.getName().getString() + ChatFormatting.RESET + " ya no es tu amigo :(");
            } else {
                switch (FriendManager.INSTANCE.add(target)) {
                    case SUCCESS -> ChatUtil.info(ChatFormatting.GREEN + target.getName().getString() + ChatFormatting.RESET + " agregado como amigo :)");
                    case ALREADY_BEFRIENDED -> ChatUtil.warn(ChatFormatting.GREEN + target.getName().getString() + ChatFormatting.RESET + " ya es tu amigo");
                    case NOT_PLAYER -> ChatUtil.error("no se pudo añadir a " + ChatFormatting.GREEN + target.getName().getString() + ChatFormatting.RESET + " como amigo");
                }
            }
        }
    }

    @EventListener
    public void onAttack(AttackEntityEvent event) {
        if (FriendManager.INSTANCE.isFriend(event.getEntity())
                && preventAttack.getValue())
        {
            event.cancel();
        }
    }
}
