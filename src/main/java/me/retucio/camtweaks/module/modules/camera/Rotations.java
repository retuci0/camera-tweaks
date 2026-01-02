package me.retucio.camtweaks.module.modules.camera;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.ChangeRotationEvent;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Rotations extends Module {

    public NumberSetting yaw = addSetting(new NumberSetting("guiñada", "eje vertical - giro horizontal (yaw)", 0, -180, 180, 1));
    public NumberSetting pitch = addSetting(new NumberSetting("cabeceo", "eje horizontal - giro vertical (pitch)", 0, -90, 90, 1));

    public BooleanSetting smooth = addSetting(new BooleanSetting("evitar movimiento", "cancela todo movimiento de la cámara", false));
    public BooleanSetting serverSide = addSetting(new BooleanSetting("serverside", "espamea paquetes de rotación al servidor", false));

    public Rotations() {
        super("rotaciones",
                "te permite forzar una rotación específica",
                Category.CAMERA);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        mc.player.setYaw(yaw.getFloatValue());
        mc.player.setPitch(pitch.getFloatValue());

        if (serverSide.isEnabled())
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                    yaw.getFloatValue(), pitch.getFloatValue(), mc.player.isOnGround(), mc.player.horizontalCollision));
    }

    @SubscribeEvent
    public void onChangeRotation(ChangeRotationEvent event) {
        if (smooth.isEnabled()
                && (event.getYaw() != yaw.getFloatValue() || event.getPitch() != pitch.getFloatValue())){
            event.cancel();
    }}

}
