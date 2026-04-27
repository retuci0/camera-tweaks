package me.retucio.sputnik.util.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public record ProjectileInfo (
        double gravity, double drag, Vec3 initialVelocity, Vec3 offset, Vec3 position,
        double waterDrag, double underwaterGravity, List<Integer> order
) {

    private static final List<Integer> ORDER_MDG = List.of(0, 1, 2);  // move-drag-gravity
    private static final List<Integer> ORDER_GMD = List.of(2, 0, 1);  // gravity-move-drag
    private static final List<Integer> ORDER_GDM = List.of(2, 1, 0);  // gravity-drag-move

    private static final Minecraft mc = Minecraft.getInstance();

    public ProjectileInfo(double gravity, double drag, Vec3 initialVelocity, Vec3 offset,
                          Vec3 position, double waterDrag, List<Integer> order) {
        this(gravity, drag, initialVelocity, offset, position, waterDrag, gravity, order);
    }

    public static List<ProjectileInfo> getItemsInfo(ItemStack itemStack) {
        float tickProgress = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 position = mc.player.getEyePosition(tickProgress).add(0, -0.1, 0);
        Item item = itemStack.getItem();

        if (item instanceof BowItem) {
            return getBowProjectileInfo(tickProgress, position);
        } else if (item instanceof CrossbowItem) {
            return getCrossbowProjectileInfo(tickProgress, itemStack, position);
        } else if (item instanceof TridentItem) {
            return getTridentProjectileInfo(tickProgress, itemStack, position);
        } else if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderpearlItem) {
            return getThrowableProjectileInfo(tickProgress, position);
        } else if (item instanceof WindChargeItem) {
            return getWindChargeProjectileInfo(tickProgress, position);
        } else if (item instanceof ThrowablePotionItem) {
            return getPotionProjectileInfo(position);
        } else if (item instanceof ExperienceBottleItem) {
            return getExperienceBottleProjectileInfo(position);
        } else if (item instanceof FishingRodItem && mc.player.fishing == null) {
            return getFishingRodProjectileInfo(tickProgress);
        }

        return new ArrayList<>();
    }

    private static List<ProjectileInfo> getBowProjectileInfo(float tickProgress, Vec3 position) {
        List<ProjectileInfo> list = new ArrayList<>();
        int useTicks = mc.player.getTicksUsingItem();
        float pull = BowItem.getPowerForTime(useTicks);

        if (pull >= 0.1) {
            Vec3 velocity = mc.player.getViewVector(tickProgress).scale(3 * pull);
            Vec3 offset = new Vec3(0.2, -0.06, 0.2);
            list.add(new ProjectileInfo(0.05, 0.99, velocity, offset, position, 0.6, ORDER_MDG));
        }
        return list;
    }

    private static List<ProjectileInfo> getCrossbowProjectileInfo(float tickProgress, ItemStack itemStack, Vec3 position) {
        List<ProjectileInfo> list = new ArrayList<>();
        double gravity = 0.05;
        double drag = 0.99;
        double waterDrag = 0.6;

        Vec3 velocity = mc.player.getViewVector(tickProgress).scale(3.15);
        Vec3 offset = new Vec3(0, -0.06, 0.03);

        ChargedProjectiles projectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
        if (projectiles != null) {
            for (ItemStackTemplate projectile : projectiles.items()) {
                if (projectile.is(Items.FIREWORK_ROCKET)) {
                    velocity = mc.player.getViewVector(tickProgress).scale(1.6f);
                    gravity = 0;
                    waterDrag = drag;
                }
            }
        }

        if (CrossbowItem.isCharged(itemStack)) {
            list.add(new ProjectileInfo(gravity, drag, velocity, offset, position, waterDrag, ORDER_MDG));

            if (hasEnchantment(itemStack, Enchantments.MULTISHOT)) {
                float angle = 10f;
                Vec3 vel1 = velocity.yRot((float) Math.toRadians(angle));
                Vec3 vel2 = velocity.yRot((float) Math.toRadians(-angle));
                list.add(new ProjectileInfo(gravity, drag, vel1, offset, position, waterDrag, ORDER_MDG));
                list.add(new ProjectileInfo(gravity, drag, vel2, offset, position, waterDrag, ORDER_MDG));
            }
        }
        return list;
    }

    private static List<ProjectileInfo> getTridentProjectileInfo(float tickProgress, ItemStack itemStack, Vec3 position) {
        List<ProjectileInfo> list = new ArrayList<>();
        int useTicks = mc.player.getTicksUsingItem();

        if (useTicks >= TridentItem.THROW_THRESHOLD_TIME && !hasEnchantment(itemStack, Enchantments.RIPTIDE)) {
            Vec3 velocity = mc.player.getViewVector(tickProgress).scale(TridentItem.PROJECTILE_SHOOT_POWER);
            Vec3 offset = new Vec3(0.2, 0.1, 0.2);
            list.add(new ProjectileInfo(0.05, 0.99, velocity, offset, position, 0.99, ORDER_MDG));
        }
        return list;
    }

    private static List<ProjectileInfo> getThrowableProjectileInfo(float tickProgress, Vec3 position) {
        Vec3 velocity = mc.player.getViewVector(tickProgress).scale(SnowballItem.PROJECTILE_SHOOT_POWER);
        Vec3 offset = new Vec3(0.2, -0.06, 0.2);

        return List.of(new ProjectileInfo(0.03, 0.99, velocity, offset, position, 0.8, ORDER_GDM));
    }

    private static List<ProjectileInfo> getWindChargeProjectileInfo(float tickProgress, Vec3 position) {
        Vec3 velocity = mc.player.getViewVector(tickProgress).scale(1.5f);
        Vec3 offset = new Vec3(0.2, -0.06, 0.2);

        return List.of(new ProjectileInfo(0, 1, velocity, offset, position, 1, ORDER_MDG));
    }

    private static List<ProjectileInfo> getPotionProjectileInfo(Vec3 position) {
        Vec3 direction = angleFromRotation(mc.player.getYRot(), mc.player.getXRot());
        Vec3 velocity = direction.scale(ThrowablePotionItem.PROJECTILE_SHOOT_POWER);
        Vec3 offset = new Vec3(0.2, -0.06, 0.2);

        return List.of(new ProjectileInfo(0.05, 0.99, velocity, offset, position, 0.8, ORDER_GDM));
    }

    private static List<ProjectileInfo> getExperienceBottleProjectileInfo(Vec3 position) {
        Vec3 direction = angleFromRotation(mc.player.getYRot(), mc.player.getXRot()).normalize();
        Vec3 velocity = direction.scale(0.7);
        Vec3 offset = new Vec3(0.2, -0.06, 0.2);

        return List.of(new ProjectileInfo(0.07, 0.99, velocity, offset, position, 0.8, ORDER_GDM));
    }

    private static List<ProjectileInfo> getFishingRodProjectileInfo(float tickProgress) {
        Vec3 cameraPos = mc.player.getEyePosition(tickProgress);

        float yawRad = (float) Math.toRadians(mc.player.getYRot());
        float pitchRad = (float) Math.toRadians(mc.player.getXRot());

        Vec3 position = new Vec3(
                cameraPos.x - Mth.sin(-yawRad) * 0.3,
                cameraPos.y,
                cameraPos.z - Mth.cos(-yawRad) * 0.3
        );

        float h = Mth.cos(-yawRad - Mth.PI);
        float i = Mth.sin(-yawRad - Mth.PI);
        float j = -Mth.cos(-pitchRad);
        float k = Mth.sin(-pitchRad);

        Vec3 velocity = new Vec3(-i, Mth.clamp(-(k / j), -5f, 5f), -h);
        double length = velocity.length();
        velocity = velocity.scale(0.6 / length + 0.5);
        Vec3 offset = new Vec3(0.16, -0.06, 0.2);

        return List.of(new ProjectileInfo(0.03, 0.92, velocity, offset, position, 0.92, ORDER_GMD));
    }

    public static boolean hasEnchantment(ItemStack stack, ResourceKey<Enchantment> enchantment) {
        Registry<Enchantment> registry = mc.player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> entry = registry.getOrThrow(enchantment);
        return EnchantmentHelper.getItemEnchantmentLevel(entry, stack) > 0;
    }


    private static Vec3 angleFromRotation(float yaw, float pitch) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        float x = -Mth.sin(yawRad) * Mth.cos(pitchRad);
        float y = -Mth.sin(Math.toRadians(pitch - 20));
        float z =  Mth.cos(yawRad) * Mth.cos(pitchRad);

        return new Vec3(x, y, z).normalize();
    }
}