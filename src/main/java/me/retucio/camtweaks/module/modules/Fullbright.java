package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.mixin.accessor.StatusEffectInstanceAccessor;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import org.lwjgl.glfw.GLFW;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.LightmapTextureManagerMixin
 */

public class Fullbright extends Module {

    public EnumSetting<Modes> mode = addSetting(new EnumSetting<>("modo", "qué modo de iluminación emplear (usar poción con shaders)", Modes.class, Modes.GAMMA));

    public NumberSetting red = addSetting(new NumberSetting("rojo", "cantidad de rojo a aplicar",
            255, 0, 255, 1));

    public NumberSetting green = addSetting(new NumberSetting("verde", "cantidad de verde a aplicar",
            255, 0, 255, 1));

    public NumberSetting blue = addSetting(new NumberSetting("azul", "cantidad de azul a aplicar",
            255, 0, 255, 1));

    public NumberSetting alpha = addSetting(new NumberSetting("opacidad", "opacidad del shader",
            255, 0, 255, 1));

    public Fullbright() {
        super("brilli brilli", "deshabilita la oscuridad (y aplica colores a los shaders)");
        assignKey(GLFW.GLFW_KEY_K);
        mode.onUpdate(mode -> { if (mode != Modes.POTION) disableNightVision(); });
        mode.onUpdate(mode -> {
            boolean v = mode.equals(Modes.GAMMA);
            red.setVisible(v);
            green.setVisible(v);
            blue.setVisible(v);
            alpha.setVisible(v);
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