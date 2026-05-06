package me.retucio.sputnik.event.sputnik;

import com.github.retucio.neutrino.Event;

public class FriendPanelEvent {

    public static class Extend extends Event {}  // al extenderlo / contraerlo
    public static class Move extends Event {}  // al cambiarlo de posición
}