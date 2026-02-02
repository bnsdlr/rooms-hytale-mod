package de.bsdlr.rooms.lib.room.block;

import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public enum RoomBlockRole {
    ENTRANCE,
    SOLID,
    EMPTY,
    FURNITURE,
    WINDOW,
    NONE;

    public boolean isRoomWall() {
        return this == RoomBlockRole.WINDOW || this == RoomBlockRole.ENTRANCE || this == RoomBlockRole.SOLID;
    }

    private static BlockType blockType(int blockId) {
        return BlockType.getAssetMap().getAsset(blockId);
    }

    public static RoomBlockRole getRole(int blockId) {
        return getRole(blockId, blockType(blockId));
    }

    public static RoomBlockRole getRole(int blockId, BlockType type) {
        if (blockId == 0) return RoomBlockRole.EMPTY;
        if (type == null) return RoomBlockRole.NONE;

        if (isEntrance(type)) return RoomBlockRole.ENTRANCE;
        if (isWindow(type)) return RoomBlockRole.WINDOW;
        if (isFurniture(type)) return RoomBlockRole.FURNITURE;
        if (isSolidBlock(type)) return RoomBlockRole.SOLID;
        else return RoomBlockRole.NONE;
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
