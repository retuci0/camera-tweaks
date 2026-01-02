package me.retucio.camtweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.camtweaks.event.events.ChangeRotationEvent;
import me.retucio.camtweaks.module.ModuleManager;
import me.retucio.camtweaks.module.modules.camera.Freecam;
import me.retucio.camtweaks.module.modules.camera.Freelook;
import me.retucio.camtweaks.module.modules.camera.Rotations;
import me.retucio.camtweaks.module.modules.misc.AntiInvis;
import me.retucio.camtweaks.module.modules.render.Nametags;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;
import static me.retucio.camtweaks.CameraTweaks.mc;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract Text getName();
    @Shadow public abstract EntityType<?> getType();

    @Shadow private float yaw;
    @Shadow private float pitch;

    @Unique
    Freecam freecam;
    @Unique
    Freelook freelook;
    @Unique
    Nametags nametags;
    @Unique
    Rotations rotations;

    @SuppressWarnings("rawtypes")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(EntityType type, World world, CallbackInfo ci) {
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        freelook = ModuleManager.INSTANCE.getModuleByClass(Freelook.class);
        nametags = ModuleManager.INSTANCE.getModuleByClass(Nametags.class);
        rotations = ModuleManager.INSTANCE.getModuleByClass(Rotations.class);
    }


    // cámara libre & perspectiva libre

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if ((Object) this != mc.player) return;

        if (freecam.isEnabled()) {
            freecam.changeLookDirection(cursorDeltaX * 0.15, cursorDeltaY * 0.15);
            ci.cancel();

        } else if (freelook.isEnabled() && freelook.mode.is(Freelook.CameraMode.CAMERA)) {
            freelook.setYaw(freelook.getYaw() + (float) (cursorDeltaX * freelook.mouseSens.getFloatValue()));
            freelook.setPitch(freelook.getPitch() + (float) (cursorDeltaY * freelook.mouseSens.getFloatValue()));

            if (Math.abs(freelook.getPitch()) > 90) freelook.setPitch(freelook.getPitch() > 0 ? 90 : -90);
            ci.cancel();
        }
    }


    // nametags

    @SuppressWarnings("ConstantConditions")
    @ModifyReturnValue(method = "isCustomNameVisible", at = @At("RETURN"))
    private boolean renderEntityNametags(boolean original) {
        if (!nametags.isEnabled()) return original;
        if ((Object) this instanceof PersistentProjectileEntity p && p.isOnGround()) return false;
        if ((Object) this instanceof ItemEntity i && !nametags.items.isEnabled(i.getStack().getItem())) return false;
        return nametags.entities.isEnabled((this.getType()));
    }

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Text showProjectileDamage(Text original) {
        if (!nametags.isEnabled() || !nametags.showProjectileDamage.isEnabled()) return original;

        if ((Object) this instanceof PersistentProjectileEntity arrow) {  // aunque lo llame "arrow", también cubre flechas espectrales y tridentes
            String damage = nametags.getArrowDamage(arrow);
            if (!damage.equals("0")) return original.copy().append(Text.literal(" (" + damage + ")").formatted(Formatting.RED));
        }

        return original;
    }

    @SuppressWarnings("ConstantConditions")
    @ModifyReturnValue(method = "getCustomName", at = @At("RETURN"))
    private Text displayEntityOwner(Text original) {
        if (!nametags.isEnabled() || !nametags.petOwner.isEnabled()) return original;
        if ((Object) this instanceof TameableEntity entity && entity.getOwnerReference() != null) {
            if (original != null) return original.copy().append(" (de " + nametags.getOwnerName(entity.getOwnerReference()) + ")");
            else return Text.of(nametags.getOwnerName(entity.getOwnerReference()));
        }
        return original;
    }


    @SuppressWarnings("ConstantConditions")
    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Text showBabies(Text original) {
        if (!nametags.isEnabled() || !nametags.distinguishBabies.isEnabled()) return original;
        if ((Object) this instanceof LivingEntity entity && entity.isBaby()) return original.copy().append(" (baby)");
        return original;
    }


    // rotaciones

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    private void onRotation(float yaw, float pitch, CallbackInfo ci) {
        if ((Object) this != mc.player) return;
        ChangeRotationEvent event = EVENT_BUS.post(new ChangeRotationEvent(yaw, pitch));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "setYaw", at = @At("HEAD"), cancellable = true)
    private void onChangeYaw(float yaw, CallbackInfo ci) {
        if ((Object) this != mc.player) return;
        ChangeRotationEvent event = EVENT_BUS.post(new ChangeRotationEvent(yaw, this.pitch));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "setPitch", at = @At("HEAD"), cancellable = true)
    private void onChangePitch(float pitch, CallbackInfo ci) {
        if ((Object) this != mc.player) return;
        ChangeRotationEvent event = EVENT_BUS.post(new ChangeRotationEvent(this.yaw, pitch));
        if (event.isCancelled()) ci.cancel();
    }


    // otros

    @Inject(method = "isInvisibleTo", at = @At("RETURN"), cancellable = true)
    public void renderInvisPlayers(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.INSTANCE.getModuleByClass(AntiInvis.class).isEnabled()) cir.setReturnValue(false);
    }
}