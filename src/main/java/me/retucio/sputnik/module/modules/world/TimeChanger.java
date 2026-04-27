package me.retucio.sputnik.module.modules.world;

import com.github.retucio.neutrino.EventListener;
import me.retucio.sputnik.event.network.PacketEvent;
import me.retucio.sputnik.mixin.mixins.render.SkyRendererMixin;
import me.retucio.sputnik.mixin.mixins.world.ClientLevelDataMixin;
import me.retucio.sputnik.module.Category;
import me.retucio.sputnik.module.Module;
import me.retucio.sputnik.module.setting.SettingGroup;
import me.retucio.sputnik.module.setting.settings.BooleanSetting;
import me.retucio.sputnik.module.setting.settings.EnumSetting;
import me.retucio.sputnik.module.setting.settings.NumberSetting;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;


/** continúa en:
 * @see ClientLevelDataMixin
 * @see SkyRendererMixin
 */

public class TimeChanger extends Module {

    private final SettingGroup sgCelestialBodies = addSg(new SettingGroup("cuerpos celeste", false));

    public final NumberSetting time = sgGeneral.add(new NumberSetting("hora", "hora del juego", 0, -20000, 20000, 1));

    public final BooleanSetting renderSun = sgCelestialBodies.add(new BooleanSetting("sol", "que haya sol o no", true));
    public final BooleanSetting renderMoon = sgCelestialBodies.add(new BooleanSetting("luna", "que haya luna o no", true));
    public final BooleanSetting renderStars = sgCelestialBodies.add(new BooleanSetting("estrellas", "que hayan estrellas o no", true));

    public EnumSetting<MoonPhases> moonPhase = sgCelestialBodies.add(new EnumSetting<>("fase lunar", "fase lunar actual", MoonPhases.class, MoonPhases.DEFAULT));

    public TimeChanger() {
        super("cielo custom",
                "te deja cambiar visualmente el progreso del día, entre otras cosas",
                Category.WORLD);

        renderMoon.onUpdate(v -> moonPhase.visibility(v));
    }

    private long realTime;

    @Override
    public void onEnable() {
        if (mc.level == null) return;
        realTime = mc.level.getGameTime();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.level == null) return;
        mc.level.getLevelData().setGameTime(realTime);
        super.onDisable();
    }

    @Override
    public void onTick() {
        if (mc.level == null) return;
        mc.level.getLevelData().setGameTime(time.getLongValue());
    }

    @EventListener
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ClientboundSetTimePacket packet) {
            realTime = packet.gameTime();
            event.cancel();
        }
    }

    public enum MoonPhases {
        FULL_MOON("luna llena"),
        WANING_GIBBOUS("menguante gibosa"),
        THIRD_QUARTER("cuarto menguante"),
        WANING_CRESCENT("luna vieja"),
        NEW_MOON("luna nueva"),
        WAXING_CRESCENT("creciente"),
        FIRST_QUARTER("cuarto creciente"),
        WAXING_GIBBOUS("creciente gibosa"),
        DEFAULT("por defecto");

        private final String name;
        MoonPhases(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
