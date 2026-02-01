package me.retucio.sputnik.util.misc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public record ProjectileInfo (
        double gravity, double drag, Vec3d initialVelocity, Vec3d offset, Vec3d position,
        double waterDrag, double underwaterGravity, List<Integer> order
) {

    private static final List<Integer> ORDER_MDG = List.of(0, 1, 2);  // move-drag-gravity
    private static final List<Integer> ORDER_GMD = List.of(2, 0, 1);  // gravity-move-drag
    private static final List<Integer> ORDER_GDM = List.of(2, 1, 0);  // gravity-drag-move

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public ProjectileInfo(double gravity, double drag, Vec3d initialVelocity, Vec3d offset,
                          Vec3d position, double waterDrag, List<Integer> order) {
        this(gravity, drag, initialVelocity, offset, position, waterDrag, gravity, order);
    }

    public static List<ProjectileInfo> getItemsInfo(ItemStack itemStack) {
        float tickProgress = mc.getRenderTickCounter().getTickProgress(false);
        Vec3d position = mc.player.getCameraPosVec(tickProgress).add(0, -0.1, 0);
        Item item = itemStack.getItem();

        if (item instanceof BowItem) {
            return getBowProjectileInfo(tickProgress, position);
        } else if (item instanceof CrossbowItem) {
            return getCrossbowProjectileInfo(tickProgress, itemStack, position);
        } else if (item instanceof TridentItem) {
            return getTridentProjectileInfo(tickProgress, itemStack, position);
        } else if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem) {
            return getThrowableProjectileInfo(tickProgress, position);
        } else if (item instanceof WindChargeItem) {
            return getWindChargeProjectileInfo(tickProgress, position);
        } else if (item instanceof ThrowablePotionItem) {
            return getPotionProjectileInfo(position);
        } else if (item instanceof ExperienceBottleItem) {
            return getExperienceBottleProjectileInfo(position);
        } else if (item instanceof FishingRodItem && mc.player.fishHook == null) {
            return getFishingRodProjectileInfo(tickProgress);
        }

        return new ArrayList<>();
    }

    private static List<ProjectileInfo> getBowProjectileInfo(float tickProgress, Vec3d position) {
        List<ProjectileInfo> list = new ArrayList<>();
        int useTicks = mc.player.getItemUseTime();
        float pull = BowItem.getPullProgress(useTicks);

        if (pull >= 0.1) {
            Vec3d velocity = mc.player.getRotationVec(tickProgress).multiply(3 * pull);
            Vec3d offset = new Vec3d(0.2, -0.06, 0.2);
            list.add(new ProjectileInfo(0.05, 0.99, velocity, offset, position, 0.6, ORDER_MDG));
        }
        return list;
    }

    private static List<ProjectileInfo> getCrossbowProjectileInfo(float tickProgress, ItemStack itemStack, Vec3d position) {
        List<ProjectileInfo> list = new ArrayList<>();
        double gravity = 0.05;
        double drag = 0.99;
        double waterDrag = 0.6;

        Vec3d velocity = mc.player.getRotationVec(tickProgress).multiply(3.15);
        Vec3d offset = new Vec3d(0, -0.06, 0.03);

        ChargedProjectilesComponent projectiles = itemStack.get(DataComponentTypes.CHARGED_PROJECTILES);
        if (projectiles != null) {
            for (ItemStack projectile : projectiles.getProjectiles()) {
                if (projectile.isOf(Items.FIREWORK_ROCKET)) {
                    velocity = mc.player.getRotationVec(tickProgress).multiply(1.6f);
                    gravity = 0;
                    waterDrag = drag;
                }
            }
        }

        if (CrossbowItem.isCharged(itemStack)) {
            list.add(new ProjectileInfo(gravity, drag, velocity, offset, position, waterDrag, ORDER_MDG));

            if (hasEnchantment(itemStack, Enchantments.MULTISHOT)) {
                float angle = 10f;
                Vec3d vel1 = velocity.rotateY((float) Math.toRadians(angle));
                Vec3d vel2 = velocity.rotateY((float) Math.toRadians(-angle));
                list.add(new ProjectileInfo(gravity, drag, vel1, offset, position, waterDrag, ORDER_MDG));
                list.add(new ProjectileInfo(gravity, drag, vel2, offset, position, waterDrag, ORDER_MDG));
            }
        }
        return list;
    }

    private static List<ProjectileInfo> getTridentProjectileInfo(float tickProgress, ItemStack itemStack, Vec3d position) {
        List<ProjectileInfo> list = new ArrayList<>();
        int useTicks = mc.player.getItemUseTime();

        if (useTicks >= TridentItem.MIN_DRAW_DURATION && !hasEnchantment(itemStack, Enchantments.RIPTIDE)) {
            Vec3d velocity = mc.player.getRotationVec(tickProgress).multiply(TridentItem.THROW_SPEED);
            Vec3d offset = new Vec3d(0.2, 0.1, 0.2);
            list.add(new ProjectileInfo(0.05, 0.99, velocity, offset, position, 0.99, ORDER_MDG));
        }
        return list;
    }

    private static List<ProjectileInfo> getThrowableProjectileInfo(float tickProgress, Vec3d position) {
        Vec3d velocity = mc.player.getRotationVec(tickProgress).multiply(SnowballItem.POWER);
        Vec3d offset = new Vec3d(0.2, -0.06, 0.2);

        return List.of(new ProjectileInfo(0.03, 0.99, velocity, offset, position, 0.8, ORDER_GDM));
    }

    private static List<ProjectileInfo> getWindChargeProjectileInfo(float tickProgress, Vec3d position) {
        Vec3d velocity = mc.player.getRotationVec(tickProgress).multiply(1.5f);
        Vec3d offset = new Vec3d(0.2, -0.06, 0.2);

        return List.of(new ProjectileInfo(0, 1, velocity, offset, position, 1, ORDER_MDG));
    }

    private static List<ProjectileInfo> getPotionProjectileInfo(Vec3d position) {
        Vec3d direction = angleFromRotation(mc.player.getYaw(), mc.player.getPitch());
        Vec3d velocity = direction.multiply(ThrowablePotionItem.POWER);
        Vec3d offset = new Vec3d(0.2, -0.06, 0.2);

        return List.of(new ProjectileInfo(0.05, 0.99, velocity, offset, position, 0.8, ORDER_GDM));
    }

    private static List<ProjectileInfo> getExperienceBottleProjectileInfo(Vec3d position) {
        Vec3d direction = angleFromRotation(mc.player.getYaw(), mc.player.getPitch()).normalize();
        Vec3d velocity = direction.multiply(0.7);
        Vec3d offset = new Vec3d(0.2, -0.06, 0.2);

        return List.of(new ProjectileInfo(0.07, 0.99, velocity, offset, position, 0.8, ORDER_GDM));
    }

    private static List<ProjectileInfo> getFishingRodProjectileInfo(float tickProgress) {
        Vec3d cameraPos = mc.player.getCameraPosVec(tickProgress);

        float yawRad = (float) Math.toRadians(mc.player.getYaw());
        float pitchRad = (float) Math.toRadians(mc.player.getPitch());

        Vec3d position = new Vec3d(
                cameraPos.x - MathHelper.sin(-yawRad) * 0.3,
                cameraPos.y,
                cameraPos.z - MathHelper.cos(-yawRad) * 0.3
        );

        float h = MathHelper.cos(-yawRad - MathHelper.PI);
        float i = MathHelper.sin(-yawRad - MathHelper.PI);
        float j = -MathHelper.cos(-pitchRad);
        float k = MathHelper.sin(-pitchRad);

        Vec3d velocity = new Vec3d(-i, MathHelper.clamp(-(k / j), -5f, 5f), -h);
        double length = velocity.length();
        velocity = velocity.multiply(0.6 / length + 0.5);
        Vec3d offset = new Vec3d(0.16, -0.06, 0.2);

        return List.of(new ProjectileInfo(0.03, 0.92, velocity, offset, position, 0.92, ORDER_GMD));
    }

    public static boolean hasEnchantment(ItemStack stack, RegistryKey<Enchantment> enchantment) {
        Registry<Enchantment> registry = mc.player.getEntityWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> entry = registry.getOrThrow(enchantment);
        return EnchantmentHelper.getLevel(entry, stack) > 0;
    }

    private static Vec3d angleFromRotation(float yaw, float pitch) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        float x = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad);
        float y = -MathHelper.sin(Math.toRadians(pitch - 20));
        float z = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad);

        return new Vec3d(x, y, z).normalize();
    }
}