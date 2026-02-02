package de.bsdlr.rooms.lib.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.lib.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.lib.room.Room;
import de.bsdlr.rooms.lib.room.RoomDetector;
import de.bsdlr.rooms.lib.room.RoomManager;
import de.bsdlr.rooms.utils.PositionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class BreakBlockEventSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public BreakBlockEventSystem() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull BreakBlockEvent event) {
        World world = commandBuffer.getExternalData().getWorld();
        Vector3i target = event.getTargetBlock();

        RoomManager roomManager = RoomsPlugin.get().getRoomManager();
        Vector3i scanRadius = RoomsPlugin.get().getConfig().get().getScanRadius();

        LOGGER.atInfo().log("target block: %d %d %d", target.x, target.y, target.z);
        LOGGER.atInfo().log("scan radius : %d %d %d", scanRadius.x, scanRadius.y, scanRadius.z);

        Set<Room> rooms = PositionUtils.forOffsetInRadius(scanRadius, (dx, dy, dz) -> {
            int bx = target.x + dx;
            int by = target.y + dy;
            int bz = target.z + dz;
            long key = PositionUtils.encodePosition(bx, by, bz);

            try {
                Room room = roomManager.getRoom(key);
                Room detectedRoom = RoomDetector.getRoomAt(world, bx, by, bz);

                if (room != null) {
                    if (!room.equals(detectedRoom)) {
                        LOGGER.atInfo().log("Removing %s room at %d %d %d", room.getId(), bx, by, bz);
                        roomManager.removeRoom(room);
                        return detectedRoom;
                    }
                } else {
                    return detectedRoom;
                }
            } catch (FailedToDetectRoomException e) {
//                LOGGER.atWarning().withCause(e).log(e.getMessage());
            }

            return null;
        }, HashSet::new);

        for (Room room : rooms) {
            roomManager.addRoom(room);
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
