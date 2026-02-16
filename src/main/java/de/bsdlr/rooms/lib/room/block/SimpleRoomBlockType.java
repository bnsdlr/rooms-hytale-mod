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

public class SimpleRoomBlockType {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final BuilderCodec<SimpleRoomBlockType> CODEC = BuilderCodec.builder(SimpleRoomBlockType.class, SimpleRoomBlockType::new)
            .appendInherited(new KeyedCodec<>("BlockIdPattern", Codec.STRING),
                    ((roomBlockType, s) -> roomBlockType.blockIdPattern = s),
                    (roomBlockType -> roomBlockType.blockIdPattern),
                    ((roomBlockType, parent) -> roomBlockType.blockIdPattern = parent.blockIdPattern)
            )
            .addValidator(Validators.nonNull())
            .addValidator(PatternValidator.BLOCK_TYPE_KEYS_VALIDATOR)
            .add()
            .appendInherited(new KeyedCodec<>("CountsAs", Codec.DOUBLE),
                    ((roomBlockType, s) -> roomBlockType.countsAs = s),
                    (roomBlockType -> roomBlockType.countsAs),
                    ((roomBlockType, parent) -> roomBlockType.countsAs = parent.countsAs)
            )
            .add()
            .build();
    @Nonnull
    protected String blockIdPattern = "*";
    protected double countsAs = 1;

    private String[] blockIds;

    SimpleRoomBlockType() {
    }

    public SimpleRoomBlockType(@Nonnull SimpleRoomBlockType o) {
        this.blockIdPattern = o.blockIdPattern;
        this.countsAs = o.countsAs;
        this.blockIds = o.blockIds;
    }

    public static void addMatchingBlockIds(@Nonnull SimpleRoomBlockType simpleRoomBlockType) {
        addMatchingBlockIds(simpleRoomBlockType, new String[]{"*"});
    }

    public static void addMatchingBlockIds(@Nonnull SimpleRoomBlockType simpleRoomBlockType, @Nonnull String[] allowedBlockIdPatterns) {
        simpleRoomBlockType.blockIds =
                RoomBlockType.getMatchingBlockIds(simpleRoomBlockType.getBlockIdPattern(), allowedBlockIdPatterns).stream().filter(id -> {
                    boolean matches = false;
                    for (String allowedBlockIdPattern : allowedBlockIdPatterns) {
                        if (StringUtil.isGlobMatching(allowedBlockIdPattern, id)) {
                            matches = true;
                            break;
                        }
                    }
                    return matches;
                }).toArray(String[]::new);
    }

    public String[] getMatchingBlockIds() {
        if (blockIds == null) addMatchingBlockIds(this);
        return blockIds;
    }

    @Nonnull
    public String getBlockIdPattern() {
        return blockIdPattern;
    }

    public double getCountsAs() {
        return countsAs;
    }
}
