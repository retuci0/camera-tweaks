package me.retucio.camtweaks.event.events;

import me.retucio.camtweaks.event.Event;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;

import java.util.Iterator;


/**
 * @see me.retucio.camtweaks.mixin.BossBarHudMixin#modifyBossBarIterator
 * @see me.retucio.camtweaks.mixin.BossBarHudMixin#modifyBossBarName
 * @see me.retucio.camtweaks.mixin.BossBarHudMixin#modifySpacingConstant
 */
public class RenderBossbarEvent {

    public static class BossText extends Event {

        private ClientBossBar bossBar;
        private Text name;

        public BossText(ClientBossBar bossBar, Text name) {
            this.bossBar = bossBar;
            this.name = name;
        }

        public ClientBossBar getBossBar() {
            return bossBar;
        }

        public void setBossBar(ClientBossBar bossBar) {
            this.bossBar = bossBar;
        }

        public Text getName() {
            return name;
        }

        public void setName(Text name) {
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

        private Iterator<ClientBossBar> iterator;

        public BossIterator(Iterator<ClientBossBar> iterator) {
            this.iterator = iterator;
        }

        public Iterator<ClientBossBar> getIterator() {
            return iterator;
        }

        public void setIterator(Iterator<ClientBossBar> iterator) {
            this.iterator = iterator;
        }
    }
}
