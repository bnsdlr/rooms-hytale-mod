package de.bsdlr.rooms.lib.blocks;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import de.bsdlr.rooms.lib.asset.validators.NoDuplicateValidator;

import javax.annotation.Nonnull;

public class SurroundingBlock {
    public static final NoDuplicateValidator<SurroundingBlock> NO_DUPLICATE_IN_ARRAY_VALIDATOR = new NoDuplicateValidator<>((s1, s2) -> s1.offset.equals(s2.offset), "There shouldn't be duplicate offsets.");
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
