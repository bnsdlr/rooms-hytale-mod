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
import de.bsdlr.rooms.lib.asset.pattern.PatternValidator;

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
            .addValidator(PatternValidator.BLOCK_TYPE_KEYS_VALIDATOR)
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
            .afterDecode(RoomBlockType::addMatchingBlockIds)
            .build();
    @Nonnull
    protected String blockIdPattern = "*";
    protected int minCount = 1;
    protected int maxCount = Integer.MAX_VALUE;
    protected SimpleRoomBlockType[] logicOrs;

    private String[] blockIds = null;

    public RoomBlockType() {
    }

    public RoomBlockType(@Nonnull RoomBlockType other) {
        this.blockIdPattern = other.blockIdPattern;
        this.minCount = other.minCount;
        this.maxCount = other.maxCount;
        this.logicOrs = other.logicOrs;
        this.blockIds = other.blockIds;
    }

    @Nonnull
    public static List<String> getMatchingBlockIds(@Nonnull String blockIdPattern) {
        List<String> matchingBlockIds = new ArrayList<>();

        for (String blockId : BlockType.getAssetMap().getAssetMap().keySet()) {
            if (StringUtil.isGlobMatching(blockIdPattern, blockId)) {
                matchingBlockIds.add(blockId);
            }
        }

        return matchingBlockIds;
    }

    public static void addMatchingBlockIds(RoomBlockType roomBlockType) {
        try {
            roomBlockType.blockIds =
                    RoomBlockType.getMatchingBlockIds(roomBlockType.getBlockIdPattern()).toArray(new String[0]);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Could not find matching block ids.");
            throw e;
        }
    }

    public boolean matches(String blockId) {
        return StringUtil.isGlobMatching(blockIdPattern, blockId);
    }

    public String[] getMatchingBlockIds() {
//        if (this.blockIds == null) this.blockIds = RoomBlockType.getMatchingBlockIds(blockIdPattern).toArray(new String[0]);
        return this.blockIds;
    }

    @Nonnull
    public String getBlockIdPattern() {
        return blockIdPattern;
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
