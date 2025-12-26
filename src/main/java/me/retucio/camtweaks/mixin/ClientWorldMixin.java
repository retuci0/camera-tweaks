package me.retucio.camtweaks.mixin;

import me.retucio.camtweaks.event.events.AddEntityEvent;
import me.retucio.camtweaks.event.events.RemoveEntityEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.camtweaks.CameraTweaks.EVENT_BUS;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Shadow @Nullable
    public abstract Entity getEntityById(int id);

    @Inject(method = "addEntity", at = @At("HEAD"))
    private void onAddEntity(Entity entity, CallbackInfo ci) {
        if (entity != null) EVENT_BUS.post(new AddEntityEvent(entity));
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
        Entity entity = getEntityById(entityId);
        if (entity != null) EVENT_BUS.post(new RemoveEntityEvent(entity));
    }
}
