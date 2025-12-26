package me.retucio.camtweaks.module.modules;

import com.mojang.authlib.GameProfile;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.StringSetting;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class FakePlayer extends Module {

    public StringSetting name = addSetting(new StringSetting("nombre", "qué nombre asignarle al jugador", "apio boy", 22));

    private OtherClientPlayerEntity player = null;

    public FakePlayer() {
        super("jugador falso", "invoca una entidad de jugador falsa por motivos de testeo");
    }

    @Override
    public void onEnable() {
        if (mc.world == null) return;
        addPlayer(mc.player, name.getValue());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.world == null || player == null) return;
        removePlayer();
        super.onDisable();
    }

    // intento miserable de hacer que no colisione con jugadores
    public OtherClientPlayerEntity addPlayer(PlayerEntity playerToCopy, String dummyName) {
        player = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), dummyName)) {
            @Override public void onPlayerCollision(PlayerEntity player) {}
        };

        player.copyFrom(playerToCopy);
        player.setCustomNameVisible(true);
        player.noClip = true;
        player.horizontalCollision = false;
        player.verticalCollision = false;
        mc.world.addEntity(player);
        return player;
    }

    public void removePlayer() {
        player.setRemoved(Entity.RemovalReason.KILLED);
        player.onRemoved();
        player = null;
    }
}
