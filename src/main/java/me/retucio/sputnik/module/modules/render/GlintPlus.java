package me.retucio.sputnik.module.modules.render;

import me.retucio.sputnik.mixin.mixins.render.RenderBuffersMixin;
import me.retucio.sputnik.mixin.mixins.render.EquipmentLayerRendererMixin;
import me.retucio.sputnik.mixin.mixins.render.ItemFeatureRendererMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.util.render.GlintRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.item.DyeColor;

/** continúa en:
 * @see RenderBuffersMixin
 * @see EquipmentLayerRendererMixin
 * @see ItemFeatureRendererMixin
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

    public RenderType getGlint() {
        int color = getColor();
        if (!isEnabled() || !items.getValue()) return RenderTypes.glint();
        return GlintRenderType.glintColor.get(color);
    }

    public RenderType getGlintTranslucent() {
        int color = getColor();
        if (!isEnabled() || !items.getValue()) return RenderTypes.glintTranslucent();
        return GlintRenderType.glintTranslucentColor.get(color);
    }

    public RenderType getEntityGlint() {
        int color = getColor();
        if (!isEnabled() || !items.getValue()) return RenderTypes.entityGlint();
        return GlintRenderType.entityGlintColor.get(color);
    }

    public RenderType getArmorEntityGlint() {
        int color = getColor();
        if (!isEnabled() || !armor.getValue()) return RenderTypes.armorEntityGlint();
        return GlintRenderType.armorEntityGlintColor.get(color);
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
            if (dye.name().equalsIgnoreCase(colorName)) return dye.getId();

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
