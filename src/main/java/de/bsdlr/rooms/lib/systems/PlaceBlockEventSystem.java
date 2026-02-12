package de.bsdlr.rooms.lib.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.utils.PositionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PlaceBlockEventSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public PlaceBlockEventSystem() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PlaceBlockEvent event) {
        Vector3i target = event.getTargetBlock();

        if (event.getItemInHand() == null) {
            LOGGER.atWarning().log("Item in hand is null.");
            return;
        }

        BlockType type = BlockType.getAssetMap().getAsset(event.getItemInHand().getBlockKey());

        if (type == null) {
            LOGGER.atWarning().log("Block placed type is null.");
            return;
        }

        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            playerRef.sendMessage(Message.raw("Placed block: " + type.getId()));
        }

        LOGGER.atInfo().log("target: %d %d %d", target.x, target.y, target.z);
        LOGGER.atInfo().log("placed %s block", type.getId());

        Map<Long, BlockType> overrideBlocks = new HashMap<>();
        long key = PositionUtils.pack3dPos(target);
        overrideBlocks.put(key, type);

        LOGGER.atInfo().log("decoded block pos: %d %d %d", PositionUtils.unpack3dX(key), PositionUtils.unpack3dY(key), PositionUtils.unpack3dZ(key));

        for (Map.Entry<Long, BlockType> entry : overrideBlocks.entrySet()) {
            long k = entry.getKey();
            BlockType btype = entry.getValue();
            int decodedX = PositionUtils.unpack3dX(k);
            int decodedY = PositionUtils.unpack3dY(k);
            int decodedZ = PositionUtils.unpack3dZ(k);
            LOGGER.atInfo().log("%d %d %d %s", decodedX, decodedY, decodedZ, btype.getId());
        }

        World world = commandBuffer.getExternalData().getWorld();
//        RoomDetector.setSilent(true);
        RoomsPlugin.get()
                .getRoomManagerAndComputeIfAbsent(world.getWorldConfig().getUuid())
                .updateAround(world, target, overrideBlocks);
//        RoomDetector.restoreSilent();
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
