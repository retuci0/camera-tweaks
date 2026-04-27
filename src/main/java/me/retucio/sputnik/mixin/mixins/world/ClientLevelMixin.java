package me.retucio.sputnik.mixin.mixins.world;

import me.retucio.sputnik.event.network.AddEntityEvent;
import me.retucio.sputnik.event.network.RemoveEntityEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.retucio.sputnik.Sputnik.EVENT_BUS;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Shadow @Nullable
    public abstract Entity getEntity(int id);

    @Inject(method = "addEntity", at = @At("HEAD"))
    private void onAddEntity(Entity entity, CallbackInfo ci) {
        if (entity != null) EVENT_BUS.post(new AddEntityEvent(entity));
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
        Entity entity = getEntity(entityId);
        if (entity != null) EVENT_BUS.post(new RemoveEntityEvent(entity));
    }
}
