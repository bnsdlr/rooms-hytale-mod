package de.bsdlr.rooms.lib.room.block;

import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.bsdlr.rooms.utils.PositionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum RoomBlockRole {
    ENTRANCE,
    SOLID,
    EMPTY,
    FURNITURE,
    WINDOW,
    UNKNOWN;

    public static final byte SOLID_IS_NOT_SOLID = 0;
    public static final byte SOLID_IS_WALL = 1;
    public static final byte SOLID_IS_FLOOR = 2;

    public boolean isRoomBound() {
        return this == RoomBlockRole.WINDOW || this == RoomBlockRole.ENTRANCE || this == RoomBlockRole.SOLID;
    }

    public boolean isEmpty() {
        return this == RoomBlockRole.EMPTY;
    }

    public boolean isEntrance() {
        return this == RoomBlockRole.ENTRANCE;
    }

    public boolean isSolid() {
        return this == RoomBlockRole.SOLID;
    }

    public boolean isFurniture() {
        return this == RoomBlockRole.FURNITURE;
    }

    public boolean isWindow() {
        return this == RoomBlockRole.WINDOW;
    }

    public boolean isUnknown() {
        return this == RoomBlockRole.UNKNOWN;
    }

    public static void isSolid(RoomBlockType roomBlockType, ValidationResults results) {
        List<String> notSolidMatches = new ArrayList<>();

        for (String id : roomBlockType.getMatchingBlockIds()) {
            BlockType type = BlockType.getAssetMap().getAsset(id);
            if (type == null || type.isUnknown()) continue;
            if (!isSolidBlock(type)) {
                notSolidMatches.add(type.getId());
            }
        }

        if (!notSolidMatches.isEmpty()) {
            results.fail("There is a non solid block matching: " + notSolidMatches);
        }
    }

    public static RoomBlockRole getRole(BlockType type) {
        if (type == null || type.isUnknown()) return RoomBlockRole.UNKNOWN;
        if (type.getId().equals(BlockType.EMPTY_KEY)) return RoomBlockRole.EMPTY;

        if (isRoomBoundBlock(type)) return RoomBlockRole.SOLID;
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

    public static boolean isRoomBoundBlock(BlockType type) {
        return type.getMaterial() == BlockMaterial.Solid
                && (type.getHitboxType().equals("Full") || type.getHitboxType().equals("Stairs") || type.getHitboxType().equals("Block_Half"));
    }

    public static boolean isRoomBound(BlockType type) {
        return isRoomBoundBlock(type) || isEntrance(type) || isWindow(type);
    }

    public static boolean isFloor(long blockPos, RoomBlock block, Map<Long, RoomBlock> blockMap) {
        return whatKindOfWall(blockPos, block, blockMap) == SOLID_IS_FLOOR;
    }

    public static boolean isWall(long blockPos, RoomBlock block, Map<Long, RoomBlock> blockMap) {
        return whatKindOfWall(blockPos, block, blockMap) == SOLID_IS_WALL;
    }

    public static byte whatKindOfWall(long blockPos, RoomBlock block, Map<Long, RoomBlock> blockMap) {
        if (block.getRole() != RoomBlockRole.SOLID) return SOLID_IS_NOT_SOLID;
        long below = PositionUtils.update(blockPos, 0, -1, 0);
        long above = PositionUtils.update(blockPos, 0, 1, 0);
        boolean isBottomOfWall = blockMap.containsKey(above) && blockMap.get(above).getRole().isRoomBound();
        if (isBottomOfWall) return SOLID_IS_WALL;
        if (!blockMap.containsKey(below) || !blockMap.get(below).getRole().isRoomBound()) return SOLID_IS_FLOOR;
        return SOLID_IS_WALL;
    }
}
