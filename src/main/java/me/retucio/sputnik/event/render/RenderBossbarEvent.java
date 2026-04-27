package me.retucio.sputnik.event.render;

import com.github.retucio.neutrino.Event;
import me.retucio.sputnik.mixin.mixins.hud.BossBarHudMixin;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.MutableComponent;

import java.awt.*;
import java.util.Iterator;


/**
 * @see BossBarHudMixin#modifyBossBarIterator
 * @see BossBarHudMixin#modifyBossBarName
 * @see BossBarHudMixin#modifySpacingConstant
 */
public class RenderBossbarEvent {

    public static class BossText extends Event {

        private LerpingBossEvent bossBar;
        private MutableComponent name;

        public BossText(LerpingBossEvent bossBar, MutableComponent name) {
            this.bossBar = bossBar;
            this.name = name;
        }

        public LerpingBossEvent getBossBar() {
            return bossBar;
        }

        public void setBossBar(LerpingBossEvent bossBar) {
            this.bossBar = bossBar;
        }

        public MutableComponent getName() {
            return name;
        }

        public void setName(MutableComponent name) {
            this.name = name;
        }
    }

    public static class BossSpacing extends Event {

        private int spacing;

        public BossSpacing(int spacing) {
            this.spacing = spacing;
        }

        public int getSpacing() {
            return spacing;
        }

        public void setSpacing(int spacing) {
            this.spacing = spacing;
        }
    }

    public static class BossIterator extends Event {

        private Iterator<LerpingBossEvent> iterator;

        public BossIterator(Iterator<LerpingBossEvent> iterator) {
            this.iterator = iterator;
        }

        public Iterator<LerpingBossEvent> getIterator() {
            return iterator;
        }

        public void setIterator(Iterator<LerpingBossEvent> iterator) {
            this.iterator = iterator;
        }
    }
}
