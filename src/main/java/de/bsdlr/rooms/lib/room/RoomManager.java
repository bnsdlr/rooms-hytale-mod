package de.bsdlr.rooms.lib.room;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import de.bsdlr.rooms.utils.PositionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoomManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final BuilderCodec<RoomManager> CODEC = BuilderCodec.builder(RoomManager.class, RoomManager::new)
            .append(new KeyedCodec<>("Rooms", new SetCodec<>(Room.CODEC, HashSet::new, false)),
                    (manager, rooms) -> {
                        for (Room room : rooms) {
                            manager.addRoom(room);
                        }
                    },
                    RoomManager::getRooms)
            .add()
            .build();
    protected final Set<Room> rooms;
    protected final Map<Long, Room> longToRoom;

    public RoomManager() {
        longToRoom = new HashMap<>();
        rooms = new HashSet<>();
    }

    public Set<Room> getRooms() {
        return rooms;
    }

    public Room getRoom(Vector3d position) {
        return getRoom(PositionUtils.encodePosition(PositionUtils.positionToVector3i(position)));
    }

    public Room getRoom(int x, int y, int z) {
        return getRoom(PositionUtils.encodePosition(x, y, z));
    }

    public Room getRoom(long key) {
        return longToRoom.get(key);
    }

    public void addRoom(Room room) {
        LOGGER.atInfo().log("Adding room: %s", room.uuid);
        rooms.add(room);
//        int containsKeyCounter = 0;
//        int sameRoomCounter = 0;
        for (Long key : room.getBlocks()) {
//            if (longToRoom.containsKey(key)) {
//                containsKeyCounter++;
//                if (!longToRoom.get(key).equals(room)) {
//                    sameRoomCounter++;
//                    removeRoom(longToRoom.get(key));
//                }
//            }
            longToRoom.put(key, room);
        }
//        LOGGER.atInfo().log("contains key counter: %d", containsKeyCounter);
//        LOGGER.atInfo().log("same room counter: %d", sameRoomCounter);
    }

    public void removeRoom(Room room) {
        LOGGER.atInfo().log("Removing room: %s", room.uuid);
        rooms.remove(room);
        for (Long key : room.getBlocks()) {
            longToRoom.remove(key);
        }
    }
}
