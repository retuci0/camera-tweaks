package me.retucio.sputnik.module.modules.render;

import me.retucio.sputnik.mixin.mixins.hud.InGameHudMixin;
import me.retucio.sputnik.mixin.mixins.network.ClientPlayNetworkHandlerMixin;
import me.retucio.sputnik.mixin.mixins.render.*;
import me.retucio.sputnik.mixin.mixins.world.ParticleManagerMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.ListSetting;
import me.retucio.sputnik.util.Lists;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Formatting;

/** continúa en:
 * @see ClientPlayNetworkHandlerMixin
 * @see FogRendererMixin
 * @see GameRendererMixin
 * @see InGameHudMixin
 * @see InGameOverlayRendererMixin
 * @see ParticleManagerMixin
 * @see StatusEffectFogModifierMixin
 * @see StuckArrowsFeatureRendererMixin
 * @see TextRendererDrawerMixin
 * @see WeatherRenderingMixin
 */

public class NoRender extends Module {

    private final SettingGroup sgOverlays = addSg(new SettingGroup("overlays", true));
    private final SettingGroup sgAmbient = addSg(new SettingGroup("ambiente", true));
    private final SettingGroup sgEffects = addSg(new SettingGroup("efectos", true));
    private final SettingGroup sgUi = addSg(new SettingGroup("interfaz", true));
    private final SettingGroup sgText = addSg(new SettingGroup("texto", true));
    private final SettingGroup sgMisc = addSg(new SettingGroup("misc.", true));

    public final BooleanSetting spyglassOverlay = sgOverlays.add(new BooleanSetting("catalejo", "renderizar overlay del catalejo", false));
    public final BooleanSetting fluidOverlay = sgOverlays.add(new BooleanSetting("fluídos", "renderizar fluídos sobre la cámara como el agua, lava o nieve en polvo (aunque no sea un fluído)", false));
    public final BooleanSetting pumpkinOverlay = sgOverlays.add(new BooleanSetting("calabaza", "renderiza el overlay de la calabaza tallada", false));
    public final BooleanSetting fireOverlay = sgOverlays.add(new BooleanSetting("fuego", "renderizar llamas sobre la cámara", false));
    public final BooleanSetting portalOverlay = sgOverlays.add(new BooleanSetting("portal", "renderizar el overlay del portal del nether", false));

    public final BooleanSetting rain = sgAmbient.add(new BooleanSetting("lluvia", "l l u v i a", true));
    public final BooleanSetting snow = sgAmbient.add(new BooleanSetting("nieve", "n i e v e", true));
    public final BooleanSetting endFlashes = sgAmbient.add(new BooleanSetting("flashes del end", "renderizar los recientemente añadidos flashes en el end", true));
    public final ListSetting<ParticleType<?>> particles = sgAmbient.add(new ListSetting<>("partículas", "lista de partículas que se renderizan",
            Lists.particleList, Lists.allTrue(Lists.particleList), Lists.particleNames));

    public final BooleanSetting nauseaEffect = sgEffects.add(new BooleanSetting("náusea", "renderizar borrachera", false));
    public final BooleanSetting blindnessEffect = sgEffects.add(new BooleanSetting("ceguera", "renderizar miopía", false));
    public final BooleanSetting darknessEffect = sgEffects.add(new BooleanSetting("oscuridad", "renderizar miedo a la oscuridad", false));

    public final BooleanSetting scoreboard = sgUi.add(new BooleanSetting("marcador", "mostrar marcador a la derecha", false));
    public final BooleanSetting titles = sgUi.add(new BooleanSetting("títulos", "mostrar títulos (del comando /title)", false));
    public final BooleanSetting totemPop = sgUi.add(new BooleanSetting("tótem", "renderizar el pop del tótem", true));

    public final BooleanSetting bold = sgText.add(new BooleanSetting("negrita", "letra pero §lgorda", true));
//    public BooleanSetting italics = addSetting(new BooleanSetting("cursiva", "letra pero §otorcida", true));
    public final BooleanSetting underlined = sgText.add(new BooleanSetting("subrayado", "letra pero §nen el suelo", true));
    public final BooleanSetting strikethrough = sgText.add(new BooleanSetting("tachado", "letra pero §mdiscriminada", true));
    public final EnumSetting<Colors> color = sgText.add(new EnumSetting<>("colores", "§co§4r§6g§eu§al§2l§9o §1g§5a§dy", Colors.class, Colors.DEFAULT));
    public final BooleanSetting scrambledText = sgText.add(new BooleanSetting("garabato", "engarabatar texto (o como se diga) (§kasí§r)", false));

    public final BooleanSetting stuckArrows = sgMisc.add(new BooleanSetting("flechas clavadas", "renderizar flechas clavadas en jugadores", false));

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
