package me.retucio.sputnik.event.sputnik;

import com.github.retucio.neutrino.Event;

public class LoadConfigEvent extends Event {

    private final boolean failed;
    private final boolean fileExisted;


    public LoadConfigEvent(boolean failed, boolean fileExisted) {
        this.failed = failed;
        this.fileExisted = fileExisted;
    }

    public boolean hasFailed() {
        return failed;
    }

    public boolean didFileExist() {
        return fileExisted;
    }
}
