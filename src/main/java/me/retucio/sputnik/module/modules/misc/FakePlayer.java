package me.retucio.sputnik.module.modules.misc;

import com.mojang.authlib.GameProfile;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.StringSetting;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

import java.util.UUID;


public class FakePlayer extends Module {

    private final StringSetting name = sgGeneral.add(new StringSetting("nombre", "qué nombre asignarle al jugador", "apio boy", 22));

    private RemotePlayer player = null;

    public FakePlayer() {
        super("jugador falso",
                "invoca una entidad de jugador falsa por motivos de testeo",
                Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.level == null) return;
        addPlayer(mc.player, name.getValue());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.level == null || player == null) return;
        removePlayer();
        super.onDisable();
    }

    // intento miserable de hacer que no colisione con jugadores
    public RemotePlayer addPlayer(Player playerToCopy, String dummyName) {
        player = new RemotePlayer(mc.level, new GameProfile(UUID.randomUUID(), dummyName)) {
            @Override public void playerTouch(@NonNull Player player) {}
        };

        player.restoreFrom(playerToCopy);
        player.setCustomNameVisible(true);
        player.noPhysics = true;
        player.horizontalCollision = false;
        player.verticalCollision = false;
        mc.level.addEntity(player);
        return player;
    }

    public void removePlayer() {
        player.setRemoved(Entity.RemovalReason.KILLED);
        player.onClientRemoval();
        player = null;
    }

    public RemotePlayer getPlayer() {
        return player;
    }
}
