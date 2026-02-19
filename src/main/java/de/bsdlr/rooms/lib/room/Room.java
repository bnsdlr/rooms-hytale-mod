package de.bsdlr.rooms.lib.room;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
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
import de.bsdlr.rooms.lib.room.block.BoundRoomBlockType;
import de.bsdlr.rooms.lib.room.block.RoomBlock;
import de.bsdlr.rooms.lib.room.block.RoomBlockRole;
import de.bsdlr.rooms.lib.room.block.RoomBlockType;
import de.bsdlr.rooms.utils.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class Room {
    public static final BuilderCodec<Room> CODEC = BuilderCodec.builder(Room.class, Room::new)
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
            .append(new KeyedCodec<>("Boxes", new SetCodec<>(PackedBox.CODEC, HashSet::new, false)),
                    (room, s) -> room.boxes = s,
                    room -> room.boxes)
            .addValidator(Validators.nonNull())
            .add()
            .append(new KeyedCodec<>("BlockMap", new MapCodec<>(Codec.INTEGER, HashMap::new, false)),
                    (room, s) -> room.blockMap = s,
                    room -> room.blockMap)
            .add()
            .build();
    @Nonnull
    protected String roomTypeId;
    @Nonnull
    protected String[] matchingRoomTypeIds;
    protected UUID worldUuid;
    protected int score;
    @Nonnull
    protected Set<PackedBox> boxes;
    @Nonnull
    protected Map<String, Integer> blockMap = new HashMap<>();
    protected boolean validated = false;
    protected int area;

    Room() {
        this.roomTypeId = RoomType.DEFAULT_KEY;
        this.matchingRoomTypeIds = new String[]{RoomType.DEFAULT_KEY};
        this.boxes = new HashSet<>();
    }

    public Room(String roomTypeId, String[] matchingRoomTypeIds, @Nonnull UUID worldUuid, int score, @Nonnull Set<PackedBox> boxes, @Nonnull Map<String, Integer> blockMap, int area) {
        if (roomTypeId == null) roomTypeId = RoomType.DEFAULT_KEY;
        if (matchingRoomTypeIds == null) matchingRoomTypeIds = new String[]{RoomType.DEFAULT_KEY};
        else Arrays.sort(matchingRoomTypeIds);

        this.roomTypeId = roomTypeId;
        this.matchingRoomTypeIds = matchingRoomTypeIds;
        this.worldUuid = worldUuid;
        this.score = score;
        this.boxes = boxes;
        this.blockMap = blockMap;
        this.area = area;
        this.validated = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!obj.getClass().equals(getClass())) return false;
        Room o = (Room) obj;

        return Arrays.equals(matchingRoomTypeIds, o.matchingRoomTypeIds)
                && worldUuid.equals(o.worldUuid)
                && score == o.score
                && area == o.area
                && blockMap.equals(o.blockMap)
                && boxes.equals(o.boxes);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Arrays.hashCode(matchingRoomTypeIds);
        result = 31 * result + Objects.hashCode(worldUuid);
        result = 31 * result + Objects.hashCode(score);
        result = 31 * result + Objects.hashCode(area);
        result = 31 * result + Objects.hashCode(blockMap);
        result = 31 * result + Objects.hashCode(boxes);
        return result;
    }

    public RoomType getType() {
        return RoomType.getAssetMap().getAsset(roomTypeId);
    }

    @Nonnull
    public String getRoomTypeId() {
        return roomTypeId;
    }

    @Nonnull
    public String[] getMatchingRoomTypeIds() {
        return matchingRoomTypeIds;
    }

    @Nonnull
    public UUID getWorldUuid() {
        return worldUuid;
    }

    public int getScore() {
        return score;
    }

    @Nonnull
    public Set<PackedBox> getBoxes() {
        return boxes;
    }

    @Nonnull
    public Map<String, Integer> getBlockMap() {
        return blockMap;
    }

    public int getArea() {
        return area;
    }

    public boolean isValidated() {
        return validated;
    }

    public boolean containsPos(long key) {
        int x = PositionUtils.unpack3dX(key);
        int y = PositionUtils.unpack3dY(key);
        int z = PositionUtils.unpack3dZ(key);

        return containsPos(x, y, z);
    }

    public boolean containsPos(Vector3i pos) {
        return containsPos(pos.x, pos.y, pos.z);
    }

    public boolean containsPos(int x, int y, int z) {
        for (PackedBox box : boxes) {
            if (box.containsPos(x, y, z)) return true;
        }

        return false;
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
        private final Map<Long, RoomBlock> blocks = new HashMap<>();
        private final Map<Long, RoomBlock> fillerBlocks = new HashMap<>();
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
        private TripleOf<Map<String, Integer>> getBlockId2CountMaps() {
            Map<String, Integer> allBlockIds2Count = new HashMap<>();
            Map<String, Integer> wallBlockId2Count = new HashMap<>();
            Map<String, Integer> floorBlockId2Count = new HashMap<>();

            for (Map.Entry<Long, RoomBlock> entry : blocks.entrySet()) {
                int kindOfWall = RoomBlockRole.whatKindOfWall(entry.getKey(), entry.getValue(), blocks);
                if (kindOfWall == RoomBlockRole.SOLID_IS_WALL) {
                    wallBlockId2Count.merge(entry.getValue().getType().getId(), 1, Integer::sum);
                } else if (kindOfWall == RoomBlockRole.SOLID_IS_FLOOR) {
                    floorBlockId2Count.merge(entry.getValue().getType().getId(), 1, Integer::sum);
                }
                allBlockIds2Count.merge(entry.getValue().getType().getId(), 1, Integer::sum);
            }

            return new TripleOf<>(allBlockIds2Count, wallBlockId2Count, floorBlockId2Count);
        }

        private Map<Long, Map<Integer, Boolean>> xz2IsRoomWall() {
            Map<Long, Map<Integer, Boolean>> xz2isRoomWall = new HashMap<>();

            addBlocksToY2isRoomWall(xz2isRoomWall, blocks.values());
            addBlocksToY2isRoomWall(xz2isRoomWall, fillerBlocks.values());

            return xz2isRoomWall;
        }

        private void addBlocksToY2isRoomWall(Map<Long, Map<Integer, Boolean>> y2isRoomWall, Collection<RoomBlock> blocks) {
//            World world = Universe.get().getWorld(worldUuid);

            for (RoomBlock block : blocks) {
                long key = PositionUtils.pack2dPos(block.getX(), block.getZ());
                y2isRoomWall.computeIfAbsent(key, k -> new HashMap<>()).put(block.getY(), block.getRole().isRoomBound());
//                if (world != null && config.isTestBlockEnabled()) {
//                    if (block.getRole().isRoomWall())
//                        world.setBlock(block.getX(), block.getY(), block.getZ(), config.getTestBlockId());
//                }
            }
        }

        private int findArea() {
            Map<Long, Map<Integer, Boolean>> xz2IsRoomWall = xz2IsRoomWall();
            int area = 0;

            for (Map.Entry<Long, Map<Integer, Boolean>> entry : xz2IsRoomWall.entrySet()) {
                Map<Integer, Boolean> isRoomWallMap = entry.getValue();
                List<Integer> ys = new ArrayList<>(isRoomWallMap.keySet());
                Collections.sort(ys);

                int lastWall = 0;

                for (Integer y : ys) {
                    if (y == null) {
                        if (lastWall >= 2) {
                            area++;
                        }
                        lastWall = 0;
                        continue;
                    }

                    if (isRoomWallMap.get(y)) {
                        if (lastWall >= 2) {
                            area++;
                        }
                        lastWall = 0;
                    } else {
                        lastWall++;
                    }
                }

                if (lastWall >= 2) {
                    area++;
                }
            }

            return area;
        }

        @Nonnull
        private List<RoomType> findMatchingRoomTypes(int area, Map<String, Integer> blockId2Count, Map<String, Integer> wallBlockId2Count, Map<String, Integer> floorBlockId2Count) {
            List<RoomType> matching = new ArrayList<>();
            int wallBlockCount = wallBlockId2Count.values().stream().reduce(Integer::sum).orElse(0);
            int floorBlockCount = floorBlockId2Count.values().stream().reduce(Integer::sum).orElse(0);

            if (wallBlockCount == 0) {
                LOGGER.atSevere().log("There are no wall blocks.");
            }
            if (floorBlockCount == 0) {
                LOGGER.atSevere().log("There are no floor blocks.");
            }

            for (RoomType type : RoomType.getAssetMap().getAssetMap().values()) {
                boolean matches = type.matches(area, blockId2Count, wallBlockId2Count, wallBlockCount, floorBlockId2Count, floorBlockCount);
                if (matches) {
                    matching.add(type);
                    LOGGER.atInfo().log("room %s matches", type.getId());
                } else {
                    LOGGER.atInfo().log("room %s does not match", type.getId());
                }
            }

            return matching;
        }

        @Nonnull
        private RoomType findBestRoomType(List<RoomType> matching) {
            RoomType best = null;

            for (RoomType type : matching) {
                best = RoomType.findBetter(type, best);
            }

            return best == null ? RoomType.DEFAULT : best;
        }

        @Nonnull
        private Set<PackedBox> getPackedBoxes() {
            Set<Long> blocks = new HashSet<>();

            blocks.addAll(this.blocks.keySet());
            blocks.addAll(this.fillerBlocks.keySet());

            return RoomUtils.compress(blocks);
        }

        private int calculateScore(RoomType roomType, Map<String, Integer> blockId2Count, Map<String, Integer> wallBlockId2Count, Map<String, Integer> floorBlockId2Count) {
            int score = roomType.getScore();

            for (Map.Entry<String, Integer> entry : blockId2Count.entrySet()) {
                BlockType type = BlockType.fromString(entry.getKey());
                if (type == null || type.isUnknown()) continue;
                if (type.getId().equals(BlockType.EMPTY_KEY)) continue;

                for (ScoreGroup group : ScoreGroup.getAssetMap().getAssetMap().values()) {
                    if (group.matches(type)) {
                        score += group.getScore() * entry.getValue();
                    }
                }

                score += roomType.getExtraScore(type.getId(), wallBlockId2Count, floorBlockId2Count) * entry.getValue();
            }

            LOGGER.atInfo().log("score is %d", score);

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
            long key = PositionUtils.pack3dPos(roomBlock.getPos());
            if (roomBlock.isFiller()) {
                this.fillerBlocks.put(key, roomBlock);
            } else {
                this.blocks.put(key, roomBlock);
            }
        }

        public void removeIf(Predicate<Map.Entry<Long, RoomBlock>> f) {
            this.blocks.entrySet().removeIf(f);
        }

        public Room build() throws FailedToDetectRoomException {
            int height = maxY - minY + 1;
//        LOGGER.atInfo().log("miny: %d; maxy: %d; height: %d; counter: %d", minY, maxY, height, counter);

            if (height < config.getMinRoomHeight())
                throw new FailedToDetectRoomException("Room not heigh enough (min: " + config.getMinRoomHeight() + ", actual: " + height + ").");

            int finalMaxY = maxY;
            removeIf(e -> e.getValue().getY() > finalMaxY);

            boolean hasEntrance = false;
            boolean hasLightSource = false;

            for (RoomBlock block : blocks.values()) {
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
            TripleOf<Map<String, Integer>> blockId2CountMaps = getBlockId2CountMaps();
            Map<String, Integer> allBlockIds2Count = blockId2CountMaps.first();
            Map<String, Integer> wallBlockId2Count = blockId2CountMaps.second();
            Map<String, Integer> floorBlockId2Count = blockId2CountMaps.third();
            List<RoomType> matchingRoomTypes = findMatchingRoomTypes(area, allBlockIds2Count, wallBlockId2Count, floorBlockId2Count);
            RoomType type = findBestRoomType(matchingRoomTypes);

            String[] ids = new String[matchingRoomTypes.size()];

            for (int i = 0; i < matchingRoomTypes.size(); i++) {
                ids[i] = matchingRoomTypes.get(i).id;
            }

            int score = calculateScore(type, allBlockIds2Count, wallBlockId2Count, floorBlockId2Count);

            return new Room(type.getId(), ids, worldUuid, score, getPackedBoxes(), allBlockIds2Count, area);
        }
    }
}
