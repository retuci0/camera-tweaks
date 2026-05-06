package me.retucio.sputnik.mixin.mixins.player;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.interact.ClipAtLedgeEvent;
import me.retucio.sputnik.friend.FriendManager;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.render.Nametags;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    private Component addHealthIndicator(Component original) {
        Nametags nametags = ModuleManager.INSTANCE.getModuleByClass(Nametags.class);
        if (!nametags.isEnabled() || !nametags.health.getValue()) return original;

        float health = getHealth();
        float absorption = getAbsorptionAmount();
        ChatFormatting color;

        if (health < 5) color = ChatFormatting.RED;
        else if (health < 10) color = ChatFormatting.YELLOW;
        else if (health < 15) color = ChatFormatting.GREEN;
        else color = ChatFormatting.DARK_GREEN;

        // me gustaría también añadir un modo en el que se renderizan los corazones directamente en la nametag, pero no sé cómo hacer para las imágenes :'(
        // ahora que lo pienso en la snapshot más reciente han metido iconos que se pueden usar en texto, y uno de ellos es un corazón :D
        String text = " [" + color +
                (nametags.healthMode.is(Nametags.HealthMode.HEARTS)
                        ? String.format("%.2f", health / 2)  // redondear a dos decimales
                        : (int) health) +
                (absorption > 0  // tener absorción en cuenta
                        ? ChatFormatting.RESET + " + " + ChatFormatting.GOLD +
                        (nametags.healthMode.is(Nametags.HealthMode.HEARTS)
                                ? String.format("%.2f", absorption / 2)
                                : Integer.toString((int) absorption))
                        : "") +
                ChatFormatting.RESET + "]";

        // mote de amigo
        Component name = FriendManager.INSTANCE.isFriend((Player) (Object) this) && nametags.friendNick.getValue()
                ? Component.literal(FriendManager.INSTANCE.get(uuid).getName())
                : original;

        return name.copy().append(text);
    }

    @Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
    private void clipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        if (!level().isClientSide()) return;
        ClipAtLedgeEvent event = Sputnik.EVENT_BUS.post(new ClipAtLedgeEvent(isCrouching()));
        cir.setReturnValue(event.isClipping());
    }
}