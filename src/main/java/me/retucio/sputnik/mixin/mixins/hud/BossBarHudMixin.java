package me.retucio.sputnik.mixin.mixins.hud;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.render.RenderBossbarEvent;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Iterator;


@Mixin(value = BossHealthOverlay.class)
public abstract class BossBarHudMixin {

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    private Iterator<LerpingBossEvent> modifyBossBarIterator(Iterator<LerpingBossEvent> original) {
        RenderBossbarEvent.BossIterator event = Sputnik.EVENT_BUS.post(new RenderBossbarEvent.BossIterator(original));
        return event.getIterator();
    }

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LerpingBossEvent;getName()Lnet/minecraft/network/chat/Component;"))
    private Component modifyBossBarName(Component original, @Local(name = "event") LerpingBossEvent clientBossBar) {
        RenderBossbarEvent.BossText event = Sputnik.EVENT_BUS.post(new RenderBossbarEvent.BossText(clientBossBar, (MutableComponent) original));
        return event.getName();
    }

    // require = 0 para compat. con meteor
    @ModifyConstant(method = "extractRenderState", constant = @Constant(intValue = 9, ordinal = 1), require = 0)
    private int modifySpacingConstant(int constant) {
        RenderBossbarEvent.BossSpacing event = Sputnik.EVENT_BUS.post(new RenderBossbarEvent.BossSpacing(constant));
        return event.getSpacing();
    }
}
