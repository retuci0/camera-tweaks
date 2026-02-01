package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;


public class ElytraFly extends Module {

    private final NumberSetting speed = sgGeneral.add(new NumberSetting(
            "velocidad",
            "mi madre es parcialmente vagabunda",
            1,
            0,
            5,
            0.1
    ));

    public ElytraFly() {
        super("elytras", "vuela con elytras", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || !mc.player.isGliding() || !wearingElytra()) return;

        float yaw = mc.player.getYaw();
        int dx = 0, dy = 0, dz = 0;

        if (mc.options.rightKey.isPressed()) dx++;
        if (mc.options.leftKey.isPressed()) dx--;

        if (mc.options.jumpKey.isPressed()) dy++;
        if (mc.options.sneakKey.isPressed()) dy--;

        if (mc.options.backKey.isPressed()) dz++;
        if (mc.options.forwardKey.isPressed()) dz--;

        double speed = this.speed.getValue();
        double sin = Math.sin(Math.toRadians(yaw));
        double cos = Math.cos(Math.toRadians(yaw));

        double x = speed * dz * sin;
        double y = speed * dy;
        double z = speed * dz * -cos;

        x += speed * dx * -cos;
        z += speed * dx * -sin;

        mc.player.setVelocity(new Vec3d(x, y, z));
    }

    private boolean wearingElytra() {
        ItemStack equippedStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        return equippedStack != null && equippedStack.getItem() == Items.ELYTRA;
    }
}
