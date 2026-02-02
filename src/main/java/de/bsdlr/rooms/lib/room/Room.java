package de.bsdlr.rooms.lib.room;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import de.bsdlr.rooms.lib.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.lib.room.block.RoomBlock;
import de.bsdlr.rooms.lib.room.block.RoomBlockRole;
import de.bsdlr.rooms.lib.room.block.RoomBlockType;
import de.bsdlr.rooms.utils.PositionUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class Room {
    public static final BuilderCodec<Room> CODEC = BuilderCodec.builder(Room.class, Room::new)
            .append(new KeyedCodec<>("UUID", Codec.UUID_BINARY),
                    (room, s) -> room.uuid = s,
                    room -> room.uuid)
            .add()
            .append(new KeyedCodec<>("Id", Codec.STRING),
                    (room, s) -> room.id = s,
                    room -> room.id)
            .addValidator(Validators.nonNull())
            .add()
            .append(new KeyedCodec<>("WorldUuid", Codec.UUID_BINARY),
                    (room, s) -> room.worldUuid = s,
                    room -> room.worldUuid)
            .addValidator(Validators.nonNull())
            .add()
            .append(new KeyedCodec<>("Score", Codec.INTEGER),
                    (room, s) -> room.score = s,
                    room -> room.score)
            .add()
            .append(new KeyedCodec<>("Blocks", new SetCodec<>(Codec.LONG, HashSet::new, false)),
                    (room, s) -> room.blocks = s,
                    room -> room.blocks)
            .addValidator(Validators.nonNull())
            .add()
            .build();
    @Nonnull
    protected UUID uuid;
    @Nonnull
    protected String id;
    protected UUID worldUuid;
    protected int score;
    @Nonnull
    protected Set<Long> blocks;
    protected boolean validated = false;

    Room() {
        this.uuid = UUID.randomUUID();
        this.id = RoomType.DEFAULT_KEY;
        this.blocks = new HashSet<>();
    }

    public Room(@Nonnull String id, @Nonnull UUID worldUuid, int score, @Nonnull Set<Long> blocks) {
        this.uuid = UUID.randomUUID();
        this.id = id;
        this.worldUuid = worldUuid;
        this.score = score;
        this.blocks = blocks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!obj.getClass().equals(getClass())) return false;
        Room o = (Room) obj;

        return id.equals(o.id) && worldUuid.equals(o.worldUuid) && score == o.score && blocks.equals(o.blocks);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(worldUuid);
        result = 31 * result + Objects.hashCode(score);
        result = 31 * result + Objects.hashCode(blocks);
        return result;
    }

    public RoomType getType() {
        return RoomType.getAssetMap().getAsset(id);
    }

    public UUID getUuid() {
        return uuid;
    }

    @Nonnull
    public String getId() {
        return id;
    }

    @Nonnull
    public UUID getWorldUuid() {
        return worldUuid;
    }

    public int getScore() {
        return score;
    }

    @Nonnull
    public Set<Long> getBlocks() {
        return blocks;
    }

    public boolean isValidated() {
        return validated;
    }

    public Room validate(Vector3i pos, long key) throws ExecutionException, InterruptedException {
        if (validated) return this;
        Room room = RoomDetector.validate(this, pos, key);
        if (this.equals(room)) validated = true;
        return room;
    }

    public static class Builder {
        private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
        private final Set<RoomBlock> blocks = new HashSet<>();
        private UUID worldUuid;

        public Builder() {
        }

        public Builder(UUID worldUuid) {
            this.worldUuid = worldUuid;
        }

        @Nonnull
        private Map<String, Integer> getBlockId2Count() {
            Map<String, Integer> blockId2Count = new HashMap<>();

            for (RoomBlock block : blocks) {
                BlockType type = BlockType.getAssetMap().getAsset(block.getBlockId());
                if (type == null) continue;
                blockId2Count.merge(type.getId(), 1, Integer::sum);
            }

            return blockId2Count;
        }

        @Nonnull
        private List<RoomType> getMatching() {
            Map<String, Integer> blockId2Count = getBlockId2Count();
            List<RoomType> matching = new ArrayList<>();

            for (RoomType type : RoomType.getAssetMap().getAssetMap().values()) {
                boolean matches = true;
//                LOGGER.atInfo().log("block count: %d", type.getRoomBlocks().length);
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
        private RoomType findRoomType() {
            List<RoomType> matching = getMatching();
            if (matching.isEmpty()) return RoomType.DEFAULT;
            if (matching.size() == 1) return matching.getFirst();

            RoomType best = null;

            for (RoomType type : matching) {
                best = RoomType.getBetter(type, best);
            }

            return best == null ? RoomType.DEFAULT : best;
        }

        @Nonnull
        private Set<Long> getEncodedBlocks() {
            Set<Long> blocks = new HashSet<>();

            for (RoomBlock roomBlock : this.blocks) {
                blocks.add(PositionUtils.encodePosition(roomBlock.getPos()));
            }

            return blocks;
        }

        // TODO: improve this...
        private int calculateScore() {
            int score = 0;

            for (RoomBlock roomBlock : blocks) {
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

        public void setWorldUuid(UUID worldUuid) {
            this.worldUuid = worldUuid;
        }

        public void addBlock(RoomBlock roomBlock) {
            this.blocks.add(roomBlock);
        }

        public void removeIf(Predicate<RoomBlock> f) {
            this.blocks.removeIf(f);
        }

        public Room build() throws FailedToDetectRoomException {
            boolean hasEntrance = false;
            boolean hasLightSource = false;

            for (RoomBlock block : blocks) {
                if (hasEntrance && hasLightSource) break;
                if (block.getRole() == RoomBlockRole.ENTRANCE) hasEntrance = true;
                if (block.getLight() != null) hasLightSource = true;
            }

            if (worldUuid == null) {
                Optional<World> world = Universe.get().getWorlds().values().stream().findFirst();
                world.ifPresent(value -> this.worldUuid = value.getWorldConfig().getUuid());
            }

            if (worldUuid == null)
                throw new FailedToDetectRoomException("Could not detect world and no world uuid was set.");
            if (!hasEntrance)
                throw new FailedToDetectRoomException("Could not build room: room has no entrance");
            if (!hasLightSource)
                throw new FailedToDetectRoomException("Could not build room: room has no light source");

            RoomType type = findRoomType();
            String id = type.getId() == null ? "Room" : type.getId();

            return new Room(id, worldUuid, calculateScore(), getEncodedBlocks());
        }
    }
}
