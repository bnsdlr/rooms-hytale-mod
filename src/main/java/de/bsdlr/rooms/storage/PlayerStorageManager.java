package de.bsdlr.rooms.storage;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStorageManager {
    private static PlayerStorageManager instance;
    private final Map<UUID, PlayerStorage> uuidToPlayerStorage;

    public PlayerStorageManager() {
        instance = this;
        uuidToPlayerStorage = new HashMap<>();
    }

    public static PlayerStorageManager get() {
        return instance;
    }

    public void add(UUID uuid) {
        uuidToPlayerStorage.put(uuid, new PlayerStorage());
    }

    public PlayerStorage getAndCreate(UUID uuid) {
        if (!uuidToPlayerStorage.containsKey(uuid)) {
            add(uuid);
        }
        return get(uuid);
    }

    public PlayerStorage get(UUID uuid) {
        return uuidToPlayerStorage.get(uuid);
    }

    public void remove(UUID uuid) {
        uuidToPlayerStorage.remove(uuid);
    }
}
