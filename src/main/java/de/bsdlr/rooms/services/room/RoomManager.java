package de.bsdlr.rooms.services.room;

import com.hypixel.hytale.math.vector.Vector3d;
import de.bsdlr.rooms.utils.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoomManager {
    private final Set<RoomEntity> rooms;
    private final Map<Long, RoomEntity> longToRoom;

    public RoomManager() {
        longToRoom = new HashMap<>();
        rooms = new HashSet<>();
    }

    public Set<RoomEntity> getRoomsEntity() {
        return rooms;
    }

    public RoomEntity getRoomEntity(Vector3d position) {
        return getRoomEntity(Utils.encodePosition(Utils.positionToVector3i(position)));
    }

    public RoomEntity getRoomEntity(int x, int y, int z) {
        return getRoomEntity(Utils.encodePosition(x, y, z));
    }

    public RoomEntity getRoomEntity(long key) {
        return longToRoom.get(key);
    }

    public void addRoomEntity(RoomEntity roomEntity) {
        rooms.add(roomEntity);
        for (Long key : roomEntity.getBlocks()) {
            longToRoom.put(key, roomEntity);
        }
    }

    public void removeRoomEntity(RoomEntity roomEntity) {
        rooms.remove(roomEntity);
        for (Long key : roomEntity.getBlocks()) {
            longToRoom.remove(key);
        }
    }
}
