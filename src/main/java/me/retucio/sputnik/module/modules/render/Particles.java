package me.retucio.sputnik.module.modules.render;

import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import me.retucio.sputnik.util.Lists;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Random;


public class Particles extends Module {

    private final SettingGroup sgDispersion = addSg(new SettingGroup("dispersión", true));
    private final SettingGroup sgOffset = addSg(new SettingGroup("desplazamiento", true));

    List<SimpleParticleType> particleList = Lists.particleList.stream()
            .filter(particleType -> particleType instanceof SimpleParticleType)
            .map(particleType -> (SimpleParticleType) particleType)
            .toList();

    Map<SimpleParticleType, String> particleNames = Lists.getMapOfLists(particleList,
            particleList.stream().map(particle -> Component.translatable(
                    BuiltInRegistries.PARTICLE_TYPE.getKey(particle).toShortLanguageKey()).getString())
                    .toList());

    private final ListSetting<SimpleParticleType> particles = sgGeneral.add(new ListSetting<>(
            "partículas",
            "partículas a generar",
            particleList,
            Lists.allFalse(particleList),
            particleNames
    ));

    private final NumberSetting spawnInterval = sgGeneral.add(new NumberSetting(
            "intervalo",
            "intervalo entre generaciones (en ticks)",
            4, 1, 100, 1
    ));

    private final NumberSetting particleCount = sgGeneral.add(new NumberSetting(
            "cantidad",
            "partículas por generación",
            10, 1, 100, 1
    ));


    private final NumberSetting velocity = sgDispersion.add(new NumberSetting(
            "velocidad",
            "multiplicador de la velocidad de dispersión",
            0.2, 0, 2, 0.05
    ));

    private final NumberSetting spreadX = sgDispersion.add(new NumberSetting(
            "dispersión X",
            "dispersión horizontal de posición",
            0.5, 0, 5, 0.1
    ));

    private final NumberSetting spreadY = sgDispersion.add(new NumberSetting(
            "dispersión Y",
            "dispersión vertical de posición",
            0.5, 0, 5, 0.1
    ));

    private final NumberSetting spreadZ = sgDispersion.add(new NumberSetting(
            "dispersión Z",
            "dispersión de profundidad de posición",
            0.5, 0, 5, 0.1
    ));

    private final NumberSetting velocityRandomness = sgDispersion.add(new NumberSetting(
            "aleatoriedad velocidad",
            "variación aleatoria en la velocidad",
            0.3, 0, 1, 0.05
    ));

    private final NumberSetting positionRandomness = sgDispersion.add(new NumberSetting(
            "aleatoriedad posición",
            "variación aleatoria en la posición",
            0.2, 0, 1, 0.05
    ));


    private final NumberSetting offsetX = sgOffset.add(new NumberSetting(
            "desplazamiento pos. X",
            "desplazamiento en el eje X respecto al jugador",
            0, -1, 1, 0.01
    ));

    private final NumberSetting offsetY = sgOffset.add(new NumberSetting(
            "desplazamiento pos. Y",
            "desplazamiento en el eje Y respecto al jugador",
            0, -1, 1, 0.01
    ));

    private final NumberSetting offsetZ = sgOffset.add(new NumberSetting(
            "desplazamiento pos. Z",
            "desplazamiento en el eje Z respecto al jugador",
            -0.8, -1, 1, 0.01
    ));

    private final Random random = new Random();
    private int tickCounter = 0;

    public Particles() {
        super("partículas", "emite particulas artificiales", Category.RENDER);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        super.onEnable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        tickCounter++;
        if (tickCounter < spawnInterval.getIntValue()) {
            return;
        }
        tickCounter = 0;

        List<SimpleParticleType> enabledParticles = particles.getEnabledOptions();
        if (enabledParticles.isEmpty()) return;

        Vec3 basePos = new Vec3(
                mc.player.getX(),
                mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
                mc.player.getZ()
        );

        for (int i = 0; i < particleCount.getIntValue(); i++) {
            SimpleParticleType particle = enabledParticles.get(
                    random.nextInt(enabledParticles.size())
            );

            Vec3 pos = getPos(basePos);
            Vec3 vel = getVel();

            double yaw = Math.toRadians(mc.player.getYRot());
            double offX = offsetX.getValue() * Math.cos(yaw) - offsetZ.getValue() * Math.sin(yaw);
            double offZ = offsetX.getValue() * Math.sin(yaw) + offsetZ.getValue() * Math.cos(yaw);

            mc.level.addParticle(
                    particle,
                    pos.x + offX,
                    pos.y + offsetY.getValue(),
                    pos.z + offZ,
                    vel.x, vel.y, vel.z
            );
        }
    }

    private Vec3 getPos(Vec3 basePos) {
        double randX = (random.nextDouble() - 0.5) * 2 * positionRandomness.getValue();
        double randY = (random.nextDouble() - 0.5) * 2 * positionRandomness.getValue();
        double randZ = (random.nextDouble() - 0.5) * 2 * positionRandomness.getValue();

        return new Vec3(
                basePos.x + (random.nextDouble() - 0.5) * spreadX.getValue() + randX,
                basePos.y + (random.nextDouble() - 0.5) * spreadY.getValue() + randY,
                basePos.z + (random.nextDouble() - 0.5) * spreadZ.getValue() + randZ
        );
    }

    private Vec3 getVel() {
        double randX = (random.nextDouble() - 0.5) * 2 * velocityRandomness.getValue();
        double randY = (random.nextDouble() - 0.5) * 2 * velocityRandomness.getValue();
        double randZ = (random.nextDouble() - 0.5) * 2 * velocityRandomness.getValue();

        return new Vec3(
                (random.nextDouble() - 0.5) * velocity.getValue() + randX,
                (random.nextDouble() - 0.5) * velocity.getValue() + randY,
                (random.nextDouble() - 0.5) * velocity.getValue() + randZ
        );
    }
}