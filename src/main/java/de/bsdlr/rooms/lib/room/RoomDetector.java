package de.bsdlr.rooms.lib.room;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.config.PluginConfig;
import de.bsdlr.rooms.lib.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.lib.exceptions.RoomValidationException;
import de.bsdlr.rooms.lib.exceptions.UnknownBlockException;
import de.bsdlr.rooms.lib.exceptions.WorldChunkNullException;
import de.bsdlr.rooms.lib.room.block.RoomBlock;
import de.bsdlr.rooms.lib.room.block.RoomBlockRole;
import de.bsdlr.rooms.utils.ChunkManager;
import de.bsdlr.rooms.utils.PositionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RoomDetector {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static Room validate(Room room, Vector3i pos, long key) throws RoomValidationException, WorldChunkNullException, ExecutionException, InterruptedException {
        World world = Universe.get().getWorld(room.getWorldUuid());
        if (world == null)
            throw new RoomValidationException("Couldn't validate room: no world with uuid: " + room.getWorldUuid());

        CompletableFuture<Room> future = new CompletableFuture<>();

        LOGGER.atInfo().log("Executing on world thread.");
        if (room.getBlocks().contains(key)) {
            world.execute(() -> {
                try {
                    Room r = getRoomAt(world, pos.x, pos.y, pos.z);
                    future.complete(r);
                } catch (FailedToDetectRoomException e) {
                    LOGGER.atSevere().withCause(e).log("Failed to validate room: %s", e.getMessage());
                    future.complete(null);
                }
            });
        } else {
            world.execute(() -> {
                ChunkManager chunkManager = new ChunkManager(world);

                for (Long k : room.getBlocks()) {
                    int bx = PositionUtils.decodeX(k);
                    int by = PositionUtils.decodeY(k);
                    int bz = PositionUtils.decodeZ(k);
                    BlockType type = chunkManager.getBlockTypeAt(bx, by, bz);
                    if (!RoomBlockRole.isRoomWall(type)) {
                        future.complete(getRoomAt(world, bx, by, bz));
                    }
                }
            });
        }

        LOGGER.atInfo().log("Waiting for execution to finish.");
        Room result = future.get();
        LOGGER.atInfo().log("Got room: %s", result);
        return result;
    }

    public static Set<Room> detectRooms(World world, int x, int y, int z) throws WorldChunkNullException {
        Vector3i radius = RoomsPlugin.get().getConfig().get().getScanRadius();

        return PositionUtils.forOffsetInRadius(radius, (dx, dy, dz) -> {
            try {
                int bx = dx + x;
                int by = dy + y;
                int bz = dz + z;
//                        ctx.sendMessage(Message.raw("pos: " + bx + " " + by + " " + bz));
//                        world.setBlock(bx, by, bz, "Rock_Stone");
                return getRoomAt(world, bx, by, bz);
            } catch (FailedToDetectRoomException e) {
                LOGGER.atWarning().withCause(e).log(e.getMessage());
                return null;
            }
        }, HashSet::new);
    }

    public static Room getRoomAt(World world, int posX, int posY, int posZ) throws FailedToDetectRoomException, WorldChunkNullException {
        if (posY > 320) return null;
//        LOGGER.atWarning().log("get room at: %d %d %d", posX, posY, posZ);

        ChunkManager chunkManager = new ChunkManager(world);

        PluginConfig config = RoomsPlugin.get().getConfig().get();
//        LOGGER.atInfo().log("bound scan radius: " + config.getBoundScanRadius());
//        LOGGER.atInfo().log("min wall height: " + config.getRoomsConfig().getMinRoomHeight());
//        LOGGER.atInfo().log("max room height: " + config.getRoomsConfig().getMaxRoomHeight());

        int floorY = detectFloor(chunkManager, config.getBoundScanRadius(), posX, posY, posZ);
//        LOGGER.atInfo().log("floor: " + floorY);
        if (posY - floorY > config.getRoomsConfig().getMaxRoomHeight()) return null;

        return flood(chunkManager, config, posX, posY, posZ);
    }

    private static Room flood(ChunkManager chunkManager, PluginConfig config, int x, int y, int z) throws FailedToDetectRoomException, WorldChunkNullException {
//        LOGGER.atInfo().log("Start flood (%d %d %d).", x, y, z);
        Set<Vector3i> visited = new HashSet<>();
        Room.Builder builder = new Room.Builder();

        Deque<RoomBlock.Builder> queue = new ArrayDeque<>();

        Vector3i boundScanRadius = config.getBoundScanRadius();

        if (!isInRoom(chunkManager, boundScanRadius, x, y, z))
            throw new FailedToDetectRoomException("Could not detect room: no walls detected at source position");

        RoomBlock.Builder start = chunkManager.getRoomBlockBuilderAt(x, y, z);
        if (start.setAndGetRole().isRoomWall())
            throw new FailedToDetectRoomException("Flood aborted: start position is in wall.");

        visited.add(start.getPos());
        queue.add(start);

        int maxY = Math.min(319, y + boundScanRadius.y);
        int minY = y;

        final int fixedMinY = Math.max(0, y - boundScanRadius.y);
        final int fixedMaxX = x + boundScanRadius.x;
        final int fixedMinX = x - boundScanRadius.x;
        final int fixedMaxZ = z + boundScanRadius.z;
        final int fixedMinZ = z - boundScanRadius.z;

        int counter = 0;
        while (!queue.isEmpty()) {
            if (++counter >= 1000) throw new FailedToDetectRoomException("Flood aborted at " + counter);

//            LOGGER.atInfo().log("counter: %d", counter);

            RoomBlock.Builder currentBuilder = queue.poll();

            if (currentBuilder.getY() < fixedMinY) continue;
            else if (currentBuilder.getY() < minY) {
                minY = currentBuilder.getY();
            }

            if (currentBuilder.getY() > maxY) continue;

            RoomBlock currentRoomBlock = currentBuilder.build();

            builder.addBlock(currentRoomBlock);

            for (Vector3i next : List.of(
                    new Vector3i(currentRoomBlock.getX(), currentRoomBlock.getY() - 1, currentRoomBlock.getZ()),
                    new Vector3i(currentRoomBlock.getX(), currentRoomBlock.getY() + 1, currentRoomBlock.getZ()),
                    new Vector3i(currentRoomBlock.getX() - 1, currentRoomBlock.getY(), currentRoomBlock.getZ()),
                    new Vector3i(currentRoomBlock.getX() + 1, currentRoomBlock.getY(), currentRoomBlock.getZ()),
                    new Vector3i(currentRoomBlock.getX(), currentRoomBlock.getY(), currentRoomBlock.getZ() - 1),
                    new Vector3i(currentRoomBlock.getX(), currentRoomBlock.getY(), currentRoomBlock.getZ() + 1)
//                    new Vector3i(0, -1, 0),
//                    new Vector3i(0,  1, 0),
//                    new Vector3i(-1, 0, 0),
//                    new Vector3i( 1, 0, 0),
//                    new Vector3i(0, 0, -1),
//                    new Vector3i(0, 0,  1)
            )) {
//                Vector3i next = new Vector3i(offset.x + currentRoomBlock.getX(), offset.y + currentRoomBlock.getY(), offset.z + currentRoomBlock.getZ());
                if (visited.contains(next)) continue;
                if (next.y < fixedMinY || next.y > maxY || next.x < fixedMinX || next.x > fixedMaxX || next.z < fixedMinZ || next.z > fixedMaxZ)
                    continue;

                RoomBlock.Builder blockBuilder = chunkManager.getRoomBlockBuilderAt(next);

                BlockType type = blockBuilder.setAndGetBlockType();
                if (type == null) {
                    LOGGER.atWarning().log("RoomBlock at %d %d %d is null!", next.x, next.y, next.z);
                    continue;
                }

                RoomBlockRole role = blockBuilder.setAndGetRole();

//                LOGGER.atInfo().log("(%d) next: %d %d %d; role: %s; wall: %s", counter, next.x, next.y, next.z, role, role.isRoomWall());
                if (role.isRoomWall()) {
                    visited.add(blockBuilder.getPos());
                    builder.addBlock(blockBuilder.build());
                    continue;
                }

                if (next.y != currentRoomBlock.getY()) {
                    if (!isInRoom(chunkManager, boundScanRadius, next.x, next.y, next.z)) {
                        int newMaxY = next.y - 1;
                        if (newMaxY < maxY) {
                            maxY = newMaxY;
//                            LOGGER.atInfo().log("new max y: %d", newMaxY);
                        }
//                        LOGGER.atInfo().log("block is not in room: %d %d %d", next.x, next.y, next.z);
                    }
                }

                if (visited.add(blockBuilder.getPos())) {
                    queue.add(blockBuilder);
                }
            }
        }

        int height = maxY - minY + 1;
//        LOGGER.atInfo().log("miny: %d; maxy: %d; height: %d; counter: %d", minY, maxY, height, counter);

        if (height < config.getMinRoomHeight())
            throw new FailedToDetectRoomException("Room not heigh enough (min: " + config.getMinRoomHeight() + ", actual: " + height + ").");

        int finalMaxY = maxY;
        builder.removeIf(block -> block.getY() > finalMaxY);

        return builder.build();
    }

    private static boolean isInRoom(ChunkManager chunkManager, Vector3i boundScanRadius, int x, int y, int z) throws WorldChunkNullException {
        if (!detectRoomWall(chunkManager, boundScanRadius.x, x, y, z, 1, 0)) return false;
        if (!detectRoomWall(chunkManager, boundScanRadius.x, x, y, z, -1, 0)) return false;
        if (!detectRoomWall(chunkManager, boundScanRadius.z, x, y, z, 0, 1)) return false;
        return detectRoomWall(chunkManager, boundScanRadius.z, x, y, z, 0, -1);
    }

    private static boolean detectRoomWall(ChunkManager chunkManager, int boundScanRadius, int x, int y, int z, int offSetX, int offSetZ) throws WorldChunkNullException {
        int chunkX = ChunkUtil.chunkCoordinate(x);
        int chunkZ = ChunkUtil.chunkCoordinate(z);
        WorldChunk chunk = chunkManager.getChunk(chunkX, chunkZ);

        for (int delta = 0; delta < boundScanRadius; delta++) {
            int bx = x + (offSetX * delta);
            int bz = z + (offSetZ * delta);

            int currentChunkX = ChunkUtil.chunkCoordinate(bx);
            int currentChunkZ = ChunkUtil.chunkCoordinate(bz);

            if (currentChunkX != chunkX || currentChunkZ != chunkZ) {
                chunkX = currentChunkX;
                chunkZ = currentChunkZ;
                chunk = chunkManager.getChunk(chunkX, chunkZ);
            }

            if (chunk == null) throw new WorldChunkNullException(currentChunkX, currentChunkZ);

            int blockId = chunk.getBlock(bx, y, bz);
            if (blockId == 0) continue;

            BlockType type = BlockType.getAssetMap().getAsset(blockId);
            if (type == null) throw new UnknownBlockException(blockId);

            if (RoomBlockRole.isRoomWall(type)) return true;
        }

        return false;
    }

    private static int detectFloor(ChunkManager chunkManager, Vector3i boundScanRadius, int x, int y, int z) throws WorldChunkNullException {
        WorldChunk chunk = chunkManager.getChunkFromBlock(x, z);
        if (chunk == null) throw new WorldChunkNullException(x, z, true);

        int dy = -1;
        while (dy < boundScanRadius.y) {
            dy++;
            int by = y - dy;

            if (by < 0) return -1;

            int blockId = chunk.getBlock(x, by, z);

            if (blockId == 0) continue;

            BlockType type = BlockType.getAssetMap().getAsset(blockId);

            if (type == null) continue;

            if (RoomBlockRole.isSolidBlock(type)) {
                return by;
            }
        }

        return -1;
    }
}
