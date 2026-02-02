package de.bsdlr.rooms.utils;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import de.bsdlr.rooms.lib.room.block.RoomBlock;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {
    private final Map<Long, WorldChunk> chunkMap;
    public final World world;

    public ChunkManager(World world) {
        this.chunkMap = new HashMap<>();
        this.world = world;
    }

    public WorldChunk getChunkFromBlock(int blockX, int blockZ) {
        return getChunk(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
    }

    public WorldChunk getChunk(int chunkX, int chunkZ) {
        return getChunk(ChunkUtil.indexChunk(chunkX, chunkZ));
    }

    public WorldChunk getChunk(long index) {
        WorldChunk chunk = this.chunkMap.get(index);
        if (chunk != null) return chunk;

        WorldChunk newChunk = ChunkManager.loadChunk(this.world, index);
        if (newChunk == null) return null;

        this.chunkMap.put(index, newChunk);
        return newChunk;
    }

    public int getBlockIdAt(Vector3i vec) {
        return getBlockIdAt(vec.x, vec.y, vec.z);
    }

    public int getBlockIdAt(int x, int y, int z) {
        WorldChunk chunk = this.getChunkFromBlock(x, z);
        return chunk.getBlock(x, y, z);
    }

    public BlockType getBlockTypeAt(Vector3i vec) {
        return getBlockTypeAt(vec.x, vec.y, vec.z);
    }

    public BlockType getBlockTypeAt(int x, int y, int z) {
        int blockId = this.getBlockIdAt(x, y, z);
        return BlockType.getAssetMap().getAsset(blockId);
    }

    public RoomBlock.Builder getRoomBlockBuilderAt(int x, int y, int z) {
        return getRoomBlockBuilderAt(new Vector3i(x, y, z));
    }

    public RoomBlock.Builder getRoomBlockBuilderAt(Vector3i vec) {
        WorldChunk chunk = this.getChunkFromBlock(vec.x, vec.z);
        int blockId = chunk.getBlock(vec.x, vec.y, vec.z);

        return new RoomBlock.Builder(blockId, vec);
    }

    public static WorldChunk loadChunk(World world, long index) {
        return world.getNonTickingChunk(index);
    }
}
