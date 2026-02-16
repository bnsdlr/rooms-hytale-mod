package de.bsdlr.rooms.lib.room;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.lib.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.utils.PackedBox;
import de.bsdlr.rooms.utils.PositionUtils;

import java.util.*;

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

    public RoomManager() {
        rooms = new HashSet<>();
    }

    public Set<Room> getRooms() {
        return rooms;
    }

    public Room getRoom(Vector3d position) {
        Vector3i pos = PositionUtils.positionToVector3i(position);
        return getRoom(pos.x, pos.y, pos.z);
    }

    public Room getRoom(long key) {
        int x = PositionUtils.unpack3dX(key);
        int y = PositionUtils.unpack3dY(key);
        int z = PositionUtils.unpack3dZ(key);

        return getRoom(x, y, z);
    }

    public Room getRoom(int x, int y, int z) {
        for (Room room : rooms) {
            if (room.containsPos(x, y, z)) return room;
        }
        return null;
    }

    public void addRoom(Room room) {
        LOGGER.atInfo().log("Adding room: %s", room.roomTypeId);
        rooms.add(room);
    }

    public void removeRoom(Room room) {
        LOGGER.atInfo().log("Removing room: %s", room.roomTypeId);
        rooms.remove(room);
    }

    public void updateAround(World world, Vector3i target, Map<Long, BlockType> overrideBlocks) {
        Vector3i scanRadius = RoomsPlugin.get().getConfig().get().getScanRadius();

        LOGGER.atInfo().log("target block: %d %d %d", target.x, target.y, target.z);
        LOGGER.atInfo().log("scan radius : %d %d %d", scanRadius.x, scanRadius.y, scanRadius.z);

        for (Long key : overrideBlocks.keySet()) {
            BlockType type = overrideBlocks.get(key);
            int x = PositionUtils.unpack3dX(key);
            int y = PositionUtils.unpack3dY(key);
            int z = PositionUtils.unpack3dZ(key);
            for (PlayerRef playerRef : Universe.get().getPlayers()) {
                playerRef.sendMessage(Message.raw("override block at " + x + " " + y + " " + z + " with " + type.getId()));
            }
        }

        Set<Room> rooms = PositionUtils.forOffsetInRadius(scanRadius, target, (bx, by, bz) -> {
            long key = PositionUtils.pack3dPos(bx, by, bz);

            Room room = getRoom(key);

            try {
                Room detectedRoom = RoomDetector.getRoomAt(world, bx, by, bz, overrideBlocks);

                LOGGER.atInfo().log("detected room: %s (at %d %d %d)", detectedRoom, bx, by, bz);

                if (room != null) {
                    if (!room.equals(detectedRoom)) {
                        LOGGER.atInfo().log("Removing %s room at %d %d %d", room.getRoomTypeId(), bx, by, bz);
                        removeRoom(room);
                        return detectedRoom;
                    }
                } else {
                    return detectedRoom;
                }
            } catch (FailedToDetectRoomException e) {
//                LOGGER.atWarning().withCause(e).log(e.getMessage());
                if (room != null) {
                    removeRoom(room);
                }
            }

            return null;
        }, HashSet::new);

        LOGGER.atInfo().log("rooms: %d", rooms.size());

        for (Room room : rooms) {
            addRoom(room);
        }
    }
}
