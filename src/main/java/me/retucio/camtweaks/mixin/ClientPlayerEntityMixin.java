package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.world.NoMiningInterruptions;
import me.retucio.camtweaks.module.modules.player.PortalGUI;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @ModifyExpressionValue(method = "tickNausea", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", opcode = Opcodes.GETFIELD))
    private Screen allowScreensInPortals(Screen original) {
        if (ModuleManager.INSTANCE.getModuleByClass(PortalGUI.class).isEnabled()) return null;
        return original;
    }

    @ModifyReturnValue(method = "getCrosshairTarget(FLnet/minecraft/entity/Entity;)Lnet/minecraft/util/hit/HitResult;", at = @At("RETURN"))
    private static HitResult onGetCrosshairTarget(HitResult original, @Local HitResult hitResult) {
        NoMiningInterruptions nmi = ModuleManager.INSTANCE.getModuleByClass(NoMiningInterruptions.class);
        if (original instanceof EntityHitResult ehr) {
            if (nmi.shouldIgnoreEntity(ehr.getEntity())
                    && hitResult.getType() == HitResult.Type.BLOCK) {
                return hitResult;
            }
        }

        return original;
    }
}
