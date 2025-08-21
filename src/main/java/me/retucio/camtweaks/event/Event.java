package me.retucio.camtweaks.event;

// evento base, cancelable
public class Event {
    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }
}