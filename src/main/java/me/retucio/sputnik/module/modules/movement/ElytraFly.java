package me.retucio.sputnik.module.modules.movement;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;


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
        if (mc.player == null || !mc.player.isFallFlying() || !wearingElytra()) return;

        float yaw = mc.player.getYRot();
        int dx = 0, dy = 0, dz = 0;

        if (mc.options.keyRight.isDown()) dx++;
        if (mc.options.keyLeft.isDown()) dx--;

        if (mc.options.keyJump.isDown()) dy++;
        if (mc.options.keyShift.isDown()) dy--;

        if (mc.options.keyDown.isDown()) dz++;
        if (mc.options.keyUp.isDown()) dz--;

        double speed = this.speed.getValue();
        double sin = Math.sin(Math.toRadians(yaw));
        double cos = Math.cos(Math.toRadians(yaw));

        double x = speed * dz * sin;
        double y = speed * dy;
        double z = speed * dz * -cos;

        x += speed * dx * -cos;
        z += speed * dx * -sin;

        mc.player.setDeltaMovement(new Vec3(x, y, z));
    }

    private boolean wearingElytra() {
        ItemStack equippedStack = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        return equippedStack.getItem() == Items.ELYTRA;
    }
}
