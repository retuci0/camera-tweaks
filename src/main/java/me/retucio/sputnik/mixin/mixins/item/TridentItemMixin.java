package me.retucio.sputnik.mixin.mixins.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.movement.TridentBoost;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;


@Mixin(TridentItem.class)
public abstract class TridentItemMixin {

    @ModifyArgs(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;push(DDD)V"))
    private void modifyVelocity(Args args) {
        TridentBoost tridentBoost = ModuleManager.INSTANCE.getModuleByClass(TridentBoost.class);
        if (!tridentBoost.isEnabled()) return;
        args.set(0, (double) args.get(0) * tridentBoost.multiplier.getValue());
        args.set(1, (double) args.get(1) * tridentBoost.multiplier.getValue());
        args.set(2, (double) args.get(2) * tridentBoost.multiplier.getValue());
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private boolean allowUseOutOfWaterPre(boolean original) {
        TridentBoost tridentBoost = ModuleManager.INSTANCE.getModuleByClass(TridentBoost.class);
        return (tridentBoost.outOfWater.getValue() && tridentBoost.isEnabled()) || original;
    }

    @ModifyExpressionValue(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private boolean allowUseOutOfWaterPost(boolean original) {
        TridentBoost tridentBoost = ModuleManager.INSTANCE.getModuleByClass(TridentBoost.class);
        return (tridentBoost.outOfWater.getValue() && tridentBoost.isEnabled()) || original;
    }
}
