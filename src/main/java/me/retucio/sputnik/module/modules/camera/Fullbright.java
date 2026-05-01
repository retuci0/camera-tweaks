package me.retucio.sputnik.module.modules.camera;

import me.retucio.sputnik.mixin.accessors.MobEffectInstanceAccessor;
import me.retucio.sputnik.mixin.mixins.render.LightmapRenderStateExtractorMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.ColorSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

/** continúa en:
 * @see LightmapRenderStateExtractorMixin
 *
 * @author retucio
 */

public class Fullbright extends Module {

    public final EnumSetting<Modes> mode = sgGeneral.add(new EnumSetting<>(
            "modo",
            "qué modo de iluminación emplear (usar poción con shaders)",
            Modes.class, Modes.GAMMA
    ));

    public final ColorSetting color = sgGeneral.add(new ColorSetting(
            "filtro",
            "filtro de color",
            new Color(255, 255, 255, 255),
            false
    )).visibility(() -> mode.is(Modes.GAMMA));


    public Fullbright() {
        super("brilli brilli",
                "deshabilita la oscuridad (y aplica colores a los shaders)",
                Category.CAMERA,
                GLFW.GLFW_KEY_K);

        mode.onUpdate(mode -> { if (mode != Modes.POTION) disableNightVision(); });
    }

    @Override
    public void onDisable() {
        disableNightVision();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || !mode.getValue().equals(Modes.POTION)) return;

        if (mc.player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.NIGHT_VISION.value()))) {
            MobEffectInstance instance = mc.player.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.NIGHT_VISION.value()));
            if (instance != null && instance.getDuration() < 5200) ((MobEffectInstanceAccessor) instance).setDuration(5200);
        } else {
            mc.player.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.NIGHT_VISION.value()), 69, 0));
        }
    }

    private void disableNightVision() {
        if (mc.player == null) return;
        if (mc.player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.NIGHT_VISION.value()))) {
            mc.player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.NIGHT_VISION.value()));
        }
    }


    public enum Modes {
        GAMMA("gamma"),
        POTION("poción");

        private final String name;
        Modes(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}