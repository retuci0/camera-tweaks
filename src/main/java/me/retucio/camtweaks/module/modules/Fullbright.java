package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.mixin.accessor.StatusEffectInstanceAccessor;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.EnumSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import org.lwjgl.glfw.GLFW;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.LightmapTextureManagerMixin
 */

public class Fullbright extends Module {

    public EnumSetting<Modes> mode = addSetting(new EnumSetting<>("modo", "qué modo de iluminación emplear (usar poción con shaders)", Modes.class, Modes.GAMMA));

    public Fullbright() {
        super("brilli brilli", "deshabilita la oscuridad");
        assignKey(GLFW.GLFW_KEY_K);
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

        if (mc.player.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(StatusEffects.NIGHT_VISION.value()))) {
            StatusEffectInstance instance = mc.player.getStatusEffect(Registries.STATUS_EFFECT.getEntry(StatusEffects.NIGHT_VISION.value()));
            if (instance != null && instance.getDuration() < 69) ((StatusEffectInstanceAccessor) instance).setDuration(69);
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

        @Override
        public String toString() { return name; }
    }
}