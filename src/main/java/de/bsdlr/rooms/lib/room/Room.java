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
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.config.PluginConfig;
import de.bsdlr.rooms.lib.asset.score.ScoreGroup;
import de.bsdlr.rooms.lib.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.lib.room.block.RoomBlock;
import de.bsdlr.rooms.lib.room.block.RoomBlockRole;
import de.bsdlr.rooms.lib.room.block.RoomBlockType;
import de.bsdlr.rooms.utils.PackedBox;
import de.bsdlr.rooms.utils.PositionUtils;
import de.bsdlr.rooms.utils.RoomUtils;

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
                    (room, s) -> room.roomTypeId = s,
                    room -> room.roomTypeId)
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
            .append(new KeyedCodec<>("Area", Codec.INTEGER),
                    (room, s) -> room.area = s,
                    room -> room.area)
            .addValidator(Validators.min(1))
            .add()
            .<Set<PackedBox>>append(new KeyedCodec<>("PackedBlocks", new SetCodec<>(PackedBox.CODEC, HashSet::new, false)),
                    (room, s) -> {
                        room.blocks = RoomUtils.uncompress(s);
                    },
                    room -> RoomUtils.compress(room.blocks))
            .addValidator(Validators.nonNull())
            .add()
            .build();
    @Nonnull
    protected UUID uuid;
    @Nonnull
    protected String roomTypeId;
    protected UUID worldUuid;
    protected int score;
    @Nonnull
    protected Set<Long> blocks;
    protected boolean validated = false;
    protected int area;

    Room() {
        this.uuid = UUID.randomUUID();
        this.roomTypeId = RoomType.DEFAULT_KEY;
        this.blocks = new HashSet<>();
    }

    public Room(@Nonnull String roomTypeId, @Nonnull UUID worldUuid, int score, @Nonnull Set<Long> blocks, int area) {
        this.uuid = UUID.randomUUID();
        this.roomTypeId = roomTypeId;
        this.worldUuid = worldUuid;
        this.score = score;
        this.blocks = blocks;
        this.area = area;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!obj.getClass().equals(getClass())) return false;
        Room o = (Room) obj;

        return roomTypeId.equals(o.roomTypeId) && worldUuid.equals(o.worldUuid) && score == o.score && blocks.equals(o.blocks);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(roomTypeId);
        result = 31 * result + Objects.hashCode(worldUuid);
        result = 31 * result + Objects.hashCode(score);
        result = 31 * result + Objects.hashCode(blocks);
        return result;
    }

    public RoomType getType() {
        return RoomType.getAssetMap().getAsset(roomTypeId);
    }

    public UUID getUuid() {
        return uuid;
    }

    @Nonnull
    public String getRoomTypeId() {
        return roomTypeId;
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

    public int getArea() {
        return area;
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
        private final PluginConfig config;
        private final Set<RoomBlock> blocks = new HashSet<>();
        private final Set<RoomBlock> fillerBlocks = new HashSet<>();
        private UUID worldUuid;
        private int maxY;
        private int minY;

        public Builder() {
            this.config = RoomsPlugin.get().getConfig().get();
        }

        public Builder(PluginConfig config) {
            this.config = config;
        }

        public Builder(UUID worldUuid) {
            this.worldUuid = worldUuid;
            this.config = RoomsPlugin.get().getConfig().get();
        }

        public Builder(PluginConfig config, UUID worldUuid) {
            this.worldUuid = worldUuid;
            this.config = config;
        }

        @Nonnull
        private Map<String, Integer> getBlockId2Count() {
            Map<String, Integer> blockId2Count = new HashMap<>();

            for (RoomBlock block : blocks) {
                blockId2Count.merge(block.getType().getId(), 1, Integer::sum);
            }

            return blockId2Count;
        }

        private Map<Long, Map<Integer, Boolean>> xz2IsRoomWall() {
            Map<Long, Map<Integer, Boolean>> xz2isRoomWall = new HashMap<>();

            addBlocksToY2isRoomWall(xz2isRoomWall, blocks);
            addBlocksToY2isRoomWall(xz2isRoomWall, fillerBlocks);

            return xz2isRoomWall;
        }

        private void addBlocksToY2isRoomWall(Map<Long, Map<Integer, Boolean>> y2isRoomWall, Set<RoomBlock> blocks) {
//            World world = Universe.get().getWorld(worldUuid);

            for (RoomBlock block : blocks) {
                long key = PositionUtils.pack2dPos(block.getX(), block.getZ());
                y2isRoomWall.computeIfAbsent(key, k -> new HashMap<>()).put(block.getY(), block.getRole().isRoomWall());
//                if (world != null && config.isTestBlockEnabled()) {
//                    if (block.getRole().isRoomWall())
//                        world.setBlock(block.getX(), block.getY(), block.getZ(), config.getTestBlockId());
//                }
            }
        }

//        private List<Vector3i> placeblockshere = new ArrayList<>();

        private int findArea() {
            Map<Long, Map<Integer, Boolean>> xz2IsRoomWall = xz2IsRoomWall();
            int area = 0;

            for (Map.Entry<Long, Map<Integer, Boolean>> entry : xz2IsRoomWall.entrySet()) {
                Map<Integer, Boolean> isRoomWallMap = entry.getValue();
//                int x = PositionUtils.unpack2dX(entry.getKey());
//                int z = PositionUtils.unpack2dZ(entry.getKey());
//
//                if (x == 115 && z == 25) {
//                    LOGGER.atInfo().log("%s", isRoomWallMap.toString());
//                }

                List<Boolean> isRoomWallList = new ArrayList<>();

                int minY = Integer.MAX_VALUE;
                int maxY = Integer.MIN_VALUE;

                for (Map.Entry<Integer, Boolean> yEntry : isRoomWallMap.entrySet()) {
                    int y = yEntry.getKey();
                    boolean b = yEntry.getValue();

                    if (y < minY) {
                        if (!isRoomWallList.isEmpty()) {
                            for (int dy = 0; dy < minY - y - 1; dy++) {
                                isRoomWallList.addFirst(null);
                            }
                        } else {
                            maxY = y;
                        }
                        isRoomWallList.addFirst(b);
                        minY = y;
                    } else if (y > maxY) {
                        for (int dy = 0; dy < y - maxY - 1; dy++) {
                            isRoomWallList.addLast(null);
                        }
                        isRoomWallList.addLast(b);
                        maxY = y;
                    } else {
                        int index = y - minY;
                        isRoomWallList.set(index, b);
                    }
                }

                int newArea = 0;
                int lastWall = 0;

                for (Boolean b : isRoomWallList) {
                    if (b == null) {
                        LOGGER.atInfo().log("is room wall is null");
                        continue;
                    }
                    if (b) {
                        if (lastWall >= 2) {
                            newArea++;
                        }
                        lastWall = 0;
                    } else {
                        lastWall++;
                    }
                }

                if (lastWall >= 2) {
                    newArea++;
                }

//                if (newArea > 0) {
//                    LOGGER.atInfo().log("area increased by %d at %d ~ %d", newArea, x, z);
//                    if (config.isTestBlockEnabled()) {
//                        for (int dy = 0; dy < newArea; dy++) {
//                            placeblockshere.add(new Vector3i(x, this.maxY + 2 + dy, z));
//                        }
//                    }
//                }

                area += newArea;
            }

            return area;
        }

        @Nonnull
        private List<RoomType> findMatchingRoomTypes(int area, Map<String, Integer> blockId2Count) {
            List<RoomType> matching = new ArrayList<>();

            for (RoomType type : RoomType.getAssetMap().getAssetMap().values()) {
                if (type.minArea > area) continue;
                boolean matches = true;
//                LOGGER.atInfo().log("block count: %d", type.getRoomBlocks().length);
                for (RoomBlockType blockType : type.getRoomBlocks()) {
                    int count = 0;
                    for (String matchingBlockId : blockType.getMatchingBlockIds()) {
//                        if (blockId2Count.getOrDefault(matchingBlockId, 0) > 0) {
//                            LOGGER.atWarning().log("%s matches pattern %s", matchingBlockId, blockType.getBlockIdPattern().getPattern());
//                        }
                        count += blockId2Count.getOrDefault(matchingBlockId, 0);
                    }
//                    LOGGER.atInfo().log("%d matches for %s (min: %d, max: %d)", count, blockType.getBlockIdPattern().getPattern(), blockType.getMinCount(), blockType.getMaxCount());
                    if (blockType.getMinCount() > count || blockType.getMaxCount() < count) {
                        matches = false;
                        break;
                    }
                }

                if (matches) {
                    matching.add(type);
                    LOGGER.atWarning().log("room %s matches", type.getId());
                }
            }

            return matching;
        }

        @Nonnull
        private RoomType findRoomType(int area, Map<String, Integer> blockId2Count) {
            List<RoomType> matching = findMatchingRoomTypes(area, blockId2Count);
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
                blocks.add(PositionUtils.pack3dPos(roomBlock.getPos()));
            }
            for (RoomBlock fillerBlock : this.fillerBlocks) {
                blocks.add(PositionUtils.pack3dPos(fillerBlock.getPos()));
            }

            return blocks;
        }

        // TODO: improve this...
        private int calculateScore(Map<String, Integer> blockId2Count) {
            int score = 0;

            for (Map.Entry<String, Integer> entry : blockId2Count.entrySet()) {
                BlockType type = BlockType.getAssetMap().getAsset(entry.getKey());
                if (type == null || type.isUnknown()) continue;
                if (type.getId().equals(BlockType.EMPTY_KEY)) continue;
                int count = entry.getValue();

                for (ScoreGroup group : ScoreGroup.getAssetMap().getAssetMap().values()) {
                    if (group.matches(type)) {
                        score += group.getScore() * count;
                    }
                }
            }

            return score;
        }

        public int getMaxY() {
            return maxY;
        }

        public void setMaxY(int maxY) {
            this.maxY = maxY;
        }

        public int getMinY() {
            return minY;
        }

        public void setMinY(int minY) {
            this.minY = minY;
        }

        public void setWorldUuid(UUID worldUuid) {
            this.worldUuid = worldUuid;
        }

        public void addBlock(RoomBlock roomBlock) {
            if (roomBlock.isFiller()) {
                this.fillerBlocks.add(roomBlock);
            } else {
                this.blocks.add(roomBlock);
            }
        }

        public void removeIf(Predicate<RoomBlock> f) {
            this.blocks.removeIf(f);
        }

        public Room build() throws FailedToDetectRoomException {
            int height = maxY - minY + 1;
//        LOGGER.atInfo().log("miny: %d; maxy: %d; height: %d; counter: %d", minY, maxY, height, counter);

            if (height < config.getMinRoomHeight())
                throw new FailedToDetectRoomException("Room not heigh enough (min: " + config.getMinRoomHeight() + ", actual: " + height + ").");

            int finalMaxY = maxY;
            removeIf(block -> block.getY() > finalMaxY);

            boolean hasEntrance = false;
            boolean hasLightSource = false;

            for (RoomBlock block : blocks) {
                if (hasEntrance && hasLightSource) break;
                if (block.getRole() == RoomBlockRole.ENTRANCE) hasEntrance = true;
                if (block.getType().getLight() != null) hasLightSource = true;
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

            int area = findArea();
            Map<String, Integer> blockId2Count = getBlockId2Count();
            RoomType type = findRoomType(area, blockId2Count);
            String id = type.getId() == null ? "Room" : type.getId();

//            World world = Universe.get().getWorld(worldUuid);
//
//            if (world != null && config.isTestBlockEnabled()) {
//                LOGGER.atInfo().log("Would place %d blocks (max y = %d)", placeblockshere.size(), maxY);
//                for (Vector3i pos : placeblockshere) {
//                    world.setBlock(pos.x, pos.y, pos.z, config.getTestBlockId());
//                }
//            }

            return new Room(id, worldUuid, calculateScore(blockId2Count), getEncodedBlocks(), area);
        }
    }
}
