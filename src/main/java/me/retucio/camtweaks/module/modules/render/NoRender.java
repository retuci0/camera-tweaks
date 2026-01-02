package me.retucio.camtweaks.module.modules.render;

import me.retucio.camtweaks.module.Category;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.module.settings.ListSetting;
import me.retucio.camtweaks.util.Lists;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Formatting;

/** continúa en:
 * @see me.retucio.camtweaks.mixin.ClientPlayNetworkHandlerMixin
 * @see me.retucio.camtweaks.mixin.FogRendererMixin
 * @see me.retucio.camtweaks.mixin.GameRendererMixin
 * @see me.retucio.camtweaks.mixin.InGameHudMixin
 * @see me.retucio.camtweaks.mixin.InGameOverlayRendererMixin
 * @see me.retucio.camtweaks.mixin.ParticleManagerMixin
 * @see me.retucio.camtweaks.mixin.StatusEffectFogModifierMixin
 * @see me.retucio.camtweaks.mixin.StuckArrowsFeatureRendererMixin
 * @see me.retucio.camtweaks.mixin.TextRendererDrawerMixin
 * @see me.retucio.camtweaks.mixin.WeatherRenderingMixin
 */

public class NoRender extends Module {

    public BooleanSetting spyglassOverlay = addSetting(new BooleanSetting("catalejo", "renderizar overlay del catalejo", false));
    public BooleanSetting fluidOverlay = addSetting(new BooleanSetting("fluídos", "renderizar fluídos sobre la cámara como el agua, lava o nieve en polvo (aunque no sea un fluído)", false));
    public BooleanSetting pumpkinOverlay = addSetting(new BooleanSetting("calabaza", "renderiza el overlay de la calabaza tallada", false));
    public BooleanSetting fireOverlay = addSetting(new BooleanSetting("fuego", "renderizar llamas sobre la cámara", false));
    public BooleanSetting portalOverlay = addSetting(new BooleanSetting("portal", "renderizar el overlay del portal del nether", false));

    public BooleanSetting rain = addSetting(new BooleanSetting("lluvia", "l l u v i a", true));
    public BooleanSetting snow = addSetting(new BooleanSetting("nieve", "n i e v e", true));
    public BooleanSetting endFlashes = addSetting(new BooleanSetting("flashes del end", "renderizar los recientemente añadidos flashes en el end", true));

    public ListSetting<ParticleType<?>> particles = addSetting(new ListSetting<>("partículas", "lista de partículas que se renderizan",
            Lists.particleList, Lists.allTrue(Lists.particleList), Lists.particleNames));

    public BooleanSetting nauseaEffect = addSetting(new BooleanSetting("náusea", "renderizar borrachera", false));
    public BooleanSetting blindnessEffect = addSetting(new BooleanSetting("ceguera", "renderizar miopía", false));
    public BooleanSetting darknessEffect = addSetting(new BooleanSetting("oscuridad", "renderizar miedo a la oscuridad", false));

    public BooleanSetting scoreboard = addSetting(new BooleanSetting("marcador", "mostrar marcador a la derecha", false));
    public BooleanSetting titles = addSetting(new BooleanSetting("títulos", "mostrar títulos (del comando /title)", false));
    public BooleanSetting totemPop = addSetting(new BooleanSetting("tótem", "renderizar el pop del tótem", true));

    public BooleanSetting stuckArrows = addSetting(new BooleanSetting("flechas clavadas", "renderizar flechas clavadas en jugadores", false));

    public BooleanSetting bold = addSetting(new BooleanSetting("negrita", "letra pero §lgorda", true));
//    public BooleanSetting italics = addSetting(new BooleanSetting("cursiva", "letra pero §otorcida", true));
    public BooleanSetting underlined = addSetting(new BooleanSetting("subrayado", "letra pero §nen el suelo", true));
    public BooleanSetting strikethrough = addSetting(new BooleanSetting("tachado", "letra pero §mdiscriminada", true));
    public EnumSetting<Colors> color = addSetting(new EnumSetting<>("colores", "§co§4r§6g§eu§al§2l§9o §1g§5a§dy", Colors.class, Colors.DEFAULT));
    public BooleanSetting scrambledText = addSetting(new BooleanSetting("garabato", "engarabatar texto (o como se diga) (§kasí§r)", false));

    public NoRender() {
        super("no render",
                "customiza qué se renderiza y qué no",
                Category.RENDER);
    }

    public enum Colors {
        BLACK("negro"),
        DARK_BLUE("azul oscuro"),
        DARK_GREEN("verde"),
        DARK_AQUA("cian"),
        DARK_RED("granate"),
        DARK_PURPLE("morado"),
        GOLD("naranja"),
        GRAY("gris claro"),
        DARK_GRAY("gris oscuro"),
        BLUE("azul"),
        GREEN("lima"),
        AQUA("turquesa"),
        RED("rojo"),
        LIGHT_PURPLE("lila"),
        YELLOW("amarillo"),
        WHITE("blanco"),
        DEFAULT("por defecto");

        private final String name;
        Colors(String name) { this.name = name; }
        @Override public String toString() { return name; }
        public Formatting toFormatting() { return (Formatting.byColorIndex(this.ordinal())); }
    }
}
