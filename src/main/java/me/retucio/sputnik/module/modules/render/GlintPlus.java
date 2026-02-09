package me.retucio.sputnik.module.modules.render;

import me.retucio.sputnik.mixin.mixins.render.BufferStorageMixin;
import me.retucio.sputnik.mixin.mixins.render.EquipmentRendererMixin;
import me.retucio.sputnik.mixin.mixins.render.ItemRendererMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.util.render.GlintRenderLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.DyeColor;

/** continúa en:
 * @see BufferStorageMixin
 * @see EquipmentRendererMixin
 * @see ItemRendererMixin
 */

public class GlintPlus extends Module {

    private final BooleanSetting items = sgGeneral.add(new BooleanSetting(
            "items",
            "modificar el destello de encantamiento de los items",
            true
    ));

    private final BooleanSetting armor = sgGeneral.add(new BooleanSetting(
            "armadura",
            "modificar el destello de encantamiento de la armadura",
            true
    ));

    private final EnumSetting<GlintColors> glintColor = sgGeneral.add(new EnumSetting<>(
            "colores",
            "color del glint",
            GlintColors.class,
            GlintColors.PURPLE
    ));

    public GlintPlus() {
        super("destello de enchants.",
                "modifica el color del brillo de los encantamientos",
                Category.RENDER);
    }

    public RenderLayer getGlint() {
        int color = getColor();
        if (!isEnabled() || !items.getValue()) return RenderLayers.glint();
        return GlintRenderLayer.glintColor.get(color);
    }

    public RenderLayer getEntityGlint() {
        int color = getColor();
        if (!isEnabled() || !items.getValue()) return RenderLayers.entityGlint();
        return GlintRenderLayer.entityGlintColor.get(color);
    }

    public RenderLayer getArmorEntityGlint() {
        int color = getColor();
        if (!isEnabled() || !armor.getValue()) return RenderLayers.armorEntityGlint();
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
