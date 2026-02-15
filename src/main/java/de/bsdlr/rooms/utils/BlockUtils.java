package de.bsdlr.rooms.utils;

import com.hypixel.hytale.assetstore.MissingAssetException;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import de.bsdlr.rooms.lib.exceptions.WorldChunkNullException;

import java.util.ArrayList;
import java.util.List;

public class BlockUtils {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static List<Vector3i> getAllOccupiedPositions(World world, BlockType type, Vector3i target) throws WorldChunkNullException, MissingAssetException {
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(target.x, target.z));
        if (chunk == null) {
            throw new WorldChunkNullException(target.x, target.z, true);
        }

        BlockBoundingBoxes bbb = BlockBoundingBoxes.getAssetMap().getAsset(type.getHitboxTypeIndex());

        if (bbb == null)
            throw new MissingAssetException("(don't know...)", BlockBoundingBoxes.class, type.getHitboxType());

        int rotationIndex = chunk.getChunkAccessor().getBlockRotationIndex(target.x, target.y, target.z);
        BlockBoundingBoxes.RotatedVariantBoxes rotatedVariantBoxes = bbb.get(rotationIndex);

        Vector3i filler = getFiller(world.getChunkStore(), target.x, target.y, target.z);

        return getAllOccupiedPositions(rotatedVariantBoxes, filler, target);
    }

    public static List<Vector3i> getAllOccupiedPositions(BlockBoundingBoxes.RotatedVariantBoxes rotatedVariantBoxes, Vector3i filler, Vector3i target) {
        List<Vector3i> positions = new ArrayList<>();
        Box bb = rotatedVariantBoxes.getBoundingBox();

        int bx = target.x + (filler.x * -1);
        int by = target.y + (filler.y * -1);
        int bz = target.z + (filler.z * -1);

        Vector3i max = PositionUtils.toDiff(bb.min, bb.max);
        PositionUtils.applyOffset(max, bx, by, bz);

        for (int x = bx; x < max.x; x++) {
            for (int y = by; y < max.y; y++) {
                for (int z = bz; z < max.z; z++) {
                    positions.add(new Vector3i(x, y, z));
                }
            }
        }

        return positions;
    }

    public static Vector3i getFiller(ChunkStore chunkStore, int blockX, int blockY, int blockZ) {
        return getFiller(chunkStore, ChunkUtil.chunkCoordinate(blockX), ChunkUtil.indexSection(blockY), ChunkUtil.chunkCoordinate(blockZ), blockX, blockY, blockZ);
    }

    public static Vector3i getFiller(ChunkStore chunkStore, int chunkX, int chunkZ, int blockX, int blockY, int blockZ) {
        return getFiller(chunkStore, chunkX, ChunkUtil.indexSection(blockY), chunkZ, blockX, blockY, blockZ);
    }

    public static Vector3i getFiller(ChunkStore chunkStore, int chunkX, int chunkY, int chunkZ, int blockX, int blockY, int blockZ) {
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReference(chunkX, chunkY, chunkZ);
        if (sectionRef != null && sectionRef.isValid()) {
            Store<ChunkStore> cStore = chunkStore.getStore();
            BlockSection section = cStore.getComponent(sectionRef, BlockSection.getComponentType());
            if (section != null) {
                int fillerKey = section.getFiller(blockX, blockY, blockZ);
                return new Vector3i(FillerBlockUtil.unpackX(fillerKey), FillerBlockUtil.unpackY(fillerKey), FillerBlockUtil.unpackZ(fillerKey));
            } else {
                LOGGER.atSevere().log("section is null");
            }
        } else {
            LOGGER.atSevere().log("section ref is null and or invalid");
        }
        return null;
    }

    public static boolean isFiller(Vector3i filler) {
        if (filler == null) return false;
        return filler.x != 0 || filler.y != 0 || filler.z != 0;
    }

    public static boolean isFiller(ChunkStore chunkStore, int blockX, int blockY, int blockZ) {
        Vector3i filler = getFiller(chunkStore, blockX, blockY, blockZ);
        return isFiller(filler);
    }

    public static Vector3i getRoot(Vector3i filler, Vector3i fillerPos) {
        return getRoot(filler, fillerPos.x, fillerPos.y, fillerPos.z);
    }

    public static Vector3i getRoot(Vector3i filler, int x, int y, int z) {
        return new Vector3i(filler.x + (x * -1), filler.y + (y * -1), filler.z + (z * -1));
    }
}
