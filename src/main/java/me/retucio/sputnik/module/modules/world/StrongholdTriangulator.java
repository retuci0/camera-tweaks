package me.retucio.sputnik.module.modules.world;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.AddEntityEvent;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.util.ChatUtil;
import me.retucio.sputnik.util.EntityUtil;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// https://benwoodworth.com/minecraft/stronghold-calculator/index.js
public class StrongholdTriangulator extends Module {

    public StrongholdTriangulator() {
        super("triangulador de strongholds",
                "usa josecadas para calcular la posición de la stronghold más cercana",
                Category.WORLD);
    }

    private boolean firstEyeThrown;
    private boolean secondEyeThrown;

    private Vec3d a;
    private Vec3d b;
    private float alpha;
    private float beta;

    // usar un ejecutor retrasado para esperar a que el ojo de ender adquiera su ángulo
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onEnable() {
        super.onEnable();
        ChatUtil.info("para empezar, lanza un ojo de ender");
        ChatUtil.info("al lanzar los ojos, quédate quieto, o puede que la posición calculada salga errónea");
    }

    @Override
    public void onDisable() {
        reset();
        scheduler.shutdown();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (firstEyeThrown && secondEyeThrown) {
            StrongholdResult result = getIntersection();

            if (result != null) {
                if (result.errorMessage == null) {
                    ChatUtil.info(Text.literal("stronghold aproximadamente en: "
                            + Formatting.GREEN
                            + String.format("%.2f, %.2f", result.x, result.z)
                            + Formatting.RESET
                    ));
                } else {
                    ChatUtil.error(result.errorMessage);
                }
            } else {
                ChatUtil.error("upsi");
            }

            reset();
        }
    }

    @EventListener
    private void onAddEntity(AddEntityEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getStackInHand(Hand.MAIN_HAND).getItem() != Items.ENDER_EYE) return;

        if (event.getEntity() instanceof EyeOfEnderEntity eye) {
            Vec3d throwPos = mc.player.getEntityPos();
            int entityId = eye.getId();

            scheduler.schedule(() -> {
                // ejecutar en el hilo principal
                mc.execute(() -> {
                    EyeOfEnderEntity currentEye = findEyeEntity(entityId);
                    if (currentEye != null) {
                        Vec3d eyeTargetPos = eye.getEntityPos();

                        if (eyeTargetPos != null) {
                            float yaw = (float) EntityUtil.getYaw(eyeTargetPos);

                            if (!firstEyeThrown) {
                                a = throwPos;
                                alpha = yaw;
                                firstEyeThrown = true;
                                ChatUtil.info("primer ojo registrado: "
                                        + String.format("%.1f, %.1f (%.1f°)", a.x, a.z, alpha));
                                ChatUtil.info("ahora muévete unos 200 bloques en una dirección distinta a la del ojo para lanzar el segundo");
                                ChatUtil.info("cuanta más distancia te separes, más preciso será el resultado");
                            } else if (!secondEyeThrown) {
                                b = throwPos;
                                beta = yaw;
                                secondEyeThrown = true;
                                ChatUtil.info("segundo ojo registrado: " +
                                        String.format("%.1f, %.1f (%.1f°)", b.x, b.z, beta));
                            }
                        } else {
                            ChatUtil.error("upsi dupsi");
                        }
                    }
                });
            }, 1000, TimeUnit.MILLISECONDS);  // esperar un segundo, a que el ojo adquiera velocidad
        }
    }

    private EyeOfEnderEntity findEyeEntity(int entityId) {
        if (mc.world == null) return null;
        return (EyeOfEnderEntity) mc.world.getEntityById(entityId);
    }

    private StrongholdResult getIntersection() {
        if (a == null || b == null) return null;

        // obtener coordenadas y ángulos
        double x0 = a.x;
        double x1 = b.x;
        double z0 = a.z;
        double z1 = b.z;
        double yaw0 = alpha / 180 * Math.PI;
        double yaw1 = beta / 180 * Math.PI;

        // distancia
        double d0 = ((x1 - x0) + (z1 - z0) * Math.tan(yaw1)) / (Math.cos(yaw0) * Math.tan(yaw1) - Math.sin(yaw0));
        double d1 = (d0 * Math.cos(yaw0) - (z1 - z0)) / Math.cos(yaw1);

        // posición de la stronghold
        double x = x0 - d0 * Math.sin(yaw0);
        double z = z0 + d0 * Math.cos(yaw0);

        // posibles casos
        if (x0 == x1 && z0 == z1) {  // ambos ojos lanzados desde la misma pos.
            return new StrongholdResult(null, null, "pero muévete");
        } else if (Double.isNaN(d0)) {
            return new StrongholdResult(null, null, "los ojos forman líneas paralelas o coincidentes");
        } else if (d0 < 0 || d1 < 0) {
            return new StrongholdResult(null, null, "los ojos apuntan a strongholds distintas");
        } else {
            return new StrongholdResult(x, z, null);
        }
    }

    private void reset() {
        firstEyeThrown = false;
        secondEyeThrown = false;
        a = null;
        b = null;
    }

    public record StrongholdResult(Double x, Double z, String errorMessage) {}
}