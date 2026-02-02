package de.bsdlr.rooms.lib.set;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import de.bsdlr.rooms.lib.set.block.FurnitureSetBlockType;
import de.bsdlr.rooms.utils.ChunkManager;

import java.util.*;

public class FurnitureSetDetector {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static Set<FurnitureSet> detectFurnitureSetsAround(World world, int x, int y, int z, Vector3i scanRadius) {
        Set<FurnitureSet> furnitureSets = new HashSet<>();

        ChunkManager chunkManager = new ChunkManager(world);

        Map<String, FurnitureSetType> furnitureSetTypeMap = FurnitureSetType.getAssetMap().getAssetMap();

        LOGGER.atInfo().log("there are %d furniture sets.", furnitureSetTypeMap.size());

        int counter = 0;

        for (int dx = -scanRadius.x; dx < scanRadius.x; dx++) {
            for (int dy = -scanRadius.y; dy < scanRadius.y; dy++) {
                for (int dz = -scanRadius.z; dz < scanRadius.z; dz++) {
                    for (FurnitureSetType furnitureSetType : furnitureSetTypeMap.values()) {
                        int bx = x + dx;
                        int by = y + dy;
                        int bz = z + dz;
                        FurnitureSet furnitureSet = getFurnitureSetAt(chunkManager, furnitureSetType, bx, by, bz);

                        if (furnitureSet != null) {
                            furnitureSets.add(furnitureSet);
                        }
                        counter++;
                    }
                }
            }
        }

        LOGGER.atInfo().log("counter: %d", counter);

        return furnitureSets;
    }

    public static FurnitureSet getFurnitureSetAt(ChunkManager chunkManager, FurnitureSetType furnitureSetType, int x, int y, int z) {
        int blockId = chunkManager.getBlockIdAt(x, y, z);
        BlockType type = BlockType.getAssetMap().getAsset(blockId);

//        LOGGER.atInfo().log("%d blocks in furniture set (%s)", furnitureSetType.getFurnitureSetBlockTypes().length, furnitureSetType.getId());
//        LOGGER.atInfo().log("block type: " + type);

        if (type == null) {
            return null;
        }

        List<FurnitureSetBlockType> matchingFurnitureSetBlockTypes = new ArrayList<>();

        for (FurnitureSetBlockType furnitureSetBlockType : furnitureSetType.furnitureSetBlockTypes) {
            if (furnitureSetBlockType.getBlockId() == null) continue;
            if (furnitureSetBlockType.getBlockId().equals(type.getId())) {
                matchingFurnitureSetBlockTypes.add(furnitureSetBlockType);
            }
        }

//        LOGGER.atInfo().log("%d matches found.", matchingFurnitureSetBlockTypes.size());

        for (FurnitureSetBlockType furnitureSetBlockType : matchingFurnitureSetBlockTypes) {
//            LOGGER.atInfo().log("block : %d %d %d", x, y, z);
//            LOGGER.atInfo().log("offset: %d %d %d", furnitureSetBlockType.getXOffset(), furnitureSetBlockType.getYOffset(), furnitureSetBlockType.getZOffset());
            int furnitureSetX = x + (furnitureSetBlockType.getXOffset() * -1);
            int furnitureSetY = y + (furnitureSetBlockType.getYOffset() * -1);
            int furnitureSetZ = z + (furnitureSetBlockType.getZOffset() * -1);

//            LOGGER.atInfo().log("furniture root pos: %d %d %d", furnitureSetX, furnitureSetY, furnitureSetZ);

            if (furnitureSetType.isValidAt(chunkManager, furnitureSetX, furnitureSetY, furnitureSetZ)) {
                return new FurnitureSet(furnitureSetType, new Vector3i(furnitureSetX, furnitureSetY, furnitureSetZ));
            }
        }

        return null;
    }
}
