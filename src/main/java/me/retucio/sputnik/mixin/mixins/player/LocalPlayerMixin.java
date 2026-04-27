package me.retucio.sputnik.mixin.mixins.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.world.NoMiningInterruptions;
import me.retucio.sputnik.module.modules.inventory.PortalGUI;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


import static me.retucio.sputnik.Sputnik.mc;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Shadow
    private static HitResult filterHitResult(HitResult hitResult, Vec3 from, double maxRange) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @ModifyExpressionValue(method = "handlePortalTransitionEffect", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", opcode = Opcodes.GETFIELD))
    private Screen allowScreensInPortals(Screen original) {
        if (ModuleManager.INSTANCE.getModuleByClass(PortalGUI.class).isEnabled()) return null;
        return original;
    }

    // no va??
    @ModifyReturnValue(method = "pick", at = @At("RETURN"))
    private static HitResult onUpdateTargetedEntity(HitResult original, @Local(name = "blockHitResult") HitResult blockHitResult, @Local(name = "from") Vec3 from) {
        NoMiningInterruptions nmi = ModuleManager.INSTANCE.getModuleByClass(NoMiningInterruptions.class);

        if (!(original instanceof EntityHitResult ehr)) return original;
        if (!nmi.shouldIgnoreEntity((ehr.getEntity()))) return original;

        return filterHitResult(blockHitResult, from, mc.player.entityInteractionRange());
    }
}