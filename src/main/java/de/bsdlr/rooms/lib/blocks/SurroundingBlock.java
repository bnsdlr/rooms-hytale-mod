package de.bsdlr.rooms.lib.blocks;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;

public class SurroundingBlock {
    public static final BuilderCodec<SurroundingBlock> CODEC = BuilderCodec.builder(SurroundingBlock.class, SurroundingBlock::new)
            .appendInherited(new KeyedCodec<>("BlockPattern", BlockPattern.CODEC),
                    (surroundingBlock, s) -> surroundingBlock.blockPattern = s,
                    surroundingBlock -> surroundingBlock.blockPattern,
                    (surroundingBlock, parent) -> surroundingBlock.blockPattern = parent.blockPattern)
            .add()
            .appendInherited(new KeyedCodec<>("Offset", Vector3i.CODEC),
                    (surroundingBlock, s) -> surroundingBlock.offset = s,
                    surroundingBlock -> surroundingBlock.offset,
                    (surroundingBlock, parent) -> surroundingBlock.offset = parent.offset)
            .add()
            .build();
    @Nonnull
    protected BlockPattern blockPattern = new BlockPattern();
    @Nonnull
    protected Vector3i offset = new Vector3i();

    SurroundingBlock() {}

    public SurroundingBlock(@Nonnull SurroundingBlock other) {
        this.blockPattern = other.blockPattern;
        this.offset = other.offset;
    }

    @Nonnull
    public BlockPattern getBlockPattern() {
        return blockPattern;
    }

    public Vector3i getOffset() {
        return offset;
    }
}
