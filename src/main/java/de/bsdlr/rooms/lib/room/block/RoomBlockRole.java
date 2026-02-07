package de.bsdlr.rooms.lib.room.block;

import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

import javax.annotation.Nonnull;

public enum RoomBlockRole {
    ENTRANCE,
    SOLID,
    EMPTY,
    FURNITURE,
    WINDOW,
    UNKNOWN;

    public boolean isRoomWall() {
        return this == RoomBlockRole.WINDOW || this == RoomBlockRole.ENTRANCE || this == RoomBlockRole.SOLID;
    }

    public static RoomBlockRole getRole(BlockType type) {
        if (type == null || type.isUnknown()) return RoomBlockRole.UNKNOWN;
        if (type.getId().equals(BlockType.EMPTY_KEY)) return RoomBlockRole.EMPTY;

        if (isRoomWallBlock(type)) return RoomBlockRole.SOLID;
        if (isEntrance(type)) return RoomBlockRole.ENTRANCE;
        if (isWindow(type)) return RoomBlockRole.WINDOW;
        if (isFurniture(type)) return RoomBlockRole.FURNITURE;
        if (isSolidBlock(type)) return RoomBlockRole.SOLID;
        else return RoomBlockRole.UNKNOWN;
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

    public static boolean isRoomWallBlock(BlockType type) {
        return type.getMaterial() == BlockMaterial.Solid
                && (type.getHitboxType().equals("Full") || type.getHitboxType().equals("Stairs") || type.getHitboxType().equals("Block_Half"));
    }

    public static boolean isRoomWall(BlockType type) {
        return isRoomWallBlock(type) || isEntrance(type) || isWindow(type);
    }
}
