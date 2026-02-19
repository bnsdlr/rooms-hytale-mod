package de.bsdlr.rooms.lib.room.block;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.logger.HytaleLogger;
import de.bsdlr.rooms.lib.asset.validators.PatternValidator;
import de.bsdlr.rooms.lib.blocks.BlockPattern;
import de.bsdlr.rooms.lib.room.RoomType;

import javax.annotation.Nonnull;

public class SimpleRoomBlockType {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final BuilderCodec<SimpleRoomBlockType> CODEC = BuilderCodec.builder(SimpleRoomBlockType.class, SimpleRoomBlockType::new)
            .appendInherited(new KeyedCodec<>("BlockPattern", BlockPattern.CODEC),
                    ((roomBlockType, s) -> roomBlockType.blockPattern = s),
                    (roomBlockType -> roomBlockType.blockPattern),
                    ((roomBlockType, parent) -> roomBlockType.blockPattern = parent.blockPattern)
            )
            .add()
            .appendInherited(new KeyedCodec<>("CountsAs", Codec.DOUBLE),
                    ((roomBlockType, s) -> roomBlockType.countsAs = s),
                    (roomBlockType -> roomBlockType.countsAs),
                    ((roomBlockType, parent) -> roomBlockType.countsAs = parent.countsAs)
            )
            .add()
            .build();
    @Nonnull
    protected BlockPattern blockPattern = new BlockPattern();
    protected double countsAs = 1;

    private String[] blockIds;

    SimpleRoomBlockType() {
    }

    public SimpleRoomBlockType(@Nonnull SimpleRoomBlockType o) {
        this.blockPattern = o.blockPattern;
        this.countsAs = o.countsAs;
        this.blockIds = o.blockIds;
    }

    public static void addMatchingBlockIds(@Nonnull SimpleRoomBlockType simpleRoomBlockType) {
        addMatchingBlockIds(simpleRoomBlockType, null);
    }

    public static void addMatchingBlockIds(@Nonnull SimpleRoomBlockType simpleRoomBlockType, RoomType roomType) {
        simpleRoomBlockType.blockIds =
                RoomBlockType.getMatchingBlockIds(simpleRoomBlockType.getBlockPattern(), roomType).toArray(String[]::new);
    }

    public String[] getMatchingBlockIds() {
        if (blockIds == null) addMatchingBlockIds(this);
        return blockIds;
    }

    @Nonnull
    public BlockPattern getBlockPattern() {
        return blockPattern;
    }

    public double getCountsAs() {
        return countsAs;
    }
}
