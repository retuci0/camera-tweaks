package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.util.render.GlintRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.DyeColor;

import java.awt.*;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.BufferStorageMixin
 * @see me.retucio.camtweaks.mixin.EquipmentRendererMixin
 * @see me.retucio.camtweaks.mixin.ItemRendererMixin
 */

public class GlintPlus extends Module {

    public BooleanSetting items = addSetting(new BooleanSetting("items", "modificar el destello de encantamiento de los items", true));
    public BooleanSetting armor = addSetting(new BooleanSetting("armadura", "modificar el destello de encantamiento de la armadura", true));
    public EnumSetting<GlintColors> glintColor = addSetting(new EnumSetting<>("colores", "color del glint", GlintColors.class, GlintColors.PURPLE));

    public GlintPlus() {
        super("destello de enchants.", "modifica el color del brillo de los encantamientos");
    }

    public RenderLayer getGlint() {
        int color = getColor();
        if (!isEnabled() || !items.isEnabled()) return RenderLayer.getGlint();
        return GlintRenderLayer.glintColor.get(color);
    }

    public RenderLayer getEntityGlint() {
        int color = getColor();
        if (!isEnabled() || !items.isEnabled()) return RenderLayer.getEntityGlint();
        return GlintRenderLayer.entityGlintColor.get(color);
    }

    public RenderLayer getArmorEntityGlint() {
        int color = getColor();
        if (!isEnabled() || !armor.isEnabled()) return RenderLayer.getArmorEntityGlint();
        return GlintRenderLayer.armorEntityGlintColor.get(color);
    }

    public int getColor() {
        String colorName = glintColor.getValue().getRealName().toLowerCase();

        switch (colorName) {
            case "rainbow":
                return DyeColor.values().length;
            case "none":
                return DyeColor.values().length + 1;
        }

        for (DyeColor dye : DyeColor.values())
            if (dye.name().equalsIgnoreCase(colorName)) return dye.getIndex();

        return -1;
    }

    public enum GlintColors {
        RED("rojo"),
        ORANGE("naranja"),
        YELLOW("amarillo"),
        LIME("lima"),
        GREEN("verde"),
        CYAN("cian"),
        LIGHT_BLUE("celeste"),
        BLUE("azul"),
        PURPLE("morado"),
        MAGENTA("magenta"),
        PINK("rosa"),
        BROWN("marrón"),
        BLACK("negro"),
        GRAY("gris"),
        LIGHT_GRAY("plata"),
        WHITE("blanco"),
        RAINBOW("gay."),
        NONE("desactivado");

        private final String name;
        GlintColors(String name) { this.name = name; }
        @Override public String toString() { return name; }
        public String getRealName() { return super.toString(); }
    }
}
