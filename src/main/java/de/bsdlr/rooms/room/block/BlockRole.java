package de.bsdlr.rooms.room.block;

import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public enum BlockRole {
    ENTRANCE,
    SOLID,
    EMPTY,
    FURNITURE,
    WINDOW,
    NONE;

    public boolean isRoomWall() {
        return this == BlockRole.WINDOW || this == BlockRole.ENTRANCE || this == BlockRole.SOLID;
    }

    private static BlockType blockType(int blockId) {
        return BlockType.getAssetMap().getAsset(blockId);
    }

    public static BlockRole getRole(int blockId) {
        return getRole(blockId, blockType(blockId));
    }

    public static BlockRole getRole(int blockId, BlockType type) {
        if (blockId == 0) return BlockRole.EMPTY;
        if (type == null) return BlockRole.NONE;

        if (isEntrance(type)) return BlockRole.ENTRANCE;
        if (isWindow(type)) return BlockRole.WINDOW;
        if (isFurniture(type)) return BlockRole.FURNITURE;
        if (isSolidBlock(type)) return BlockRole.SOLID;
        else return BlockRole.NONE;
    }

    public static boolean isFurniture(BlockType type) {
        return type.getId().contains("Furniture")
                || type.getDrawType() == DrawType.Model;
    }

    public static boolean isEntrance(BlockType type) {
        return type.getId().toLowerCase().contains("door")
                || type.getHitboxType().toLowerCase().contains("door");
    }

    public static boolean isWindow(BlockType type) {
        return type.getId().toLowerCase().contains("window")
                || type.getHitboxType().toLowerCase().contains("window");
    }

    public static boolean isSolidBlock(BlockType type) {
        return type.getMaterial() == BlockMaterial.Solid
                && type.isFullySupportive()
                && type.getHitboxType().equals("Full")
                && type.getDrawType() == DrawType.Cube
                && type.getOpacity() == Opacity.Solid;
    }

    public static boolean isRoomWall(BlockType type) {
        return isSolidBlock(type) || isEntrance(type) || isWindow(type);
    }
}
