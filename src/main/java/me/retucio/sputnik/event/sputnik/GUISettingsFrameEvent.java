package me.retucio.sputnik.event.sputnik;

import com.github.retucio.neutrino.Event;

// se genera cada que se interactúa con el marco de ajustes de la interfaz
public class GUISettingsFrameEvent {

    public static class Extend extends Event {}  // al extenderlo / contraerlo
    public static class Move extends Event {}  // al cambiarlo de posición
}
