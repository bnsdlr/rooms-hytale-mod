package de.bsdlr.rooms.services.room.block;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;

public class RoomBlockType {
    public static final BuilderCodec<RoomBlockType> CODEC = BuilderCodec.builder(RoomBlockType.class, RoomBlockType::new)
            .appendInherited(new KeyedCodec<>("BlockId", Codec.STRING),
                    ((roomBlockType, s) -> roomBlockType.blockId = s),
                    (roomBlockType -> roomBlockType.blockId),
                    ((roomBlockType, parent) -> roomBlockType.blockId = parent.blockId)
            )
            .add()
            .appendInherited(new KeyedCodec<>("MinCount", Codec.INTEGER),
                    ((roomBlockType, s) -> roomBlockType.minCount = s),
                    (roomBlockType -> roomBlockType.minCount),
                    ((roomBlockType, parent) -> roomBlockType.minCount = parent.minCount)
            )
            .add()
            .appendInherited(new KeyedCodec<>("MaxCount", Codec.INTEGER),
                    ((roomBlockType, s) -> roomBlockType.maxCount = s),
                    (roomBlockType -> roomBlockType.maxCount),
                    ((roomBlockType, parent) -> roomBlockType.maxCount = parent.maxCount)
            )
            .add()
            .build();
    protected String blockId;
    protected int minCount = 1;
    protected int maxCount = Integer.MAX_VALUE;

    public RoomBlockType() {
    }

    public RoomBlockType(@Nonnull RoomBlockType other) {
        this.blockId = other.blockId;
        this.minCount = other.minCount;
        this.maxCount = other.maxCount;
    }

    public String getBlockId() {
        return blockId;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }
}
