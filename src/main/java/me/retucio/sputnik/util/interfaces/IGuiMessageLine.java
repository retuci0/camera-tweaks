package me.retucio.sputnik.util.interfaces;

public interface IGuiMessageLine extends IGuiMessage {

    boolean sputnik$isStartOfEntry();
    void sputnik$setStartOfEntry(boolean start);
}