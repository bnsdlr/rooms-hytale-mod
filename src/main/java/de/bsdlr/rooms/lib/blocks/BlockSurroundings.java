package de.bsdlr.rooms.lib.blocks;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import de.bsdlr.rooms.lib.asset.validators.NoDuplicateValidator;

public class BlockSurroundings {
    public static final BuilderCodec<BlockSurroundings> CODEC = BuilderCodec.builder(BlockSurroundings.class, BlockSurroundings::new)
            .appendInherited(new KeyedCodec<>("SurroundingBlocks", new ArrayCodec<>(SurroundingBlock.CODEC, SurroundingBlock[]::new)),
                    (blockSurroundings, s) -> blockSurroundings.surroundingBlocks = s,
                    blockSurroundings -> blockSurroundings.surroundingBlocks,
                    (blockSurroundings, parent) -> blockSurroundings.surroundingBlocks = parent.surroundingBlocks)
            .addValidator(new NoDuplicateValidator<>((s1, s2) -> s1.offset.equals(s2.offset), "There shouldn't be duplicate offsets."))
            .add()
            .build();
    protected SurroundingBlock[] surroundingBlocks;

    BlockSurroundings() {
    }

    public SurroundingBlock[] getSurroundingBlocks() {
        return surroundingBlocks;
    }
}
