package me.retucio.sputnik.module.modules.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.retucio.sputnik.Sputnik;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.Lists;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.EntityQueriable;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** continúa en:
 * @see me.retucio.sputnik.mixin.EntityMixin
 * @see me.retucio.sputnik.mixin.ItemEntityMixin
 * @see me.retucio.sputnik.mixin.LivingEntityRendererMixin
 * @see me.retucio.sputnik.mixin.PlayerEntityMixin
 */

public class Nametags extends Module {

    SettingGroup sgInfo = addSg(new SettingGroup("info. adicional", true));

    Map<EntityType<?>, Boolean> defaultEntities = Lists.allFalse(Lists.entityList);
    public final ListSetting<EntityType<?>> entities = sgGeneral.add(new ListSetting<>("entidades", "entidades cuyo nametag será visible",
            Lists.entityList, defaultEntities, Lists.entityNames));
    public final ListSetting<Item> items = sgGeneral.add(new ListSetting<>("items", "te permite elegir cuáles items tienen su nombre visible",
            Lists.itemList, Lists.allTrue(Lists.itemList), Lists.itemNames));

    public final BooleanSetting health = sgGeneral.add(new BooleanSetting("mostrar vida", "muestra la vida de una entidad en su nametag", true));
    public final EnumSetting<HealthMode> healthMode = sgGeneral.add(new EnumSetting<>("mostrar vida en", "de qué manera mostrar la vida", HealthMode.class, HealthMode.POINTS));

    public final BooleanSetting alwaysVisible = sgGeneral.add(new BooleanSetting("siempre mostrar nametags", "mostrar nametag cuando el jugador está agachado o es invisible", false));
    public final BooleanSetting showSelf = sgGeneral.add(new BooleanSetting("mostrar propio", "mostrar nametag propio", false));

    public final BooleanSetting countItems = sgInfo.add(new BooleanSetting("contar items", "muestra cuánto de un item hay en un stack dropeado", true));
    public final BooleanSetting showProjectileDamage = sgInfo.add(new BooleanSetting("daño del proyectil", "muestra cuánto daño hace un proyectil en su nametag", true));
    public final BooleanSetting distinguishBabies = sgInfo.add(new BooleanSetting("distinguir bebés", "cambia el nametag cuando una entidad está en su fase bebé", false));
    public final BooleanSetting petOwner = sgInfo.add(new BooleanSetting("mostrar dueño", "muestra el dueño de una mascota (no funciona en servers no premium)", false));
    public final BooleanSetting tntPrime = sgInfo.add(new BooleanSetting("temporizador de TNT", "muestra el tiempo restante para que un bloque de TNT se detone", true));

    private final Map<UUID, String> cache = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public Nametags() {
        super("nametags",
                "modifica la manera en la que se renderizan los nametags",
                Category.RENDER);

        // entidades activadas por defecto
        defaultEntities.replace(EntityType.PLAYER, true);
        defaultEntities.replace(EntityType.ITEM, true);
        defaultEntities.replace(EntityType.ARROW, true);
        defaultEntities.replace(EntityType.SPECTRAL_ARROW, true);
        defaultEntities.replace(EntityType.TRIDENT, true);
        defaultEntities.replace(EntityType.TNT, true);
        entities.setDefaultValue(defaultEntities);

        health.onUpdate(v -> healthMode.setVisible(v));

        entities.onUpdate(entities -> {
            items.setVisible(entities.get(EntityType.ITEM));

            boolean anyProjectile = entities.get(EntityType.TRIDENT)
                    || entities.get(EntityType.ARROW)
                    || entities.get(EntityType.SPECTRAL_ARROW);
            showProjectileDamage.setVisible(anyProjectile);

            tntPrime.setVisible(entities.get(EntityType.TNT));
        });
    }

    // literalmente cómo lo calcula Minecraft
    public String getArrowDamage(PersistentProjectileEntity arrow) {
        if (mc.player == null) return "0";

        double initialDamage = 2;
        double velocity = arrow.getVelocity().length();
        DamageSource damageSource = arrow.getDamageSources().arrow(arrow, arrow.getOwner());

        if (arrow.getWeaponStack() != null && arrow.getEntityWorld() instanceof ServerWorld world)
            initialDamage = EnchantmentHelper.getDamage(world, arrow.getWeaponStack(), mc.player, damageSource, (float) initialDamage);

        int finalDamage = MathHelper.ceil(MathHelper.clamp(velocity * initialDamage, 0, 2.147483647E9));

        long bonus = 0;
        if (arrow.isCritical()) bonus = finalDamage / 2 - 1;

        if (healthMode.is(HealthMode.HEARTS)) finalDamage /= 2;
        return finalDamage + (bonus > 0 ? " ~ " + (finalDamage + bonus) : "");
    }

    public String getTntPrimeTime(TntEntity tnt) {
        return String.format("%.2f\"", ((float) tnt.getFuse() / 20));
    }

    @SuppressWarnings("deprecation")
    public String getOwnerName(LazyEntityReference<LivingEntity> owner) {
        // si el dueño está en línea (de manera segura)
        if (mc.world instanceof EntityQueriable<?> queriableWorld) {
            LivingEntity ownerEntity = owner.resolve(queriableWorld, LivingEntity.class);
            if (ownerEntity instanceof PlayerEntity playerEntity) return playerEntity.getName().getString();
        }

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
                Sputnik.LOGGER.error(e);
                cache.put(uuid, "?");
            }
        });

        return "...";
    }

    public enum HealthMode {
        POINTS("hp (puntos de vida)"),
        HEARTS("corazones");

        private final String name;
        HealthMode(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

}