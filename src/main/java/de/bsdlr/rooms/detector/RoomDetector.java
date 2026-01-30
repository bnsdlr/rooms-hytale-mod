package de.bsdlr.rooms.detector;

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
import de.bsdlr.rooms.room.Room;
import de.bsdlr.rooms.room.block.Block;
import de.bsdlr.rooms.room.block.BlockRole;
import de.bsdlr.rooms.util.ChunkManager;

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

    private static void placeBlocks(ChunkManager chunkManager, Collection<Block> blocks, String blockId, String reBlockId) {
        for (Block block : blocks) {
            chunkManager.world.setBlock(block.getX(), block.getY(), block.getZ(), blockId);
        }

        if (reBlockId != null) HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
            for (Block block : blocks) {
                chunkManager.world.setBlock(block.getX(), block.getY(), block.getZ(), reBlockId);
            }
        }, 3, TimeUnit.SECONDS);
    }

    public static Room getRoomAt(World world, int posX, int posY, int posZ) throws FailedToDetectRoomException, WorldChunkNullException {
        if (posY > 320) return null;
        LOGGER.atInfo().log("x: " + posX);
        LOGGER.atInfo().log("y: " + posY);
        LOGGER.atInfo().log("z: " + posZ);

        ChunkManager chunkManager = new ChunkManager(world);

        PluginConfig config = RoomsPlugin.getInstance().getConfig().get();
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
        Room.RoomBuilder builder = new Room.RoomBuilder();

        Deque<Block.BlockBuilder> queue = new ArrayDeque<>();

        if (!isInRoom(chunkManager, config, x, y, z))
            throw new FailedToDetectRoomException("Could not detect room: no walls detected at source position");

        Block.BlockBuilder start = chunkManager.getBlockBuilderAt(x, y, z);
        if (start.setAndGetRole().isRoomWall()) {
            LOGGER.atWarning().log("Flood aborted: start position is in wall.");
            return null;
        }
        visited.add(start.getPos());
        queue.add(start);

//        placeBlocksVec(chunkManager, List.of(start.getPos()), "Rock_Bedrock", "Empty");

        int maxY = Math.min(319, y + config.getScanRadius());
//        int maxY = y;
        int minY = y;

        final int fixedMinY = Math.max(0, y - config.getScanRadius());
//        final int fixedMaxY = Math.min(319, y + config.getScanRadius());
        final int fixedMaxX = x + config.getScanRadius();
        final int fixedMinX = x - config.getScanRadius();
        final int fixedMaxZ = z + config.getScanRadius();
        final int fixedMinZ = z - config.getScanRadius();

        int counter = 0;
        while (!queue.isEmpty()) {
            if (++counter >= 1000) {
                LOGGER.atWarning().log("Flood aborted at %d", counter);
                return null;
            }

//            LOGGER.atInfo().log("counter: %d", counter);

            Block.BlockBuilder currentBlockBuilder = queue.poll();

            if (currentBlockBuilder.getY() < fixedMinY) continue;
            else if (currentBlockBuilder.getY() < minY) {
                minY = currentBlockBuilder.getY();
            }

            if (currentBlockBuilder.getY() > maxY) continue;

            Block currentBlock = currentBlockBuilder.build();

            builder.addBlock(currentBlock);

            for (Vector3i next : List.of(
                    new Vector3i(currentBlock.getX(), currentBlock.getY() - 1, currentBlock.getZ()),
                    new Vector3i(currentBlock.getX(), currentBlock.getY() + 1, currentBlock.getZ()),
                    new Vector3i(currentBlock.getX() - 1, currentBlock.getY(), currentBlock.getZ()),
                    new Vector3i(currentBlock.getX() + 1, currentBlock.getY(), currentBlock.getZ()),
                    new Vector3i(currentBlock.getX(), currentBlock.getY(), currentBlock.getZ() - 1),
                    new Vector3i(currentBlock.getX(), currentBlock.getY(), currentBlock.getZ() + 1)
            )) {
                if (visited.contains(next)) continue;
                if (next.y < fixedMinY || next.y > maxY || next.x < fixedMinX || next.x > fixedMaxX || next.z < fixedMinZ || next.z > fixedMaxZ)
                    continue;

                Block.BlockBuilder blockBuilder = chunkManager.getBlockBuilderAt(next);

                BlockType type = blockBuilder.setAndGetBlockType();
                if (type == null) {
                    LOGGER.atWarning().log("Block at %d %d %d is null!", next.x, next.y, next.z);
                    continue;
                }

                BlockRole role = blockBuilder.setAndGetRole();

//                LOGGER.atInfo().log("(%d) next: %d %d %d; role: %s; wall: %s", counter, next.x, next.y, next.z, role, role.isRoomWall());
                if (role.isRoomWall()) {
                    visited.add(blockBuilder.getPos());
                    builder.addBlock(blockBuilder.build());
                    continue;
                }

                if (next.y != currentBlock.getY()) {
//                    if (!role.isRoomWall()) {
                        if (!isInRoom(chunkManager, config, next.x, next.y, next.z)) {
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

    private static boolean isInRoom(ChunkManager chunkManager, PluginConfig config, int x, int y, int z) throws WorldChunkNullException {
        if (!detectRoomWall(chunkManager, config, x, y, z, 1, 0)) return false;
        if (!detectRoomWall(chunkManager, config, x, y, z, -1, 0)) return false;
        if (!detectRoomWall(chunkManager, config, x, y, z, 0, 1)) return false;
        return detectRoomWall(chunkManager, config, x, y, z, 0, -1);
    }

    private static boolean detectRoomWall(ChunkManager chunkManager, PluginConfig config, int x, int y, int z, int offSetX, int offSetZ) throws WorldChunkNullException {
        int chunkX = ChunkUtil.chunkCoordinate(x);
        int chunkZ = ChunkUtil.chunkCoordinate(z);
        WorldChunk chunk = chunkManager.getChunk(chunkX, chunkZ);

        for (int delta = 0; delta < config.getScanRadius(); delta++) {
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

            if (BlockRole.isRoomWall(type)) return true;
        }

        return false;
    }

    private static int detectFloor(ChunkManager chunkManager, int scanRadius, int x, int y, int z) throws WorldChunkNullException {
        WorldChunk chunk = chunkManager.getChunkFromBlock(x, z);
        if (chunk == null) throw new WorldChunkNullException(x, z, true);

        int dy = -1;
        while (dy < scanRadius) {
            dy++;
            int by = y - dy;

            if (by < 0) return -1;

            int blockId = chunk.getBlock(x, by, z);

            if (blockId == 0) continue;

            BlockType type = BlockType.getAssetMap().getAsset(blockId);

            if (type == null) continue;

            if (BlockRole.isSolidBlock(type)) {
                return by;
            }
        }

        return -1;
    }
}
