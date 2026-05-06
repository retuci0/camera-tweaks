package me.retucio.sputnik.friend;

import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.event.sputnik.FriendEvent;
import me.retucio.sputnik.module.ModuleManager;
import me.retucio.sputnik.module.modules.misc.FakePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class FriendManager {

    public static FriendManager INSTANCE;

    private final Minecraft mc = Minecraft.getInstance();

    private final List<Friend> friends = new ArrayList<>();

    public void add(Friend friend) {
        friends.add(friend);
        Sputnik.EVENT_BUS.post(new FriendEvent.Add(friend));
    }

    public void add(UUID uuid) {
        Friend friend = new Friend(uuid);
        friend.setName(friend.getPlayer().getName().getString());
        add(friend);
    }

    public void remove(Friend friend) {
        friends.remove(friend);
        Sputnik.EVENT_BUS.post(new FriendEvent.Remove(friend));
    }

    public void remove(UUID uuid) {
        Friend friend = get(uuid);
        remove(friend);
    }

    public Friend.Status add(Entity entity) {
        if (!(entity instanceof Player) || entity == ModuleManager.INSTANCE.getModuleByClass(FakePlayer.class).getPlayer()) {
            return Friend.Status.NOT_PLAYER;
        }
        if (isFriend(entity)) return Friend.Status.ALREADY_BEFRIENDED;

        add(entity.getUUID());
        return Friend.Status.SUCCESS;
    }

    public Friend.Status remove(Entity entity) {
        if (!(entity instanceof Player)) return Friend.Status.NOT_PLAYER;
        if (friends.isEmpty()) return Friend.Status.NO_FRIENDS;
        if (!isFriend(entity)) return Friend.Status.NOT_BEFRIENDED;

        remove(entity.getUUID());
        return Friend.Status.SUCCESS;
    }

    public Friend get(UUID uuid) {
        for (Friend friend : friends) {
            if (friend.getUuid().equals(uuid)) {
                return friend;
            }
        }
        return null;
    }

    public Friend fromName(String name) {
        for (Friend friend : friends) {
            if (friend.getName().equals(name)) {
                return friend;
            }
        }
        return null;
    }

    public boolean isFriend(Entity entity) {
        return get(entity.getUUID()) != null;
    }

    public List<Friend> getFriends() {
        return new ArrayList<>(friends);
    }
}
