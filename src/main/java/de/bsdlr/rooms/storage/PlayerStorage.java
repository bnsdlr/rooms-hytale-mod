package de.bsdlr.rooms.storage;

import java.time.Instant;

public class PlayerStorage {
    private final Instant joined;
    private boolean updateHud = false;

    public PlayerStorage() {
        this.joined = Instant.now();
    }

    public Instant getJoined() {
        return joined;
    }

    public boolean isUpdateHud() {
        return updateHud;
    }

    public void setUpdateHud(boolean updateHud) {
        this.updateHud = updateHud;
    }
}
