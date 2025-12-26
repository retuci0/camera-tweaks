package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.camtweaks.event.events.RenderBossbarEvent;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Iterator;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"))
    public Iterator<ClientBossBar> modifyBossBarIterator(Iterator<ClientBossBar> original) {
        RenderBossbarEvent.BossIterator event = EVENT_BUS.post(new RenderBossbarEvent.BossIterator(original));
        return event.getIterator();
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ClientBossBar;getName()Lnet/minecraft/text/Text;"))
    public Text modifyBossBarName(Text original, @Local ClientBossBar clientBossBar) {
        RenderBossbarEvent.BossText event = EVENT_BUS.post(new RenderBossbarEvent.BossText(clientBossBar, original));
        return event.getName();
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = 9, ordinal = 1))
    public int modifySpacingConstant(int constant) {
        RenderBossbarEvent.BossSpacing event = EVENT_BUS.post(new RenderBossbarEvent.BossSpacing(constant));
        return event.getSpacing();
    }
}
