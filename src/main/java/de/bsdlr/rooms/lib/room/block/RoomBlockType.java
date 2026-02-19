package de.bsdlr.rooms.lib.room.block;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.bsdlr.rooms.lib.asset.Light;
import de.bsdlr.rooms.lib.asset.validators.PatternValidator;
import de.bsdlr.rooms.lib.blocks.BlockPattern;
import de.bsdlr.rooms.lib.room.RoomType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomBlockType {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final ValidatorCache<RoomBlockType> VALIDATOR_CACHE = new ValidatorCache<>(new RoomBlockTypeValidator());
    public static final BuilderCodec<RoomBlockType> CODEC = BuilderCodec.builder(RoomBlockType.class, RoomBlockType::new)
            .appendInherited(new KeyedCodec<>("BlockIdPattern", Codec.STRING),
                    ((roomBlockType, s) -> roomBlockType.blockIdPattern = s),
                    (roomBlockType -> roomBlockType.blockIdPattern),
                    ((roomBlockType, parent) -> roomBlockType.blockIdPattern = parent.blockIdPattern)
            )
            .addValidator(Validators.nonNull())
            .addValidator(PatternValidator.BLOCK_IDS)
            .add()
            .appendInherited(new KeyedCodec<>("MinCount", Codec.INTEGER),
                    ((roomBlockType, s) -> roomBlockType.minCount = s),
                    (roomBlockType -> roomBlockType.minCount),
                    ((roomBlockType, parent) -> roomBlockType.minCount = parent.minCount)
            )
            .addValidator(Validators.min(1))
            .add()
            .appendInherited(new KeyedCodec<>("MaxCount", Codec.INTEGER),
                    ((roomBlockType, s) -> roomBlockType.maxCount = s),
                    (roomBlockType -> roomBlockType.maxCount),
                    ((roomBlockType, parent) -> roomBlockType.maxCount = parent.maxCount)
            )
            .add()
            .appendInherited(new KeyedCodec<>("LogicOrs", new ArrayCodec<>(SimpleRoomBlockType.CODEC, SimpleRoomBlockType[]::new)),
                    ((roomBlockType, s) -> roomBlockType.logicOrs = s),
                    (roomBlockType -> roomBlockType.logicOrs),
                    ((roomBlockType, parent) -> roomBlockType.logicOrs = parent.logicOrs)
            )
            .documentation("All counts are added and used to check if enough (and not to much) blocks are present.")
            .add()
            .build();
    @Nonnull
    protected String blockIdPattern = "*";
    protected BlockPattern blockPattern = new BlockPattern();
    @Nonnull
    protected Light light;
    protected int minCount = 1;
    protected int maxCount = Integer.MAX_VALUE;
    protected SimpleRoomBlockType[] logicOrs = new SimpleRoomBlockType[0];

    private String[] blockIds = null;

    public RoomBlockType() {
    }

    public RoomBlockType(@Nonnull RoomBlockType other) {
        this.blockIdPattern = other.blockIdPattern;
        this.light = other.light;
        this.minCount = other.minCount;
        this.maxCount = other.maxCount;
        this.logicOrs = other.logicOrs;
        this.blockIds = other.blockIds;
    }

    @Nonnull
    public static List<String> getMatchingBlockIds(@Nonnull String blockIdPattern, RoomType roomType) {
        List<String> matchingBlockIds = new ArrayList<>();

        for (BlockType type : BlockType.getAssetMap().getAssetMap().values()) {
            if (roomType != null && !roomType.isBlockIdAllowed(type)) continue;

            if (StringUtil.isGlobMatching(blockIdPattern, type.getId())) {
                matchingBlockIds.add(type.getId());
            }
        }

        return matchingBlockIds;
    }

    public static void addMatchingBlockIds(@Nonnull RoomBlockType roomBlockType) {
        addMatchingBlockIds(roomBlockType, null);
    }

    public static void addMatchingBlockIds(@Nonnull RoomBlockType roomBlockType, RoomType roomType) {
        roomBlockType.blockIds =
                RoomBlockType.getMatchingBlockIds(roomBlockType.getBlockIdPattern(), roomType).toArray(String[]::new);
    }

    public static void addMatchingBlockIdsForLogicOrs(RoomBlockType roomBlockType, RoomType roomType) {
        for (SimpleRoomBlockType simpleRoomBlockType : roomBlockType.logicOrs) {
            SimpleRoomBlockType.addMatchingBlockIds(simpleRoomBlockType,  roomType);
        }
    }

    public boolean matches(String blockId) {
        return StringUtil.isGlobMatching(blockIdPattern, blockId);
    }

    public String[] getMatchingBlockIds() {
        if (this.blockIds == null) addMatchingBlockIds(this);
        return this.blockIds;
    }

    @Nonnull
    public String getBlockIdPattern() {
        return blockIdPattern;
    }

    @Nonnull
    public Light getLight() {
        return light;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public SimpleRoomBlockType[] getLogicOrs() {
        return logicOrs;
    }

    public double getCount(Map<String, Integer> blockId2Count) {
        double count = 0;

        for (String matchingBlockId : getMatchingBlockIds()) {
            count += blockId2Count.getOrDefault(matchingBlockId, 0);
//            if (blockId2Count.getOrDefault(matchingBlockId, 0) > 0) LOGGER.atInfo().log("%s count: %f", matchingBlockId, count);
        }

        if (logicOrs != null) {
            for (SimpleRoomBlockType or : logicOrs) {
                for (String matchingBlockId : or.getMatchingBlockIds()) {
                    count += blockId2Count.getOrDefault(matchingBlockId, 0) * or.countsAs;
//                    if (blockId2Count.getOrDefault(matchingBlockId, 0) * or.countsAs > 0) LOGGER.atInfo().log("or %s count: %f", matchingBlockId, count);
                }
            }
        }

        return count;
    }
}
