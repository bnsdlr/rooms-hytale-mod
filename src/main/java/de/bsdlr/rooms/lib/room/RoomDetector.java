package de.bsdlr.rooms.lib.room;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
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
import de.bsdlr.rooms.utils.BlockUtils;
import de.bsdlr.rooms.utils.ChunkManager;
import de.bsdlr.rooms.utils.PositionUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RoomDetector {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static boolean silent = false;
    private static Boolean last;

    public static void setSilent(boolean s) {
        if (last == null) {
            last = silent;
        }
        silent = s;
    }

    public static void restoreSilent() {
        silent = last;
        last = null;
    }

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
                    LOGGER.atWarning().withCause(e).log("Failed to validate room: %s", e.getMessage());
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

    public static Room getRoomAt(World world, int x, int y, int z) throws FailedToDetectRoomException, WorldChunkNullException {
        return getRoomAt(world, x, y, z, null);
    }

    public static Room getRoomAt(World world, int x, int y, int z, Map<Long, BlockType> overrideBlocks) throws FailedToDetectRoomException, WorldChunkNullException {
        if (y > 320) return null;
//        LOGGER.atWarning().log("get room at: %d %d %d", x, y, z);

        ChunkManager chunkManager = new ChunkManager(world);

        PluginConfig config = RoomsPlugin.get().getConfig().get();
//        LOGGER.atInfo().log("bound scan radius: " + config.getBoundScanRadius());
//        LOGGER.atInfo().log("min wall height: " + config.getRoomsConfig().getMinRoomHeight());
//        LOGGER.atInfo().log("max room height: " + config.getRoomsConfig().getMaxRoomHeight());

        int floorY = detectFloor(chunkManager, config.getBoundScanRadius(), x, y, z, overrideBlocks);
//        LOGGER.atInfo().log("floor: " + floorY);
        if (y - floorY > config.getRoomsConfig().getMaxRoomHeight()) return null;

        return flood(chunkManager, config, x, y, z, overrideBlocks);
    }

    private static Room flood(ChunkManager chunkManager, PluginConfig config, int x, int y, int z, Map<Long, BlockType> overrideBlocks) throws FailedToDetectRoomException, WorldChunkNullException {
//        LOGGER.atInfo().log("Start flood (%d %d %d).", x, y, z);
        Set<Vector3i> visited = new HashSet<>();
        Room.Builder builder = new Room.Builder();

        Deque<RoomBlock> queue = new ArrayDeque<>();

        Vector3i boundScanRadius = config.getBoundScanRadius();

        if (!isInRoom(chunkManager, boundScanRadius, x, y, z, overrideBlocks))
            throw new FailedToDetectRoomException("Could not detect room: no walls detected at source position");

        RoomBlock start = chunkManager.getRoomBlockAt(x, y, z);
        if (start.getRole().isRoomWall())
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
            if (++counter >= 1000) {
                if (!silent) LOGGER.atWarning().log("Flood aborted at " + counter);
                return null;
            }

//            LOGGER.atInfo().log("counter: %d", counter);

            RoomBlock currentBlock = queue.poll();

            if (currentBlock.getY() < fixedMinY) continue;
            else if (currentBlock.getY() < minY) {
                minY = currentBlock.getY();
            }

            if (currentBlock.getY() > maxY) continue;

            builder.addBlock(currentBlock);

            for (Vector3i next : List.of(
                    new Vector3i(currentBlock.getX(), currentBlock.getY() - 1, currentBlock.getZ()),
                    new Vector3i(currentBlock.getX(), currentBlock.getY() + 1, currentBlock.getZ()),
                    new Vector3i(currentBlock.getX() - 1, currentBlock.getY(), currentBlock.getZ()),
                    new Vector3i(currentBlock.getX() + 1, currentBlock.getY(), currentBlock.getZ()),
                    new Vector3i(currentBlock.getX(), currentBlock.getY(), currentBlock.getZ() - 1),
                    new Vector3i(currentBlock.getX(), currentBlock.getY(), currentBlock.getZ() + 1)
//                    new Vector3i(0, -1, 0),
//                    new Vector3i(0,  1, 0),
//                    new Vector3i(-1, 0, 0),
//                    new Vector3i( 1, 0, 0),
//                    new Vector3i(0, 0, -1),
//                    new Vector3i(0, 0,  1)
            )) {
//                Vector3i next = new Vector3i(offset.x + currentBlock.getX(), offset.y + currentBlock.getY(), offset.z + currentBlock.getZ());
                if (visited.contains(next)) continue;
                if (next.y < fixedMinY || next.y > maxY || next.x < fixedMinX || next.x > fixedMaxX || next.z < fixedMinZ || next.z > fixedMaxZ)
                    continue;

                RoomBlock block = null;
                boolean set = false;

                if (overrideBlocks != null) {
                    long key = PositionUtils.encodePosition(next);
                    if (overrideBlocks.containsKey(key)) {
                        block = new RoomBlock(overrideBlocks.get(key), next, chunkManager.world.getChunkStore());
                        set = true;
                    }
                }

                if (!set) {
                    block = chunkManager.getRoomBlockBuilderAt(next).setFiller(chunkManager.world.getChunkStore()).build();
                }

                if (block.getType().isUnknown()) {
                    LOGGER.atWarning().log("RoomBlock at %d %d %d is unknown!", next.x, next.y, next.z);
                    continue;
                }

                RoomBlockRole role = block.getRole();

//                LOGGER.atInfo().log("(%d) next: %d %d %d; role: %s; wall: %s", counter, next.x, next.y, next.z, role, role.isRoomWall());
                if (role.isRoomWall()) {
//                    if (blockBuilder.isFiller()) {
//                        addFillerBlocks(chunkManager, visited, builder, blockBuilder);
//                    }

                    if (visited.add(block.getPos())) {
                        builder.addBlock(block);
                    }
                    continue;
                }

                if (next.y != currentBlock.getY()) {
                    if (!isInRoom(chunkManager, boundScanRadius, next.x, next.y, next.z, overrideBlocks)) {
                        int newMaxY = next.y - 1;
                        if (newMaxY < maxY) {
                            maxY = newMaxY;
//                            LOGGER.atInfo().log("new max y: %d", newMaxY);
                        }
//                        LOGGER.atInfo().log("block is not in room: %d %d %d", next.x, next.y, next.z);
                    }
                }

                if (visited.add(block.getPos())) {
                    queue.add(block);
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

//    private static void addFillerBlocks(@Nonnull ChunkManager chunkManager, @Nonnull Set<Vector3i> visited, @Nonnull Room.Builder builder, @Nonnull RoomBlock.Builder blockBuilder) {
//        Vector3i rootPos = BlockUtils.getRoot(blockBuilder.getFiller(), blockBuilder.getPos());
//        BlockType type = blockBuilder.getAndSetBlockTypeIfNull();
//
//        if (type == null) {
//            LOGGER.atWarning().log("BlockType is null!");
//            return;
//        }
//
//        BlockBoundingBoxes bbb = BlockBoundingBoxes.getAssetMap().getAsset(type.getHitboxTypeIndex());
//        if (bbb == null) return;
//
////        int rotationIndex = chunkManager.world.getBlockRotationIndex(rootPos.x, rootPos.y, rootPos.z);
//        LOGGER.atInfo().log("-----------------------------------------");
//        CompletableFuture<Integer> future = new CompletableFuture<>();
//
//        chunkManager.world.execute(() -> {
//            LOGGER.atInfo().log("waiting for rotation index");
//            int index = chunkManager.getChunkAccessorFromBlock(rootPos.x, rootPos.z).getBlockRotationIndex(rootPos.x, rootPos.y, rootPos.z);
//            LOGGER.atInfo().log("got rotation index, completing future");
//            future.complete(index);
//        });
//
//        try {
//            LOGGER.atInfo().log("getting rotation index...");
//            Integer rotationIndex = future.get();
//            BlockBoundingBoxes.RotatedVariantBoxes rotatedHitbox = bbb.get(rotationIndex);
//            Box bb = rotatedHitbox.getBoundingBox();
//            LOGGER.atInfo().log("bounding box: %s", bb);
//        } catch (Exception e) {
//            LOGGER.atInfo().withCause(e).log();
//        }
//
//        LOGGER.atInfo().log("-----------------------------------------");
//
////        int rotationIndex = chunkManager.getChunkAccessorFromBlock(rootPos.x, rootPos.z).getBlockRotationIndex(rootPos.x, rootPos.y, rootPos.z);
//

    /// /        LOGGER.atInfo().log("filler block pos: %d %d %d", blockBuilder.getX(), blockBuilder.getY(), blockBuilder.getZ());
    /// /        LOGGER.atInfo().log("root block pos: %d %d %d", rootPos.x, rootPos.y, rootPos.z);
    /// /        int blockId = chunkManager.getBlockIdAt(rootPos);
    /// /        RoomBlock.Builder rootBlockBuilder = new RoomBlock.Builder(blockId, rootPos)
    /// /                .setFiller(new Vector3i(0, 0, 0));
    /// /        RoomBlockRole r = rootBlockBuilder.setAndGetRole();
    /// /        LOGGER.atInfo().log("room block role: %s", r);
    /// /
    /// /        if (visited.add(rootPos)) {
    /// /            builder.addBlock(rootBlockBuilder.build());
    /// /        }
//    }
    private static boolean isInRoom(ChunkManager chunkManager, Vector3i boundScanRadius, int x, int y, int z, Map<Long, BlockType> overrideBlocks) throws WorldChunkNullException {
        if (!detectRoomWall(chunkManager, boundScanRadius.x, x, y, z, 1, 0, overrideBlocks)) return false;
        if (!detectRoomWall(chunkManager, boundScanRadius.x, x, y, z, -1, 0, overrideBlocks)) return false;
        if (!detectRoomWall(chunkManager, boundScanRadius.z, x, y, z, 0, 1, overrideBlocks)) return false;
        return detectRoomWall(chunkManager, boundScanRadius.z, x, y, z, 0, -1, overrideBlocks);
    }

    private static boolean detectRoomWall(ChunkManager chunkManager, int boundScanRadius, int x, int y, int z, int offSetX, int offSetZ, Map<Long, BlockType> overrideBlocks) throws WorldChunkNullException {
        if (offSetX == 0 && offSetZ == 0)
            throw new RuntimeException("at least one of offSetX and offSetZ should not be 0.");
        int chunkX = ChunkUtil.chunkCoordinate(x);
        int chunkZ = ChunkUtil.chunkCoordinate(z);
        WorldChunk chunk = chunkManager.getChunk(chunkX, chunkZ);

        for (int delta = 0; delta < boundScanRadius; delta++) {
            int bx = x + (offSetX * delta);
            int bz = z + (offSetZ * delta);

            BlockType type = null;
            boolean set = false;

            if (overrideBlocks != null) {
                long key = PositionUtils.encodePosition(bx, y, bz);
                type = overrideBlocks.get(key);
                set = type != null;
            }

            if (!set) {
                int currentChunkX = ChunkUtil.chunkCoordinate(bx);
                int currentChunkZ = ChunkUtil.chunkCoordinate(bz);

                if (currentChunkX != chunkX || currentChunkZ != chunkZ) {
                    chunkX = currentChunkX;
                    chunkZ = currentChunkZ;
                    chunk = chunkManager.getChunk(chunkX, chunkZ);
                }

                if (chunk == null) throw new WorldChunkNullException(chunkX, chunkZ);

                int blockId = chunk.getBlock(bx, y, bz);
                if (blockId == BlockType.EMPTY_ID) continue;

                type = BlockType.getAssetMap().getAsset(blockId);
            }

            if (type == null) throw new UnknownBlockException("Unknown block!!");

            if (RoomBlockRole.isRoomWall(type)) return true;
        }

        return false;
    }

    private static int detectFloor(ChunkManager chunkManager, Vector3i boundScanRadius, int x, int y, int z, Map<Long, BlockType> overrideBlocks) throws WorldChunkNullException {
        WorldChunk chunk = chunkManager.getChunkFromBlock(x, z);
        if (chunk == null) throw new WorldChunkNullException(x, z, true);

        int dy = -1;
        while (dy < boundScanRadius.y) {
            dy++;
            int by = y - dy;

            if (by < 0) return -1;

            BlockType type = null;
            boolean set = false;

            if (overrideBlocks != null) {
                long key = PositionUtils.encodePosition(x, by, z);
                type = overrideBlocks.get(key);
                set = type != null;
            }

            if (!set) {
                int blockId = chunk.getBlock(x, by, z);

                if (blockId == BlockType.EMPTY_ID) continue;

                type = BlockType.getAssetMap().getAsset(blockId);
            }

            if (type == null) continue;

            if (RoomBlockRole.isSolidBlock(type)) {
                return by;
            }
        }

        return -1;
    }
}
