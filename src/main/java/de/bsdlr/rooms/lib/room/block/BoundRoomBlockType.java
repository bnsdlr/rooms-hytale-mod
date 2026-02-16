package de.bsdlr.rooms.lib.room.block;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.bsdlr.rooms.lib.asset.pattern.PatternValidator;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoundRoomBlockType {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final BuilderCodec<BoundRoomBlockType> CODEC = BuilderCodec.builder(BoundRoomBlockType.class, BoundRoomBlockType::new)
            .appendInherited(new KeyedCodec<>("BlockIdPattern", Codec.STRING),
                    ((roomBlockType, s) -> roomBlockType.blockIdPattern = s),
                    (roomBlockType -> roomBlockType.blockIdPattern),
                    ((roomBlockType, parent) -> roomBlockType.blockIdPattern = parent.blockIdPattern)
            )
            .addValidator(Validators.nonNull())
            .addValidator(PatternValidator.BLOCK_TYPE_KEYS_VALIDATOR)
            .add()
            .appendInherited(new KeyedCodec<>("MinPercentage", Codec.DOUBLE),
                    ((roomBlockType, s) -> roomBlockType.minPercentage = s),
                    (roomBlockType -> roomBlockType.minPercentage),
                    ((roomBlockType, parent) -> roomBlockType.minPercentage = parent.minPercentage)
            )
            .addValidator(Validators.min(0.0))
            .addValidator(Validators.max(100.0))
            .add()
            .appendInherited(new KeyedCodec<>("MaxPercentage", Codec.DOUBLE),
                    ((roomBlockType, s) -> roomBlockType.maxPercentage = s),
                    (roomBlockType -> roomBlockType.maxPercentage),
                    ((roomBlockType, parent) -> roomBlockType.maxPercentage = parent.maxPercentage)
            )
            .addValidator(Validators.min(0.0))
            .addValidator(Validators.max(100.0))
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
            .build();
    @Nonnull
    protected String blockIdPattern = "*";
    protected double minPercentage = 0;
    protected double maxPercentage = 1.0;
    protected int minCount = 1;
    protected int maxCount = Integer.MAX_VALUE;

    private String[] blockIds = null;

    public BoundRoomBlockType() {
    }

    public BoundRoomBlockType(@Nonnull BoundRoomBlockType other) {
        this.blockIdPattern = other.blockIdPattern;
        this.minPercentage = other.minPercentage;
        this.maxPercentage = other.maxPercentage;
        this.minCount = other.minCount;
        this.maxCount = other.maxCount;
        this.blockIds = other.blockIds;
    }

    @Nonnull
    public static List<String> getMatchingBlockIds(@Nonnull String blockIdPattern, String[] allowedBlockIdPatterns) {
        List<String> matchingBlockIds = new ArrayList<>();

        for (String blockId : BlockType.getAssetMap().getAssetMap().keySet()) {
            boolean idMatches = StringUtil.isGlobMatching(blockIdPattern, blockId);
            boolean matches = allowedBlockIdPatterns == null;

            if (idMatches && !matches) {
                for (String allowedBlockIdPattern : allowedBlockIdPatterns) {
                    if (StringUtil.isGlobMatching(allowedBlockIdPattern, blockId)) {
                        matches = true;
                        break;
                    }
                }
            }

            if (matches) {
                BlockType type = BlockType.getAssetMap().getAsset(blockId);

                if (type == null) continue;

                if (RoomBlockRole.getRole(type).isSolid()) {
                    matchingBlockIds.add(blockId);
                }
            }
        }

        return matchingBlockIds;
    }

    public static void addMatchingBlockIds(@Nonnull BoundRoomBlockType roomBlockType) {
        addMatchingBlockIds(roomBlockType, new String[]{"*"});
    }

    public static void addMatchingBlockIds(@Nonnull BoundRoomBlockType roomBlockType, @Nonnull String[] allowedBlockIdPatterns) {
        roomBlockType.blockIds =
                BoundRoomBlockType.getMatchingBlockIds(roomBlockType.getBlockIdPattern(), allowedBlockIdPatterns).toArray(String[]::new);
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

    public double getMinPercentage() {
        return minPercentage;
    }

    public double getMaxPercentage() {
        return maxPercentage;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public double getCount(Map<String, Integer> blockId2Count) {
        double count = 0;

        for (String matchingBlockId : getMatchingBlockIds()) {
            count += blockId2Count.getOrDefault(matchingBlockId, 0);
//            if (blockId2Count.getOrDefault(matchingBlockId, 0) > 0) LOGGER.atInfo().log("%s count: %f", matchingBlockId, count);
        }

        return count;
    }
}
