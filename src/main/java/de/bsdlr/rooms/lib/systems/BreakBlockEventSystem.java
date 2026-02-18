package de.bsdlr.rooms.lib.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.lib.exceptions.WorldChunkNullException;
import de.bsdlr.rooms.lib.room.RoomDetector;
import de.bsdlr.rooms.utils.BlockUtils;
import de.bsdlr.rooms.utils.PositionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BreakBlockEventSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public BreakBlockEventSystem() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull BreakBlockEvent event) {
        World world = commandBuffer.getExternalData().getWorld();
        Vector3i target = event.getTargetBlock();

        if (event.getBlockType().getId().equals(BlockType.EMPTY_KEY)) {
            return;
        }

        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            playerRef.sendMessage(Message.raw("Broke block: " + event.getBlockType().getId()));
        }

        Map<Long, BlockType> overrideBlocks = new HashMap<>();

        // TODO: get room block and set filler blocks if needed
        for (Vector3i pos : BlockUtils.getAllOccupiedPositions(world, event.getBlockType(), target)) {
            long key = PositionUtils.pack3dPos(pos);
            overrideBlocks.put(key, BlockType.EMPTY);
            LOGGER.atInfo().log("override block pos: %d %d %d with Empty", PositionUtils.unpack3dX(key), PositionUtils.unpack3dY(key), PositionUtils.unpack3dZ(key));
        }

        RoomDetector.setSilent(true);
        RoomsPlugin.get().getRoomManagerAndComputeIfAbsent(world.getWorldConfig().getUuid()).updateAround(world, target, overrideBlocks);
        RoomDetector.restoreSilent();

//        Vector3i scanRadius = RoomsPlugin.get().getConfig().get().getScanRadius();
//
//        LOGGER.atInfo().log("target block: %d %d %d", target.x, target.y, target.z);
//        LOGGER.atInfo().log("scan radius : %d %d %d", scanRadius.x, scanRadius.y, scanRadius.z);
//
//        Set<Room> rooms = PositionUtils.forOffsetInRadius(scanRadius, (dx, dy, dz) -> {
//            int bx = target.x + dx;
//            int by = target.y + dy;
//            int bz = target.z + dz;
//            long key = PositionUtils.encodePosition(bx, by, bz);
//
//            Room room = roomManager.getRoom(key);
//
//            try {
//                RoomDetector.setSilent(true);
//                Room detectedRoom = RoomDetector.getRoomAt(world, bx, by, bz, overrideBlocks);
//                RoomDetector.restoreSilent();
//
//                LOGGER.atInfo().log("detected room: %s", detectedRoom == null ? null : detectedRoom.getBlocks().size());
//
//                if (room != null) {
//                    if (!room.equals(detectedRoom)) {
//                        LOGGER.atInfo().log("Removing %s room at %d %d %d", room.getId(), bx, by, bz);
//                        roomManager.removeRoom(room);
//                        return detectedRoom;
//                    }
//                } else {
//                    return detectedRoom;
//                }
//            } catch (FailedToDetectRoomException e) {
////                LOGGER.atWarning().withCause(e).log(e.getMessage());
//                if (room != null) {
//                    roomManager.removeRoom(room);
//                }
//            }
//
//            return null;
//        }, HashSet::new);
//
//        LOGGER.atInfo().log("rooms: %d", rooms.size());
//
//        for (Room room : rooms) {
//            roomManager.addRoom(room);
//        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
