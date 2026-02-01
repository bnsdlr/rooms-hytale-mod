package de.bsdlr.rooms.services.room;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.config.PluginConfig;
import de.bsdlr.rooms.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.exceptions.UnknownBlockException;
import de.bsdlr.rooms.exceptions.WorldChunkNullException;
import de.bsdlr.rooms.services.room.block.RoomBlock;
import de.bsdlr.rooms.services.room.block.RoomBlockRole;
import de.bsdlr.rooms.utils.ChunkManager;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RoomDetector {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static void placeBlocksVec(ChunkManager chunkManager, Collection<Vector3i> blocks, String blockId, String reBlockId) {
        for (Vector3i block : blocks) {
            chunkManager.world.setBlock(block.getX(), block.getY(), block.getZ(), blockId);
        }

        if (reBlockId != null) HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
            for (Vector3i block : blocks) {
                chunkManager.world.setBlock(block.getX(), block.getY(), block.getZ(), reBlockId);
            }
        }, 3, TimeUnit.SECONDS);
    }

    private static void placeBlocks(ChunkManager chunkManager, Collection<RoomBlock> roomBlocks, String blockId, String reBlockId) {
        for (RoomBlock roomBlock : roomBlocks) {
            chunkManager.world.setBlock(roomBlock.getX(), roomBlock.getY(), roomBlock.getZ(), blockId);
        }

        if (reBlockId != null) HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
            for (RoomBlock roomBlock : roomBlocks) {
                chunkManager.world.setBlock(roomBlock.getX(), roomBlock.getY(), roomBlock.getZ(), reBlockId);
            }
        }, 3, TimeUnit.SECONDS);
    }

    public static Room getRoomAt(World world, int posX, int posY, int posZ) throws FailedToDetectRoomException, WorldChunkNullException {
        if (posY > 320) return null;
        LOGGER.atInfo().log("x: " + posX);
        LOGGER.atInfo().log("y: " + posY);
        LOGGER.atInfo().log("z: " + posZ);

        ChunkManager chunkManager = new ChunkManager(world);

        PluginConfig config = RoomsPlugin.get().getConfig().get();
        LOGGER.atInfo().log("scan radius: " + config.getScanRadius());
        LOGGER.atInfo().log("min wall height: " + config.getRoomsConfig().getMinRoomHeight());
        LOGGER.atInfo().log("max room height: " + config.getRoomsConfig().getMaxRoomHeight());

        int floorY = detectFloor(chunkManager, config.getScanRadius(), posX, posY, posZ);
        LOGGER.atInfo().log("floor: " + floorY);
        if (posY - floorY > config.getRoomsConfig().getMaxRoomHeight()) return null;

        Room room = flood(chunkManager, config, posX, posY, posZ);

        if (room == null) return null;

//        placeBlocks(chunkManager, room.getFurnitures(), "Cloth_Block_Wool_Pink", "Furniture_Feran_Chest_Small");
//        placeBlocks(chunkManager, room.getEntrances(), "Cloth_Block_Wool_Green", "Furniture_Village_Door");
//        placeBlocks(chunkManager, room.getEmpty(), "Cloth_Block_Wool_Blue", "Empty");
//        placeBlocks(chunkManager, room.getSolidBlocks(), "Cloth_Block_Wool_Black", "Wood_Tropicalwood_Planks");
//        placeBlocks(chunkManager, room.getWindows(), "Cloth_Block_Wool_Orange", "Furniture_Village_Window");
//        placeBlocks(chunkManager, room.getLightSources(), "Cloth_Block_Wool_Yellow", "Furniture_Crude_Torch");

        return room;
    }

    private static Room flood(ChunkManager chunkManager, PluginConfig config, int x, int y, int z) throws WorldChunkNullException {
        Set<Vector3i> visited = new HashSet<>();
        Room.Builder builder = new Room.Builder();

        Deque<RoomBlock.BlockBuilder> queue = new ArrayDeque<>();

        Vector3i scanRadius = config.getScanRadius();

        if (!isInRoom(chunkManager, scanRadius, x, y, z))
            throw new FailedToDetectRoomException("Could not detect room: no walls detected at source position");

        RoomBlock.BlockBuilder start = chunkManager.getRoomBlockBuilderAt(x, y, z);
        if (start.setAndGetRole().isRoomWall()) {
            LOGGER.atWarning().log("Flood aborted: start position is in wall.");
            return null;
        }
        visited.add(start.getPos());
        queue.add(start);

//        placeBlocksVec(chunkManager, List.of(start.getPos()), "Rock_Bedrock", "Empty");

        int maxY = Math.min(319, y + scanRadius.y);
//        int maxY = y;
        int minY = y;

        final int fixedMinY = Math.max(0, y - scanRadius.y);
//        final int fixedMaxY = Math.min(319, y + config.getScanRadius());
        final int fixedMaxX = x + scanRadius.x;
        final int fixedMinX = x - scanRadius.x;
        final int fixedMaxZ = z + scanRadius.z;
        final int fixedMinZ = z - scanRadius.z;

        int counter = 0;
        while (!queue.isEmpty()) {
            if (++counter >= 1000) {
                LOGGER.atWarning().log("Flood aborted at %d", counter);
                return null;
            }

//            LOGGER.atInfo().log("counter: %d", counter);

            RoomBlock.BlockBuilder currentBlockBuilder = queue.poll();

            if (currentBlockBuilder.getY() < fixedMinY) continue;
            else if (currentBlockBuilder.getY() < minY) {
                minY = currentBlockBuilder.getY();
            }

            if (currentBlockBuilder.getY() > maxY) continue;

            RoomBlock currentRoomBlock = currentBlockBuilder.build();

            builder.addBlock(currentRoomBlock);

            for (Vector3i next : List.of(
                    new Vector3i(currentRoomBlock.getX(), currentRoomBlock.getY() - 1, currentRoomBlock.getZ()),
                    new Vector3i(currentRoomBlock.getX(), currentRoomBlock.getY() + 1, currentRoomBlock.getZ()),
                    new Vector3i(currentRoomBlock.getX() - 1, currentRoomBlock.getY(), currentRoomBlock.getZ()),
                    new Vector3i(currentRoomBlock.getX() + 1, currentRoomBlock.getY(), currentRoomBlock.getZ()),
                    new Vector3i(currentRoomBlock.getX(), currentRoomBlock.getY(), currentRoomBlock.getZ() - 1),
                    new Vector3i(currentRoomBlock.getX(), currentRoomBlock.getY(), currentRoomBlock.getZ() + 1)
            )) {
                if (visited.contains(next)) continue;
                if (next.y < fixedMinY || next.y > maxY || next.x < fixedMinX || next.x > fixedMaxX || next.z < fixedMinZ || next.z > fixedMaxZ)
                    continue;

                RoomBlock.BlockBuilder blockBuilder = chunkManager.getRoomBlockBuilderAt(next);

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
//                    if (!role.isRoomWall()) {
                        if (!isInRoom(chunkManager, scanRadius, next.x, next.y, next.z)) {
                            int newMaxY = next.y - 1;
                            if (newMaxY < maxY) {
                                maxY = newMaxY;
                                LOGGER.atInfo().log("new max y: %d", newMaxY);
                            }
                            LOGGER.atInfo().log("block is not in room: %d %d %d", next.x, next.y, next.z);
                        }
//                    }
                }

//                placeBlocksVec(chunkManager, List.of(next), "Rock_Bedrock", "Empty");

                if (visited.add(blockBuilder.getPos())) {
                    queue.add(blockBuilder);
                }
            }
        }

        int height = maxY - minY + 1;
        LOGGER.atInfo().log("miny: %d; maxy: %d; height: %d; counter: %d", minY, maxY, height, counter);

        if (height < config.getMinRoomHeight()) {
            LOGGER.atInfo().log("Room not heigh enough");
            return null;
        }

        int finalMaxY = maxY;
        builder.removeIf(block -> block.getY() > finalMaxY);

//        placeBlocks(chunkManager, builder.room.getEntrances(), "Cloth_Block_Wool_Green", "Furniture_Village_Door");
//        placeBlocks(chunkManager, builder.room.getEmpty(), "Cloth_Block_Wool_Blue", "Empty");
//        placeBlocks(chunkManager, builder.room.getSolidBlocks(), "Cloth_Block_Wool_Black", "Wood_Tropicalwood_Planks");
//        placeBlocks(chunkManager, builder.room.getFurnitures(), "Cloth_Block_Wool_Pink", "Furniture_Feran_Chest_Small");
//        placeBlocks(chunkManager, builder.room.getLightSources(), "Cloth_Block_Wool_Yellow", "Furniture_Crude_Torch");
//        placeBlocks(chunkManager, builder.room.getWindows(), "Cloth_Block_Wool_Orange", "Furniture_Village_Window");

        return builder.build();
    }

    private static boolean isInRoom(ChunkManager chunkManager, Vector3i scanRadius, int x, int y, int z) throws WorldChunkNullException {
        if (!detectRoomWall(chunkManager, scanRadius.x, x, y, z, 1, 0)) return false;
        if (!detectRoomWall(chunkManager, scanRadius.x, x, y, z, -1, 0)) return false;
        if (!detectRoomWall(chunkManager, scanRadius.z, x, y, z, 0, 1)) return false;
        return detectRoomWall(chunkManager, scanRadius.z, x, y, z, 0, -1);
    }

    private static boolean detectRoomWall(ChunkManager chunkManager, int scanRadius, int x, int y, int z, int offSetX, int offSetZ) throws WorldChunkNullException {
        int chunkX = ChunkUtil.chunkCoordinate(x);
        int chunkZ = ChunkUtil.chunkCoordinate(z);
        WorldChunk chunk = chunkManager.getChunk(chunkX, chunkZ);

        for (int delta = 0; delta < scanRadius; delta++) {
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

    private static int detectFloor(ChunkManager chunkManager, Vector3i scanRadius, int x, int y, int z) throws WorldChunkNullException {
        WorldChunk chunk = chunkManager.getChunkFromBlock(x, z);
        if (chunk == null) throw new WorldChunkNullException(x, z, true);

        int dy = -1;
        while (dy < scanRadius.y) {
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
