package de.bsdlr.rooms.lib.exceptions;

import com.hypixel.hytale.math.util.ChunkUtil;

public class WorldChunkNullException extends RuntimeException {
    public WorldChunkNullException(int x, int y) {
        throw new WorldChunkNullException(x, y, false);
    }

    public WorldChunkNullException(int x, int z, boolean areBlockCoordinates) {
        super("Chunk at " + (areBlockCoordinates ? ChunkUtil.chunkCoordinate(x) : x) + " " + (areBlockCoordinates ? ChunkUtil.chunkCoordinate(z) : z) + " (x z) is null!");
    }

    public WorldChunkNullException(String message) {
        super(message);
    }
}
