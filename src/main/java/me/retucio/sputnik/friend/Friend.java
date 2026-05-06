package me.retucio.sputnik.friend;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class Friend {

    private static final Minecraft mc = Minecraft.getInstance();

    private String name;
    private final UUID uuid;

    private boolean searchMatch;

    public Friend(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public @Nullable Player getPlayer() {
        if (mc.level == null) return null;
        Entity entity = mc.level.getEntity(uuid);
        if (!(entity instanceof Player player)) return null;
        return player;
    }

    public boolean isSearchMatch() {
        return searchMatch;
    }

    public void setSearchMatch(boolean searchMatch) {
        this.searchMatch = searchMatch;
    }

    public enum Status {
        SUCCESS,
        ALREADY_BEFRIENDED,
        NOT_PLAYER,

        NOT_BEFRIENDED,
        NO_FRIENDS;  // :(
    }
}
