package me.retucio.camtweaks.module.modules;

import com.mojang.authlib.GameProfile;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.StringSetting;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;

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

        player = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), name.getValue()));
//        player.copyPositionAndRotation(mc.player);
        player.copyFrom(mc.player);

        mc.world.addEntity(player);

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.world == null || player == null) return;

        player.setRemoved(Entity.RemovalReason.KILLED);
        player.onRemoved();
        player = null;

        super.onDisable();
    }
}
