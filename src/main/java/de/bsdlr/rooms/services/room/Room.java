package de.bsdlr.rooms.services.room;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.bsdlr.rooms.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.services.room.block.RoomBlock;
import de.bsdlr.rooms.services.room.block.RoomBlockRole;
import de.bsdlr.rooms.services.room.block.RoomBlockType;
import de.bsdlr.rooms.utils.Utils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;

public class Room {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final BuilderCodec CODEC = BuilderCodec.builder(Room.class, Room::new)
            .append(new KeyedCodec<>("AllBlocks", new SetCodec(RoomBlock.CODEC, HashSet::new, false), true),
                    Room::setAllBlocks,
                    Room::getAllBlocks).add()
            .build();
    private Set<RoomBlock> allRoomBlocks = new HashSet<>();
    private final Set<RoomBlock> solidRoomBlocks = new HashSet<>();
    private final Set<RoomBlock> furnitures = new HashSet<>();
    private final Set<RoomBlock> empty = new HashSet<>();
    private final Set<RoomBlock> entrances = new HashSet<>();
    private final Set<RoomBlock> windows = new HashSet<>();
    private final Set<RoomBlock> lightSources = new HashSet<>();

    Room() {
    }

    private void setAllBlocks(Set<RoomBlock> roomBlocks) {
        this.allRoomBlocks = roomBlocks;

        for (RoomBlock roomBlock : roomBlocks) {
            addBlockToCategory(roomBlock);
        }
    }

    private void addBlockToCategory(RoomBlock roomBlock) {
        if (roomBlock.getLight() != null) lightSources.add(roomBlock);
        switch (roomBlock.getRole()) {
            case EMPTY -> empty.add(roomBlock);
            case SOLID -> solidRoomBlocks.add(roomBlock);
            case FURNITURE -> furnitures.add(roomBlock);
            case ENTRANCE -> entrances.add(roomBlock);
            case WINDOW -> windows.add(roomBlock);
        }
    }

    public Set<RoomBlock> getAllBlocks() {
        return allRoomBlocks;
    }

    public Set<RoomBlock> getSolidBlocks() {
        return solidRoomBlocks;
    }

    public Set<RoomBlock> getFurnitures() {
        return furnitures;
    }

    public Set<RoomBlock> getEmpty() {
        return empty;
    }

    public Set<RoomBlock> getEntrances() {
        return entrances;
    }

    public Set<RoomBlock> getWindows() {
        return windows;
    }

    public Set<RoomBlock> getLightSources() {
        return lightSources;
    }

    public Map<String, Integer> getBlockId2Count() {
        Map<String, Integer> blockId2Count = new HashMap<>();

        for (RoomBlock block : allRoomBlocks) {
            BlockType type = BlockType.getAssetMap().getAsset(block.getBlockId());
            if (type == null) continue;
            blockId2Count.merge(type.getId(), 1, Integer::sum);
        }

        return blockId2Count;
    }

    public List<RoomType> getMatching() {
        Map<String, Integer> blockId2Count = getBlockId2Count();
        List<RoomType> matching = new ArrayList<>();

        for (RoomType type : RoomType.getAssetMap().getAssetMap().values()) {
            boolean matches = true;
            LOGGER.atInfo().log("block count: %d", type.getRoomBlocks().length);
            for (RoomBlockType blockType : type.getRoomBlocks()) {
                Integer count = blockId2Count.getOrDefault(blockType.getBlockId(), 0);
                if (blockType.getMinCount() > count && blockType.getMaxCount() < count) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                matching.add(type);
            }
        }

        return matching;
    }

    @Nonnull
    public RoomType findRoomType() {
        List<RoomType> matching = getMatching();
        if (matching.isEmpty()) return RoomType.DEFAULT;
        if (matching.size() == 1) return matching.getFirst();

        RoomType best = null;

        for (RoomType type : matching) {
            best = RoomType.getBetter(type, best);
        }

        return best == null ? RoomType.DEFAULT : best;
    }

    public Set<Long> getEncodedBlocks() {
        Set<Long> blocks = new HashSet<>();

        for (RoomBlock roomBlock : allRoomBlocks) {
            blocks.add(Utils.encodePosition(roomBlock.getPos()));
        }

        return blocks;
    }

    public int calculateScore() {
        int score = 0;

        for (RoomBlock roomBlock : allRoomBlocks) {
            if (roomBlock.getBlockId() == BlockType.EMPTY_ID) continue;
            if (roomBlock.getBlockId() == BlockType.UNKNOWN_ID) continue;
            if (roomBlock.getRole() == RoomBlockRole.NONE) continue;
            switch (roomBlock.getRole()) {
                case SOLID -> score += 5;
                case FURNITURE -> score += 20;
                case ENTRANCE -> score += 50;
                case WINDOW -> score += 10;
            }
            if (roomBlock.getLight() != null) {
                score += 5;
            }
        }

        return score;
    }

    public static class Builder {
        public final Room room = new Room();

        public Builder() {}

        public void addBlock(RoomBlock roomBlock) {
            this.room.addBlockToCategory(roomBlock);
            this.room.allRoomBlocks.add(roomBlock);
        }

        public void removeIf(Predicate<RoomBlock> f) {
            this.room.allRoomBlocks.removeIf(f);
            this.room.solidRoomBlocks.removeIf(f);
            this.room.furnitures.removeIf(f);
            this.room.empty.removeIf(f);
            this.room.entrances.removeIf(f);
            this.room.lightSources.removeIf(f);
        }

        public Room build() throws FailedToDetectRoomException {
            if (this.room.entrances.isEmpty()) throw new FailedToDetectRoomException("Could not build room: room has no entrance");
            if (this.room.lightSources.isEmpty()) throw new FailedToDetectRoomException("Could not build room: room has no light source");
            return this.room;
        }
    }
}
