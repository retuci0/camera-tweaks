package me.retucio.camtweaks.module.modules;

import me.retucio.camtweaks.event.SubscribeEvent;
import me.retucio.camtweaks.event.events.PacketEvent;
import me.retucio.camtweaks.module.Module;
import me.retucio.camtweaks.module.settings.BooleanSetting;
import me.retucio.camtweaks.module.settings.EnumSetting;
import me.retucio.camtweaks.module.settings.NumberSetting;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

// continúa en ClientWorldPropertiesMixin, DimensionTypeMixin,
public class TimeChanger extends Module {

    public BooleanSetting renderSun = addSetting(new BooleanSetting("sol", "que haya sol o no", true));
    public BooleanSetting renderMoon = addSetting(new BooleanSetting("luna", "que haya luna o no", true));
    public BooleanSetting renderStars = addSetting(new BooleanSetting("estrellas", "que hayan estrellas o no", true));

    public EnumSetting<MoonPhases> moonPhase = addSetting(new EnumSetting<>("fase lunar", "fase lunar actual", MoonPhases.class, MoonPhases.DEFAULT));
    public NumberSetting time = addSetting(new NumberSetting("hora", "hora del juego", 0, -20000, 20000, 1));

    public TimeChanger() {
        super("cielo custom", "te deja cambiar visualmente el progreso del día");
        renderMoon.onUpdate(v -> moonPhase.setVisible(v));
    }

    private long realTime;

    @Override
    public void onEnable() {
        if (mc.world == null) return;
        realTime = mc.world.getTime();
    }

    @Override
    public void onDisable() {
        if (mc.world == null) return;
        mc.world.getLevelProperties().setTimeOfDay(realTime);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket packet) {
            realTime = packet.timeOfDay();
            event.cancel();
        }
    }

    @Override
    public void onTick() {
        if (mc.world == null) return;
        mc.world.getLevelProperties().setTimeOfDay(time.getLongValue());
    }

    public int getMoonPhase() {
        if (mc.world == null) return -1;

        return switch (moonPhase.getValue()) {
            case DEFAULT -> (int) (mc.world.getLunarTime() / 24000L) % 8;
            case FULL_MOON -> 0;
            case WANING_GIBBOUS -> 1;
            case LAST_QUARTER -> 2;
            case WANING_CRESCENT -> 3;
            case NEW_MOON -> 4;
            case WAXING_CRESCENT -> 5;
            case FIRST_QUARTER -> 6;
            case WAXING_GIBBOUS -> 7;
        };
    }

    public enum MoonPhases {
        DEFAULT("por defecto"),
        FULL_MOON("luna llena"),
        WANING_GIBBOUS("menguante gibosa"),
        LAST_QUARTER("cuarto menguante"),
        WANING_CRESCENT("luna vieja"),
        NEW_MOON("luna nueva"),
        WAXING_CRESCENT("creciente"),
        FIRST_QUARTER("cuarto creciente"),
        WAXING_GIBBOUS("creciente gibosa");

        private final String name;
        MoonPhases(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }
}
