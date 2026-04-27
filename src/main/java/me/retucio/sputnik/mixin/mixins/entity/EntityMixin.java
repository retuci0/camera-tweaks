package me.retucio.sputnik.mixin.mixins.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.render.ChangeRotationEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.camera.Freecam;
import me.retucio.sputnik.module.modules.camera.Freelook;
import me.retucio.sputnik.module.modules.camera.Rotations;
import me.retucio.sputnik.module.modules.misc.AntiInvis;
import me.retucio.sputnik.module.modules.movement.BoatFly;
import me.retucio.sputnik.module.modules.movement.Velocity;
import me.retucio.sputnik.module.modules.render.Nametags;
import me.retucio.sputnik.util.interfaces.IVec3;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class EntityMixin {

    @Unique boolean freecamDone;

    @Shadow public abstract Component getName();
    @Shadow public abstract EntityType<?> getType();

    @Shadow private float yRot;
    @Shadow private float xRot;

    @Shadow
    public abstract HitResult pick(double range, float a, boolean withLiquids);

    @Unique
    Freecam freecam;
    @Unique
    Freelook freelook;
    @Unique
    Nametags nametags;
    @Unique
    Rotations rotations;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void getModules(CallbackInfo ci) {
        freecam = ModuleManager.INSTANCE.getModuleByClass(Freecam.class);
        freelook = ModuleManager.INSTANCE.getModuleByClass(Freelook.class);
        nametags = ModuleManager.INSTANCE.getModuleByClass(Nametags.class);
        rotations = ModuleManager.INSTANCE.getModuleByClass(Rotations.class);
    }

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(double range, float a, boolean withLiquids, CallbackInfoReturnable<HitResult> cir) {
        Minecraft mc = Minecraft.getInstance();
        if ((freecam.isEnabled()) && mc.getCameraEntity() != null && !freecamDone) {
            cir.cancel();

            Entity cameraEntity = mc.getCameraEntity();
            Vector3d pos = freecam.getPos();
            Vector3d prevPos = freecam.getPrevPos();

            double x = cameraEntity.getX();
            double y = cameraEntity.getY();
            double z = cameraEntity.getZ();
            double lastX = cameraEntity.xo;
            double lastY = cameraEntity.yo;
            double lastZ = cameraEntity.zo;
            float yaw = cameraEntity.getYRot();
            float pitch = cameraEntity.getXRot();
            float lastYaw = cameraEntity.yRotO;
            float lastPitch = cameraEntity.xRotO;

            ((IVec3) cameraEntity.position()).sputnik$set(pos.x, pos.y - cameraEntity.getEyeHeight(cameraEntity.getPose()), pos.z);
            cameraEntity.xo = prevPos.x;
            cameraEntity.yo = prevPos.y - cameraEntity.getEyeHeight(cameraEntity.getPose());
            cameraEntity.zo = prevPos.z;
            cameraEntity.setYRot(freecam.getYaw());
            cameraEntity.setXRot(freecam.getPitch());
            cameraEntity.yRotO = freecam.getPrevYaw();
            cameraEntity.xRotO = freecam.getPrevPitch();

            freecamDone = true;
            pick(range, a, withLiquids);
            freecamDone = false;

            ((IVec3) cameraEntity.position()).sputnik$set(x, y, z);
            cameraEntity.xo = lastX;
            cameraEntity.yo = lastY;
            cameraEntity.zo = lastZ;
            cameraEntity.setYRot(yaw);
            cameraEntity.setXRot(pitch);
            cameraEntity.yRotO = lastYaw;
            cameraEntity.xRotO = lastPitch;
        }
    }


    // cámara libre & perspectiva libre

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void onChangeLookDirection(double xo, double yo, CallbackInfo ci) {
        if ((Object) this != Sputnik.mc.player) return;

        if (freecam.isEnabled()) {
            freecam.changeLookDirection(xo * 0.15, yo * 0.15);
            ci.cancel();

        } else if (freelook.isEnabled() && freelook.mode.is(Freelook.CameraMode.CAMERA)) {
            freelook.setYaw(freelook.getYaw() + (float) (xo * freelook.mouseSens.getFloatValue()));
            freelook.setPitch(freelook.getPitch() + (float) (yo * freelook.mouseSens.getFloatValue()));

            if (Math.abs(freelook.getPitch()) > 90) freelook.setPitch(freelook.getPitch() > 0 ? 90 : -90);
            ci.cancel();
        }
    }


    // nametags

    @SuppressWarnings("ConstantConditions")
    @ModifyReturnValue(method = "isCustomNameVisible", at = @At("RETURN"))
    private boolean renderEntityNametags(boolean original) {
        if (!nametags.isEnabled()) return original;
        if ((Object) this instanceof AbstractArrow p && p.onGround()) return false;
        if ((Object) this instanceof ItemEntity i && !nametags.items.isEnabled(i.getItem().getItem())) return false;
        return nametags.entities.isEnabled((this.getType()));
    }

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component showProjectileDamage(Component original) {
        if (!nametags.isEnabled() || !nametags.showProjectileDamage.getValue()) return original;

        if ((Object) this instanceof AbstractArrow arrow) {  // aunque se llame "arrow", también cubre flechas espectrales y tridentes
            String damage = nametags.getArrowDamage(arrow);
            if (!damage.equals("0")) return original.copy().append(Component.literal(" (" + damage + ")").withStyle(ChatFormatting.RED));
        }

        return original;
    }

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component showTntPrimeTime(Component original) {
        if (!nametags.isEnabled() || !nametags.tntPrime.getValue()) return original;
        if ((Object) this instanceof PrimedTnt tnt) {
            return Component.nullToEmpty(nametags.getTntPrimeTime(tnt));
        }
        return original;
    }

    @SuppressWarnings("ConstantConditions")
    @ModifyReturnValue(method = "getCustomName", at = @At("RETURN"))
    private Component displayEntityOwner(Component original) {
        if (!nametags.isEnabled() || !nametags.petOwner.getValue()) return original;
        if ((Object) this instanceof TamableAnimal entity && entity.getOwnerReference() != null) {
            if (original != null) return original.copy().append(" (de " + nametags.getOwnerName(entity.getOwnerReference()) + ")");
            else return Component.nullToEmpty(nametags.getOwnerName(entity.getOwnerReference()));
        }
        return original;
    }


    @SuppressWarnings("ConstantConditions")
    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component showBabies(Component original) {
        if (!nametags.isEnabled() || !nametags.distinguishBabies.getValue()) return original;
        if ((Object) this instanceof LivingEntity entity && entity.isBaby()) return original.copy().append(" (baby)");
        return original;
    }


    // rotaciones

    @Inject(method = "setRot", at = @At("HEAD"), cancellable = true)
    private void onRotation(float yaw, float pitch, CallbackInfo ci) {
        if ((Object) this != Sputnik.mc.player) return;
        ChangeRotationEvent event = Sputnik.EVENT_BUS.post(new ChangeRotationEvent(yaw, pitch));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "setYRot", at = @At("HEAD"), cancellable = true)
    private void onChangeYaw(float yaw, CallbackInfo ci) {
        if ((Object) this != Sputnik.mc.player) return;
        ChangeRotationEvent event = Sputnik.EVENT_BUS.post(new ChangeRotationEvent(yaw, this.xRot));
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "setXRot", at = @At("HEAD"), cancellable = true)
    private void onChangePitch(float pitch, CallbackInfo ci) {
        if ((Object) this != Sputnik.mc.player) return;
        ChangeRotationEvent event = Sputnik.EVENT_BUS.post(new ChangeRotationEvent(this.yRot, pitch));
        if (event.isCancelled()) ci.cancel();
    }


    // otros

    @Inject(method = "isInvisibleTo", at = @At("RETURN"), cancellable = true)
    private void renderInvisPlayers(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.INSTANCE.getModuleByClass(AntiInvis.class).isEnabled()) cir.setReturnValue(false);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void modifyBoatStepHeight(CallbackInfoReturnable<Float> cir) {
        BoatFly boatFly = ModuleManager.INSTANCE.getModuleByClass(BoatFly.class);
        if (((Object) this) instanceof AbstractBoat && boatFly.isEnabled()) {
            cir.setReturnValue(boatFly.stepHeight.getFloatValue());
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void onPushed(Entity entity, CallbackInfo ci) {
        Velocity velocity = ModuleManager.INSTANCE.getModuleByClass(Velocity.class);
        if (velocity.isEnabled() && !velocity.push.getValue() && (Object) this == Sputnik.mc.player) ci.cancel();
    }
}