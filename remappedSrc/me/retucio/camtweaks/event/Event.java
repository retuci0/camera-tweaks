package me.retucio.camtweaks.event;

// evento base, cancelable (usado en mixins)
public class Event {
    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }
}