package me.retucio.sputnik.mixin.mixins.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.world.NoMiningInterruptions;
import me.retucio.sputnik.module.modules.inventory.PortalGUI;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static me.retucio.sputnik.Sputnik.mc;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Shadow
    private static HitResult checkCrosshairTargetRange(HitResult hitResult, Vec3d cameraPos, double range) {
        return null;
    }

    @ModifyExpressionValue(method = "tickNausea", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", opcode = Opcodes.GETFIELD))
    private Screen allowScreensInPortals(Screen original) {
        if (ModuleManager.INSTANCE.getModuleByClass(PortalGUI.class).isEnabled()) return null;
        return original;
    }

    // no va??
    @ModifyReturnValue(method = "getCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;", at = @At("RETURN"))
    private static HitResult onUpdateTargetedEntity(HitResult original, @Local HitResult hitResult,  @Local(ordinal = 0) Vec3d vec3d) {
        NoMiningInterruptions nmi = ModuleManager.INSTANCE.getModuleByClass(NoMiningInterruptions.class);

        if (!(original instanceof EntityHitResult ehr)) return original;
        if (!nmi.shouldIgnoreEntity((ehr.getEntity()))) return original;

        return checkCrosshairTargetRange(hitResult, vec3d, mc.player.getEntityInteractionRange());
    }
}