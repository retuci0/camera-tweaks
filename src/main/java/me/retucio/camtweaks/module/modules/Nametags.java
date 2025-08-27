package me.retucio.camtweaks.module.modules;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.util.ChatUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.EntityMixin
 * @see me.retucio.camtweaks.mixin.ItemEntityMixin
 * @see me.retucio.camtweaks.mixin.PlayerEntityMixin
 */

public class Nametags extends Module {

    public BooleanSetting namePlayers = addSetting(new BooleanSetting("nombrar jugadores", "renderiza el nametag de jugadores", true));
    public BooleanSetting health = addSetting(new BooleanSetting("mostrar vida", "muestra la vida de una entidad en su nametag", true));
    public EnumSetting<HealthMode> healthMode = addSetting(new EnumSetting<>("mostrar vida en", "de qué manera mostrar la vida", HealthMode.class, HealthMode.POINTS));
    public BooleanSetting alwaysVisible = addSetting(new BooleanSetting("siempre mostrar nametags", "mostrar nametags cuando no deberían de ser visibles", false));

    public BooleanSetting namePassiveMobs = addSetting(new BooleanSetting("nombrar mobs pasivos", "renderiza el nametag de mobs pasivos", false));
    public BooleanSetting nameHostileMobs = addSetting(new BooleanSetting("nombrar mobs hostiles", "renderiza el nametag de mobs hostiles", false));
    public BooleanSetting nameMiscMobs = addSetting(new BooleanSetting("nombrar mobs neutrales", "renderiza el nametag de mobs neutrales", false));
    public BooleanSetting distinguishBabies = addSetting(new BooleanSetting("distinguir bebés", "cambia el nametag cuando una entidad está en su fase bebé", false));

    public BooleanSetting nameProjectiles = addSetting(new BooleanSetting("nombrar proyectiles", "renderiza el nametag de proyectiles", false));
    public BooleanSetting showProjectileDamage = addSetting(new BooleanSetting("daño del proyectil", "muestra cuánto daño hace un proyectil en su nametag", false));

    public BooleanSetting nameItems = addSetting(new BooleanSetting("nombrar items", "renderiza el nametag de items", true));
    public BooleanSetting countItems = addSetting(new BooleanSetting("contar items", "muestra cuánto de un item hay en un stack dropeado", true));

    public BooleanSetting nameVehicles = addSetting(new BooleanSetting("nombrar vehículos", "renderiza el nametag de botes, minecarts, ...", false));
    public BooleanSetting showPassengers = addSetting(new BooleanSetting("mostrar pasajeros", "muestra qué pasajeros tiene el vehículo", false));

    public BooleanSetting petOwner = addSetting(new BooleanSetting("mostrar dueño", "muestra el dueño de una mascota (no funciona en servers no premium)", false));

    private final Map<UUID, String> cache = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public Nametags() {
        super("nametags", "modifica la manera en la que se renderizan los nametags");

        nameProjectiles.onUpdate(v -> showProjectileDamage.setVisible(v));
        nameItems.onUpdate(v -> countItems.setVisible(v));
        nameVehicles.onUpdate(v -> showPassengers.setVisible(v));

        namePassiveMobs.onUpdate(v -> distinguishBabies.setVisible(v || nameMiscMobs.isEnabled() || nameHostileMobs.isEnabled()));
        nameMiscMobs.onUpdate(v -> distinguishBabies.setVisible(v || namePassiveMobs.isEnabled() || nameHostileMobs.isEnabled()));
        nameHostileMobs.onUpdate(v -> distinguishBabies.setVisible(v || nameMiscMobs.isEnabled() || namePassiveMobs.isEnabled()));

        health.onUpdate(v -> healthMode.setVisible(v));
    }

    public enum HealthMode {
        POINTS("hp (puntos de vida)"),
        HEARTS("corazones");

        private final String name;
        HealthMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    // literalmente cómo lo calcula Minecraft
    public float getArrowDamage(PersistentProjectileEntity arrow) {
        if (mc.player == null) return 0;

        double initialDamage = 2;
        double velocity = arrow.getVelocity().length();
        DamageSource damageSource = arrow.getDamageSources().arrow(arrow, arrow.getOwner());

        if (arrow.getWeaponStack() != null)
            if (arrow.getWorld() instanceof ServerWorld world)
                initialDamage = EnchantmentHelper.getDamage(world, arrow.getWeaponStack(), mc.player, damageSource, (float) initialDamage);

        int finalDamage = MathHelper.ceil(MathHelper.clamp(velocity * initialDamage, 0, 2.147483647E9));

        if (arrow.isCritical()) {
            long bonus = arrow.getRandom().nextInt(finalDamage / 2 + 2);
            finalDamage = (int) Math.min(bonus + (long) finalDamage, 2147483647L);
        }

        return healthMode.is(HealthMode.HEARTS) ? finalDamage / 2f : finalDamage;
    }

    @SuppressWarnings("deprecation")
    public String getOwnerName(LazyEntityReference<LivingEntity> owner) {
        // si el dueño está en línea
        LivingEntity ownerEntity = owner.resolve(mc.world, LivingEntity.class);
        if (ownerEntity instanceof PlayerEntity playerEntity) return playerEntity.getName().getString();

        // mirar si ya está en la caché
        UUID uuid = owner.getUuid();
        String cachedName = cache.get(uuid);
        if (cachedName != null) return cachedName;

        cache.put(uuid, "..."); // mientras se obtiene el nombre

        // si no está en la caché, ni en línea, obtener nombre de los servers de Mojang
        executor.execute(() -> {
            try {
                String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "");
                HttpURLConnection req = (HttpURLConnection) new URL(url).openConnection();
                req.setRequestMethod("GET");
                req.setConnectTimeout(5000);
                req.setReadTimeout(5000);
                if (req.getResponseCode() != 200) {
                    cache.put(uuid, "?");
                    return;
                }

                JsonObject obj = JsonParser.parseReader(new InputStreamReader(req.getInputStream())).getAsJsonObject();
                String name = obj.get("name").getAsString();
                cache.put(uuid, name);

            } catch (Exception e) {
                e.printStackTrace();
                cache.put(uuid, "?");
            }
        });

        return "...";
    }
}