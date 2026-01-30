package de.bsdlr.rooms.util;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import de.bsdlr.rooms.room.block.Block;

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

    public Block.BlockBuilder getBlockBuilderAt(int x, int y, int z) {
        return getBlockBuilderAt(new Vector3i(x, y, z));
    }

    public Block.BlockBuilder getBlockBuilderAt(Vector3i vec) {
        WorldChunk chunk = this.getChunkFromBlock(vec.x, vec.z);
        int blockId = chunk.getBlock(vec.x, vec.y, vec.z);

        return new Block.BlockBuilder(blockId, vec);
    }

    public static WorldChunk loadChunk(World world, long index) {
        return world.getNonTickingChunk(index);
    }
}
