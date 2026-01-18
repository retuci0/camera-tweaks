package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.event.SubscribeEvent;
import me.retucio.sputnik.event.events.UseItemEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

// "pedido prestado" de: https://github.com/meanwhile131/elytra-infinite
public class InfiniteElytra extends Module {

    private FlyState state;
    private float pitch;
    private double lowestY;

    public NumberSetting pitchDown = sgGeneral.add(new NumberSetting(
            "cabeceo (abajo)",
            "cabeceo al ir hacia abajo",
            30,
            -90,
            90,
            1
    ));


    public NumberSetting pitchUp = sgGeneral.add(new NumberSetting(
            "cabeceo (arriba)",
            "cabeceo al ir hacia arriba",
            -48,
            -90,
            90,
            1
    ));

    public NumberSetting pitchDownSpeed = sgGeneral.add(new NumberSetting(
            "velocidad de cabeceo (abajo)",
            "velocidad del cabeceo al ir hacia abajo",
            0.5,
            0.01,
            5,
            0.1
    ));

    public NumberSetting pitchUpVelocity = sgGeneral.add(new NumberSetting(
            "velocidad de cabeceo (arriba)",
            "velocidad del cabeceo al ir hacia arriba",
            2,
            0.01,
            5,
            0.1
    ));

    public NumberSetting ticksCollisionLookAhead = sgGeneral.add(new NumberSetting(
            "precaución de colisión",
            "precaución de colisión en ticks",
            10,
            0,
            20,
            1
    ));


    public InfiniteElytra() {
        super("avioneta",
                "vuela infinitamente, aprovechándose el sistema defectuoso que hizo mojang (hace falta tirarse de unos 50 bloques de altura)",
                Category.MOVEMENT);
    }


    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || !mc.player.isGliding()) {
            state = FlyState.NOT_FLYING;
            return;
        }

        if (state == FlyState.NOT_FLYING) {
            state = FlyState.GLIDING_DOWN;
            pitch = pitchDown.getFloatValue();
        } else if (state == FlyState.PITCHING_DOWN) {
            pitch += Math.min(pitchDown.getFloatValue() - pitch, pitchDownSpeed.getFloatValue());
            boolean movingDownwards = mc.player.getVelocity().y <= 0 && mc.player.getY() > lowestY;

            if (pitch >= pitchDown.getFloatValue() || movingDownwards) {
                pitch = pitchDown.getFloatValue();
                state = FlyState.GLIDING_DOWN;
            }
        } else if (state == FlyState.GLIDING_DOWN) {
            boolean willCollide = willCollideWhileGliding(ticksCollisionLookAhead.getIntValue());

            if (willCollide || mc.player.getVelocity().horizontalLengthSquared() > Math.pow(pitchUpVelocity.getFloatValue(), 2)) {
                pitch = pitchUp.getFloatValue();
                state = FlyState.PITCHING_DOWN;
                lowestY = mc.player.getY();
            }
        }

        mc.player.setPitch(pitch);
    }

    @SubscribeEvent
    public void onUseItem(UseItemEvent event) {
        if (mc.player == null) return;
        if (mc.player.getStackInHand(event.getHand()).getItem() == Items.FIREWORK_ROCKET && !mc.player.isSpectator() && mc.player.isGliding()) {
            pitch = pitchUp.getFloatValue();
            mc.player.setPitch(pitch);
            state = FlyState.PITCHING_DOWN;
            lowestY = mc.player.getY();
        }
    }

    private boolean willCollideWhileGliding(int ticks) {
        Vec3d velocity = mc.player.getVelocity();
        Box boundingBox = mc.player.getBoundingBox();
        for (int i = 0; i < ticks; i++) {
            velocity = mc.player.calcGlidingVelocity(velocity);
            boundingBox = boundingBox.offset(velocity);
            if (!mc.world.isSpaceEmpty(null, boundingBox, true)) {
                return true;
            }
        }
        return false;
    }

    public enum FlyState {
        NOT_FLYING,
        GLIDING_DOWN,
        PITCHING_DOWN;
    }
}
