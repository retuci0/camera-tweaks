package me.retucio.camtweaks.event.events.camtweaks;

import me.retucio.camtweaks.event.Event;

// se genera cada que se interactúa con el marco de módulos
public class ModuleFrameEvent {

    public static class Extend extends Event {}  // al extenderlo / contraerlo
    public static class Move extends Event {}  // al cambiarlo de posición
}