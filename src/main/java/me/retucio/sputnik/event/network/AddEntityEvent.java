package me.retucio.sputnik.event.network;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.world.ClientLevelMixin;
import net.minecraft.world.entity.Entity;


/**
 * @see ClientLevelMixin#onAddEntity
 */
public class AddEntityEvent extends Event {

    private final Entity entity;

    public AddEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
