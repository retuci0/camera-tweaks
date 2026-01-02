package me.retucio.camtweaks.module.modules.camera;

import me.retucio.camtweaks.mixin.accessor.StatusEffectInstanceAccessor;
import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.ColorSetting;
import me.retucio.camtweaks.module.settings.EnumSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.LightmapTextureManagerMixin
 */

public class Fullbright extends Module {

    public EnumSetting<Modes> mode = addSetting(new EnumSetting<>("modo", "qué modo de iluminación emplear (usar poción con shaders)", Modes.class, Modes.GAMMA));

    public ColorSetting color = addSetting(new ColorSetting("filtro", "filtro de color", new Color(255, 255, 255, 255), false));

    public Fullbright() {
        super("brilli brilli",
                "deshabilita la oscuridad (y aplica colores a los shaders)",
                Category.CAMERA,
                GLFW.GLFW_KEY_K);
        mode.onUpdate(mode -> { if (mode != Modes.POTION) disableNightVision(); });
        mode.onUpdate(mode -> {
            boolean v = mode.equals(Modes.GAMMA);
            color.setVisible(v);

            if (v) disableNightVision();
        });
    }

    @Override
    public void onDisable() {
        disableNightVision();
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.player == null || !mode.getValue().equals(Modes.POTION)) return;

        if (mc.player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(StatusEffects.NIGHT_VISION.value()))) {
            StatusEffectInstance instance = mc.player.getStatusEffect(Registries.STATUS_EFFECT.getEntry(StatusEffects.NIGHT_VISION.value()));
            if (instance != null && instance.getDuration() < 5200) ((StatusEffectInstanceAccessor) instance).setDuration(5200);
        } else {
            mc.player.addStatusEffect(new StatusEffectInstance(Registries.STATUS_EFFECT.getEntry(StatusEffects.NIGHT_VISION.value()), 69, 0));
        }
    }

    private void disableNightVision() {
        if (mc.player == null) return;
        if (mc.player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(StatusEffects.NIGHT_VISION.value())))
            mc.player.removeStatusEffect(Registries.STATUS_EFFECT.getEntry(StatusEffects.NIGHT_VISION.value()));
    }


    public enum Modes {
        GAMMA("gamma"),
        POTION("poción");

        private final String name;
        Modes(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}