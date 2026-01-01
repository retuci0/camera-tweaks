package me.retucio.camtweaks.event.events.camtweaks;

import me.retucio.camtweaks.event.Event;
import me.retucio.camtweaks.ui.widgets.frames.SettingsFrame;

// se genera cada que se interactúa con un marco de ajustes de un módulo
public class SettingsFrameEvent {

    public static class Open extends Event {  // al abrirlo
        private final SettingsFrame frame;
        public Open(SettingsFrame frame) { this.frame = frame; }
        public SettingsFrame getFrame() { return frame; }
    }

    public static class Close extends Event {  // al cerrarlo
        private final SettingsFrame frame;
        public Close(SettingsFrame frame) { this.frame = frame; }
        public SettingsFrame getFrame() { return frame; }
    }

    public static class Move extends Event {  // al cambiarlo de posición
        private final SettingsFrame frame;
        public Move(SettingsFrame frame) { this.frame = frame; }
        public SettingsFrame getFrame() { return frame; }
    }

}