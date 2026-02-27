package de.bsdlr.rooms.lib.blocks;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import de.bsdlr.rooms.lib.room.block.RoomBlock;
import de.bsdlr.rooms.utils.PositionUtils;

import java.util.Map;

public class BlockSurroundings {
    public static final BuilderCodec<BlockSurroundings> CODEC = BuilderCodec.builder(BlockSurroundings.class, BlockSurroundings::new)
            .appendInherited(new KeyedCodec<>("SurroundingBlocks", new ArrayCodec<>(SurroundingBlock.CODEC, SurroundingBlock[]::new)),
                    (blockSurroundings, s) -> blockSurroundings.surroundingBlocks = s,
                    blockSurroundings -> blockSurroundings.surroundingBlocks,
                    (blockSurroundings, parent) -> blockSurroundings.surroundingBlocks = parent.surroundingBlocks)
            .addValidator(SurroundingBlock.NO_DUPLICATE_IN_ARRAY_VALIDATOR)
            .add()
            .build();
    protected SurroundingBlock[] surroundingBlocks;

    BlockSurroundings() {
    }

    public SurroundingBlock[] getSurroundingBlocks() {
        return surroundingBlocks;
    }

    public boolean matches(RoomBlock roomBlock, Map<Long, RoomBlock> blocks, int minMatches) {
        if (surroundingBlocks == null || surroundingBlocks.length == 0) return true;
        int count = 0;

        for (Rotation rotation : Rotation.values()) {
            boolean matches = true;

            for (SurroundingBlock surroundingBlock : surroundingBlocks) {
                Vector3i rotatedOffset = new Vector3i();
                rotation.rotateY(surroundingBlock.offset, rotatedOffset);

                int bx = roomBlock.getX() + rotatedOffset.x;
                int by = roomBlock.getY() + rotatedOffset.y;
                int bz = roomBlock.getZ() + rotatedOffset.z;

                long key = PositionUtils.pack3dPos(bx,by,bz);

                RoomBlock blockAtOffset = blocks.get(key);

                if (blockAtOffset == null || !surroundingBlock.blockPattern.matches(blockAtOffset.getType())) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                if (++count >= minMatches) return true;
            }
        }

        return false;
    }
}
